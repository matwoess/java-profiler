package tool.instrument;

import common.Util;
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
    public BuilderMethod withJump(JumpStatement jumpStatement) {
      element.getMethodBlock().jumpStatement = jumpStatement;
      return this;
    }
  }

  public record BuilderBlock(Block element) implements BuilderComponent {
    public BuilderBlock withJump(JumpStatement jumpStatement) {
      element.jumpStatement = jumpStatement;
      return this;
    }

    public BuilderBlock noIncOffset() {
      element.incInsertPosition = 0;
      element.beg = new CodePosition(element.beg.line(), element.beg.pos() + 1);
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
        method.element.blocks.forEach(block -> block.clazz = clazz);
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

  public static BuilderMethod jMethod(String name, BuilderBlock... blocks) {
    Method method = new Method(name);
    for (BuilderBlock block : blocks) {
      block.element.setParentMethod(method);
    }
    return new BuilderMethod(method);
  }

  public static BuilderMethod jMethod(String name, int beg, int end, int begPos, int endPos, BuilderBlock... blocks) {
    BuilderBlock methodBlock = jBlock(BlockType.METHOD, beg, end, begPos, endPos);
    return jMethod(name, Util.prependToArray(blocks, methodBlock));
  }

  public static BuilderMethod jConstructor(String name, int beg, int end, int begPos, int endPos, BuilderBlock... blocks) {
    BuilderBlock constructorBlock = jBlock(BlockType.CONSTRUCTOR, beg, end, begPos, endPos);
    return jMethod(name, Util.prependToArray(blocks, constructorBlock));
  }

  public static BuilderBlock jSsBlock(BlockType type, int beg, int end, int begPos, int endPos) {
    Block b = new Block(type);
    b.isSingleStatement = true;
    b.beg = new CodePosition(beg, begPos);
    b.end = new CodePosition(end, endPos);
    return new BuilderBlock(b);
  }

  public static BuilderBlock jBlock(BlockType type, int beg, int end, int begPos, int endPos) {
    Block b = new Block(type);
    b.incInsertPosition = begPos;
    b.beg = new CodePosition(beg, begPos - 1); // minus length of '{'
    b.end = new CodePosition(end, endPos);
    return new BuilderBlock(b);
  }

  public static BuilderBlock jBlock(BlockType type, int beg, int end, int begPos, int endPos, int incInsertPosOffset) {
    BuilderBlock block = jBlock(type, beg, end, begPos, endPos);
    block.element.incInsertPosition += incInsertPosOffset;
    return block;
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

  // TODO: also generate withJump calls
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
    builder.append(String.format(", %d, %d, %d, %d", methBlock.beg.line(), methBlock.end.line(), methBlock.beg.pos() + 1, methBlock.end.pos()));
    List<Block> blocks = method.blocks;
    if (blocks.size() == 1) {
      builder.append(")");
      return;
    }
    for (int i = 1; i < blocks.size(); i++) { // skip method block
      Block block = blocks.get(i);
      getBuilderCode(block, builder);
    }
    builder.append("\n)");
  }

  // TODO: also generate withJump calls
  public static void getBuilderCode(Block block, StringBuilder builder) {
    builder.append(",\n jBlock(");
    int begPos = block.isSingleStatement ? block.beg.pos() : block.beg.pos() + 1;
    builder.append(String.format("%s, %d, %d, %d, %d", block.blockType.name(), block.beg.line(), block.end.line(), begPos, block.end.pos()));
    if (block.incInsertPosition != 0 && block.incInsertPosition != begPos) {
      builder.append(", ").append(block.incInsertPosition - begPos);
    }
    builder.append(")");
  }
}
