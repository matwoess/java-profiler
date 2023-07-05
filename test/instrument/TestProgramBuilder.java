package instrument;

import model.*;
import model.Class;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;

public class TestProgramBuilder {

  public static JavaFile jFile(model.Class... classes) {
    JavaFile javaFile = new JavaFile(Path.of("."));
    javaFile.topLevelClasses = new ArrayList<>();
    javaFile.foundBlocks = new ArrayList<>();
    for (model.Class clazz : classes) {
      javaFile.topLevelClasses.add(clazz);
      javaFile.foundBlocks.addAll(clazz.getMethodsRecursive().stream()
          .flatMap(method -> method.blocks.stream())
          .toList());
      javaFile.foundBlocks.addAll(clazz.innerClasses.stream().flatMap(ic -> ic.classBlocks.stream()).toList());
      javaFile.foundBlocks.addAll(clazz.classBlocks);
      javaFile.foundBlocks.sort(Comparator.comparingInt(b -> b.begPos));
    }
    return javaFile;
  }

  public static JavaFile jFile(String packageName, int beginOfImports, model.Class... classes) {
    JavaFile javaFile = jFile(classes);
    javaFile.beginOfImports = beginOfImports;
    javaFile.topLevelClasses.forEach(clazz -> clazz.packageName = packageName);
    javaFile.topLevelClasses.stream().flatMap(tlc -> tlc.innerClasses.stream()).forEach(clazz -> clazz.packageName = packageName);
    return javaFile;
  }

  public static Class jClass(String name, Component... classChildren) {
    Class clazz = new Class(name);
    for (Component child : classChildren) {
      if (child instanceof Class innerClass) {
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

  public static Class jClass(ClassType classType, String name, Component... classChildren) {
    Class clazz = jClass(name, classChildren);
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
    return jMethod(name, misc.Util.prependToArray(blocks, methodBlock));
  }

  public static Method jConstructor(String name, int beg, int end, int begPos, int endPos, Block... blocks) {
    Block methodBlock = jBlock(BlockType.CONSTRUCTOR, beg, end, begPos, endPos);
    return jMethod(name, misc.Util.prependToArray(blocks, methodBlock));
  }

  public static Block jBlock(BlockType type, int beg, int end, int begPos, int endPos) {
    Block b = new Block(type);
    b.beg = beg;
    b.end = end;
    if (b.blockType.hasNoBraces()) {
      b.begPos = begPos;
    } else {
      b.incInsertPosition = begPos;
      b.begPos = begPos - 1;
    }
    b.endPos = endPos;
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
    if (javaFile.beginOfImports != 0 || javaFile.packageName != null) {
      builder.append(javaFile.packageName).append(", ").append(javaFile.beginOfImports);
    }
    for (Class clazz : javaFile.topLevelClasses) {
      getBuilderCode(clazz, builder);
    }
    builder.append("\n);");
  }

  public static void getBuilderCode(Class clazz, StringBuilder builder) {
    builder.append("\n, jClass(").append('"').append(clazz.name).append('"');
    if (clazz.innerClasses.size() + clazz.classBlocks.size() + clazz.methods.size() == 0) {
      builder.append(")");
      return;
    }
    for (Block classBlock : clazz.classBlocks) {
      getBuilderCode(classBlock, builder);
    }
    for (Class innerClass : clazz.innerClasses) {
      getBuilderCode(innerClass, builder);
    }
    for (Method method : clazz.methods) {
      getBuilderCode(method, builder);
    }
    builder.append("\n)");
  }

  public static void getBuilderCode(Method method, StringBuilder builder) {
    builder.append("\n, jMethod(").append('"').append(method.name).append('"');
    if (method.blocks.size() == 0) {
      builder.append(")");
      return;
    }
    for (Block block : method.blocks) {
      getBuilderCode(block, builder);
    }
    builder.append("\n)");
  }

  public static void getBuilderCode(Block block, StringBuilder builder) {
    builder.append("\n, jBlock(");
    int begPos = block.blockType.hasNoBraces() ? block.begPos : block.begPos + 1;
    builder.append(String.format("%s, %d, %d, %d, %d", block.blockType.name(), block.beg, block.end, begPos, block.endPos));
    if (block.incInsertPosition != 0 && block.incInsertPosition != begPos) {
      builder.append(", ").append(block.incInsertPosition - begPos);
    }
    builder.append(")");
  }
}
