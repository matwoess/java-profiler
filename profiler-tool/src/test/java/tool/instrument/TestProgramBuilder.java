package tool.instrument;

import tool.model.*;
import tool.model.JClass;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestProgramBuilder {
  public interface BuilderComponent {
  }

  public record BuilderClass(JClass element) implements BuilderComponent {
  }

  public record BuilderMethod(Method element) implements BuilderComponent {
    public BuilderMethod withJump(JumpStatement.Kind kind) {
      return withJump(kind, null);
    }

    public BuilderMethod withJump(JumpStatement.Kind kind, String label) {
      element.getMethodBlock().jumpStatement = new JumpStatement(kind, label);
      return this;
    }

    public BuilderMethod incOffset(int offset) {
      element.getMethodBlock().incInsertOffset = offset;
      return this;
    }
  }

  public record BuilderBlock(Block element) implements BuilderComponent {
    public BuilderBlock withJump(JumpStatement.Kind kind) {
      return withJump(kind, null);
    }

    public BuilderBlock withJump(JumpStatement.Kind kind, String label) {
      element.jumpStatement = new JumpStatement(kind, label);
      return this;
    }

    public BuilderBlock noIncOffset() {
      return incOffset(0);
    }

    public BuilderBlock incOffset(int offset) {
      element.incInsertOffset = offset;
      return this;
    }
  }

  public static JavaFile jFile(BuilderClass... classes) {
    JavaFile javaFile = new JavaFile(Path.of("."));
    javaFile.topLevelClasses = new ArrayList<>();
    javaFile.foundBlocks = new ArrayList<>();
    for (BuilderClass clazz : classes) {
      javaFile.topLevelClasses.add(clazz.element);
      javaFile.foundBlocks.addAll(clazz.element.getBlocksRecursive());
    }
    javaFile.foundBlocks.sort(Comparator.comparing(b -> b.beg));
    return javaFile;
  }

  public static JavaFile jFile(String packageName, int beginOfImports, BuilderClass... classes) {
    JavaFile javaFile = jFile(classes);
    javaFile.packageName = packageName;
    javaFile.beginOfImports = beginOfImports;
    javaFile.getClassesRecursive().forEach(clazz -> clazz.packageName = packageName);
    return javaFile;
  }

  public static BuilderClass jClass(String name, BuilderComponent... classChildren) {
    JClass clazz = new JClass(name, ClassType.CLASS);
    for (BuilderComponent child : classChildren) {
      if (child instanceof BuilderClass innerClass) {
        innerClass.element.setParentClass(clazz);
      } else if (child instanceof BuilderMethod method) {
        method.element.setParentClass(clazz);
        method.element.getBlocksRecursive().forEach(block -> block.setParentClass(clazz));
      } else if (child instanceof BuilderBlock classLevelBlock) {
        classLevelBlock.element.setParentClass(clazz);
      } else {
        throw new RuntimeException("invalid child of class");
      }
    }
    return new BuilderClass(clazz);
  }

  public static BuilderClass jClass(ClassType classType, String name, BuilderComponent... classChildren) {
    BuilderClass clazz = jClass(name, classChildren);
    clazz.element.classType = classType;
    return clazz;
  }

  public static BuilderMethod jMethod(String name) {
    return new BuilderMethod(new Method(name));
  }

  public static BuilderMethod jMethod(String name, int beg, int end, int begPos, int endPos, BuilderBlock... blocks) {
    Method method = new Method(name);
    BuilderBlock methodBlock = jBlock(BlockType.METHOD, beg, end, begPos, endPos);
    methodBlock.element.setParentMethod(method);
    for (BuilderBlock bb : blocks) {
      bb.element.setParentBlock(methodBlock.element);
      bb.element.setParentMethod(method);
    }
    return new BuilderMethod(method);
  }

  public static BuilderMethod jConstructor(String name, int beg, int end, int begPos, int endPos, BuilderBlock... blocks) {
    BuilderMethod builderMethod = jMethod(name, beg, end, begPos, endPos, blocks);
    builderMethod.element.methodBlock.blockType = BlockType.CONSTRUCTOR;
    return builderMethod;
  }

  public static BuilderBlock jSsBlock(BlockType type, int beg, int end, int begPos, int endPos, BuilderBlock... innerBlocks) {
    BuilderBlock bb = jBlock(type, beg, end, begPos, endPos, innerBlocks);
    bb.element.isSingleStatement = true;
    bb.element.incInsertOffset = 0;
    return bb;
  }

  public static BuilderBlock jBlock(BlockType type, int beg, int end, int begPos, int endPos, BuilderBlock... innerBlocks) {
    Block b = new Block(type);
    b.beg = new CodePosition(beg, begPos);
    b.incInsertOffset = 1; // + length of '{'
    b.end = new CodePosition(end, endPos);
    for (BuilderBlock bb : innerBlocks) {
      bb.element.setParentBlock(b);
    }
    return new BuilderBlock(b);
  }

  /* DSL-generation methods */

  public static String getBuilderCode(JavaFile javaFile) {
    StringBuilder builder = new StringBuilder();
    getBuilderCode(javaFile, builder);
    return builder.toString();
  }

  public static void getBuilderCode(JavaFile javaFile, StringBuilder builder) {
    builder.append("JavaFile expected = jFile(");
    builder.append(javaFile.packageName).append(", ").append(javaFile.beginOfImports);
    for (JClass clazz : javaFile.topLevelClasses) {
      getBuilderCode(clazz, builder);
    }
    builder.append("\n);");
  }

  public static void getBuilderCode(JClass clazz, StringBuilder builder) {
    builder.append(",\n jClass(");
    if (clazz.classType != ClassType.CLASS) {
      builder.append(clazz.classType.name()).append(", ");
    }
    if (clazz.classType == ClassType.ANONYMOUS) {
      builder.append("null");
    } else {
      builder.append('"').append(clazz.name).append('"');
    }
    if (clazz.innerClasses.size() + clazz.classBlocks.size() + clazz.methods.size() == 0) {
      builder.append(")");
      return;
    }
    for (Block classBlock : clazz.classBlocks) {
      getBuilderCode(classBlock, builder);
    }
    for (JClass innerClass : clazz.innerClasses) {
      getBuilderCode(innerClass, builder);
    }
    for (Method method : clazz.methods) {
      getBuilderCode(method, builder);
    }
    builder.append("\n)");
  }

  public static void getBuilderCode(Method method, StringBuilder builder) {
    builder.append(",\n ");
    if (!method.isAbstract() && method.getMethodBlock().blockType == BlockType.CONSTRUCTOR) {
      builder.append("jConstructor(");
    } else {
      builder.append("jMethod(");
    }
    builder.append('"').append(method.name).append('"');
    if (method.isAbstract()) {
      builder.append(")");
      return;
    }
    Block methBlock = method.getMethodBlock();
    builder.append(String.format(", %d, %d, %d, %d", methBlock.beg.line(), methBlock.end.line(), methBlock.beg.pos(), methBlock.end.pos()));
    List<Block> blocks = method.methodBlock.innerBlocks;
    if (blocks.isEmpty()) {
      builder.append(")");
    } else {
      for (Block block : blocks) {
        getBuilderCode(block, builder);
      }
      builder.append("\n)");
    }
    appendBuilderBlockSuffixes(methBlock, builder);
  }

  public static void getBuilderCode(Block block, StringBuilder builder) {
    builder.append(",\n ").append(block.isSingleStatement ? "jSsBlock" : "jBlock").append("(");
    int begPos = block.beg.pos();
    builder.append(String.format("%s, %d, %d, %d, %d", block.blockType.name(), block.beg.line(), block.end.line(), begPos, block.end.pos()));
    for (Block innerBlock : block.innerBlocks) {
      getBuilderCode(innerBlock, builder);
    }
    builder.append(")");
    appendBuilderBlockSuffixes(block, builder);
  }

  public static void appendBuilderBlockSuffixes(Block block, StringBuilder builder) {
    if (block.jumpStatement != null) {
      builder.append(".withJump(").append(block.jumpStatement.kind().name());
      String label = block.jumpStatement.label();
      if (label != null) {
        builder.append(", ").append("\"").append(label).append("\"");
      }
      builder.append(")");
    }
    if (block.blockType == BlockType.COLON_CASE) {
      builder.append(".noIncOffset()");
    } else if (block.incInsertOffset > 1) { // set and not just length of '{'
      builder.append(".incOffset(").append(block.incInsertOffset).append(")");
    }
  }
}
