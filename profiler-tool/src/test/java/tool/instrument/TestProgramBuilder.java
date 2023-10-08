package tool.instrument;

import common.Util;
import tool.model.*;
import tool.model.JClass;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestProgramBuilder {

  public static JavaFile jFile(JClass... classes) {
    JavaFile javaFile = new JavaFile(Path.of("."));
    javaFile.topLevelClasses = new ArrayList<>();
    javaFile.foundBlocks = new ArrayList<>();
    for (JClass clazz : classes) {
      javaFile.topLevelClasses.add(clazz);
      javaFile.foundBlocks.addAll(clazz.getBlocksRecursive());
    }
    javaFile.foundBlocks.sort(Comparator.comparing(b -> b.beg));
    return javaFile;
  }

  public static JavaFile jFile(String packageName, int beginOfImports, JClass... classes) {
    JavaFile javaFile = jFile(classes);
    javaFile.packageName = packageName;
    javaFile.beginOfImports = beginOfImports;
    javaFile.getClassesRecursive().forEach(clazz -> clazz.packageName = packageName);
    return javaFile;
  }

  public static JClass jClass(String name, Component... classChildren) {
    JClass clazz = new JClass(name);
    for (Component child : classChildren) {
      if (child instanceof JClass innerClass) {
        innerClass.setParentClass(clazz);
      } else if (child instanceof Method method) {
        method.setParentClass(clazz);
        method.blocks.forEach(block -> block.clazz = clazz);
      } else if (child instanceof Block classLevelBlock) {
        classLevelBlock.setParentClass(clazz);
      } else {
        throw new RuntimeException("invalid child of class");
      }
    }
    return clazz;
  }

  public static JClass jClass(ClassType classType, String name, Component... classChildren) {
    JClass clazz = jClass(name, classChildren);
    clazz.classType = classType;
    return clazz;
  }

  public static Method jMethod(String name, Block... blocks) {
    Method method = new Method(name);
    for (Block block : blocks) {
      block.setParentMethod(method);
    }
    return method;
  }

  public static Method jMethod(String name, int beg, int end, int begPos, int endPos, Block... blocks) {
    Block methodBlock = jBlock(BlockType.METHOD, beg, end, begPos, endPos);
    return jMethod(name, Util.prependToArray(blocks, methodBlock));
  }

  public static Method jConstructor(String name, int beg, int end, int begPos, int endPos, Block... blocks) {
    Block constructorBlock = jBlock(BlockType.CONSTRUCTOR, beg, end, begPos, endPos);
    return jMethod(name, Util.prependToArray(blocks, constructorBlock));
  }

  public static Block jBlock(BlockType type, int beg, int end, int begPos, int endPos) {
    Block b = new Block(type);
    b.beg = new CodePosition(beg, b.blockType.hasNoBraces() ? begPos : begPos - 1);
    if (!b.blockType.hasNoBraces()) {
      b.incInsertPosition = begPos;
    }
    b.end = new CodePosition(end, endPos);
    return b;
  }

  public static Block jBlock(BlockType type, int beg, int end, int begPos, int endPos, int incInsertPosOffset) {
    Block b = jBlock(type, beg, end, begPos, endPos);
    b.incInsertPosition += incInsertPosOffset;
    return b;
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
    builder.append(String.format(", %d, %d, %d, %d", methBlock.beg, methBlock.end, methBlock.beg.pos() + 1, methBlock.end.pos()));
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

  public static void getBuilderCode(Block block, StringBuilder builder) {
    builder.append(",\n jBlock(");
    int begPos = block.blockType.hasNoBraces() ? block.beg.pos() : block.beg.pos() + 1;
    builder.append(String.format("%s, %d, %d, %d, %d", block.blockType.name(), block.beg.line(), block.end.line(), begPos, block.end.pos()));
    if (block.incInsertPosition != 0 && block.incInsertPosition != begPos) {
      builder.append(", ").append(block.incInsertPosition - begPos);
    }
    builder.append(")");
  }
}
