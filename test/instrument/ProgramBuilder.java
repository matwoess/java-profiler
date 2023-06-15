package instrument;

import model.*;
import model.Class;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;

public class ProgramBuilder {

  public static JavaFile jFile(model.Class... classes) {
    JavaFile javaFile = new JavaFile(Path.of("."));
    javaFile.topLevelClasses = new ArrayList<>();
    javaFile.foundBlocks = new ArrayList<>();
    for (model.Class clazz : classes) {
      javaFile.topLevelClasses.add(clazz);
      javaFile.foundBlocks.addAll(clazz.getMethodsRecursive().stream()
          .flatMap(method -> method.blocks.stream())
          .toList());
      javaFile.foundBlocks.addAll(clazz.classBlocks);
      javaFile.foundBlocks.sort(Comparator.comparingInt(b -> b.begPos));
    }
    return javaFile;
  }

  public static JavaFile jFile(String packageName, int beginOfImports, model.Class... classes) {
    JavaFile javaFile = jFile(classes);
    javaFile.beginOfImports = beginOfImports;
    javaFile.topLevelClasses.forEach(clazz -> clazz.packageName = packageName);
    javaFile.topLevelClasses.stream().flatMap(tlc->tlc.innerClasses.stream()).forEach(clazz -> clazz.packageName = packageName);
    return javaFile;
  }

  public static Class jClass(String name, boolean isMain, Component... classChildren) {
    Class clazz = new Class(name, isMain);
    for (Component child : classChildren) {
      if (child instanceof Class innerClass) {
        innerClass.setParentClass(clazz);
      } else if (child instanceof Method method) {
        clazz.methods.add(method);
        method.blocks.forEach(block -> block.clazz = clazz);
      } else if (child instanceof Block classLevelBlock) {
        clazz.classBlocks.add(classLevelBlock);
        classLevelBlock.clazz = clazz;
      } else {
        throw new RuntimeException("invalid child of class");
      }
    }
    return clazz;
  }

  public static Class jClass(String name, Component... classChildren) {
    return jClass(name, false, classChildren);
  }

  public static Method jMethod(String name, boolean isMain, Block... blocks) {
    Method method = new Method(name, isMain);
    for (Block block : blocks) {
      block.method = method;
      method.blocks.add(block);
    }
    return method;
  }

  public static Method jMethod(String name, Block... blocks) {
    return jMethod(name, false, blocks);
  }

  public static Block jBlock(BlockType type, int beg, int end, int begPos, int endPos) {
    Block b = new Block();
    b.blockType = type;
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
}
