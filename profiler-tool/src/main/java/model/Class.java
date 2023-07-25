package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static model.ClassType.*;

public class Class implements Serializable, Component {
  public String name;
  public ClassType classType = CLASS;
  public String packageName;
  public Class parentClass;
  public List<Class> innerClasses = new ArrayList<>();
  public List<Method> methods = new ArrayList<>();
  public List<Block> classBlocks = new ArrayList<>();

  public Class(String name) {
    this.name = name;
  }

  public Class(String name, ClassType type) {
    this(name);
    this.classType = type;
  }

  public void setParentClass(Class parentClass) {
    if (name == null) {
      long nextAnonymousClass = parentClass.innerClasses.stream().filter(c -> c.classType == ANONYMOUS).count() + 1;
      this.name = String.valueOf(nextAnonymousClass);
    }
    this.parentClass = parentClass;
    parentClass.innerClasses.add(this);
  }

  public String getName() {
    if (parentClass != null) {
      return parentClass.getName() + "$" + name;
    } else {
      return name;
    }
  }

  public String getFullName() {
    if (packageName == null) {
      return getName();
    } else {
      return packageName + "." + getName();
    }
  }

  @Override
  public String toString() {
    return this.getName();
  }

  public List<Class> getClassesRecursive() {
    List<Class> allClasses = new ArrayList<>(innerClasses);
    for (Class clazz : innerClasses) {
      allClasses.addAll(clazz.getClassesRecursive());
    }
    return allClasses;
  }

  public List<Method> getMethodsRecursive() {
    List<Method> allMethods = new ArrayList<>(methods);
    if (innerClasses.size() > 0) {
      for (Class clazz : innerClasses) {
        allMethods.addAll(clazz.getMethodsRecursive());
      }
    }
    return allMethods;
  }

  public int getAggregatedMethodBlockCounts() {
    return getMethodsRecursive().stream()
        .flatMap(method -> method.blocks.stream())
        .filter(b -> b.blockType == BlockType.METHOD || b.blockType == BlockType.CONSTRUCTOR)
        .mapToInt(b -> b.hits)
        .sum();
  }

  public List<Block> getBlocksRecursive() {
    List<Block> allBlocks = new ArrayList<>(classBlocks);
    allBlocks.addAll(methods.stream().flatMap(method -> method.blocks.stream()).toList());
    for (Class clazz : innerClasses) {
      allBlocks.addAll(clazz.getBlocksRecursive());
    }
    return allBlocks;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Class aClass = (Class) o;
    if (!Objects.equals(name, aClass.name)) return false;
    if (classType != aClass.classType) return false;
    if (!Objects.equals(packageName, aClass.packageName)) return false;
    return Objects.equals(parentClass, aClass.parentClass);
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + classType.hashCode();
    result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
    result = 31 * result + (parentClass != null ? parentClass.hashCode() : 0);
    return result;
  }
}