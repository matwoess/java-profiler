package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Class implements Serializable {
  public String name;
  public boolean isMain;
  public ClassType classType = ClassType.CLASS;
  public String packageName = "<default>";
  public Class parentClass;
  public List<Class> innerClasses = new ArrayList<>();
  public List<Method> methods = new ArrayList<>();
  public List<Block> classBlocks = new ArrayList<>();

  public Class(String name) {
    this.name = name;
  }

  public Class(String name, boolean isMain) {
    this(name);
    this.isMain = isMain;
  }

  public Class(String name, ClassType type, boolean isMain) {
    this(name, isMain);
    this.classType = type;
  }

  public void setParentClass(Class parentClass) {
    this.parentClass = parentClass;
    parentClass.innerClasses.add(this);
    if (name == null) {
      this.name = String.valueOf(parentClass.innerClasses.size());
    }
  }

  public String getName() {
    if (parentClass != null) {
      return parentClass.getFullName() + "$" + name;
    } else {
      return name;
    }
  }

  public String getFullName() {
    if (packageName.equals("<default>")) {
      return getName();
    } else {
      return packageName + "." + getName();
    }
  }

  @Override
  public String toString() {
    return this.getName();
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
        .filter(b -> b.blockType == BlockType.METHOD)
        .mapToInt(b -> b.hits)
        .sum();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Class aClass = (Class) o;
    if (isMain != aClass.isMain) return false;
    if (!Objects.equals(name, aClass.name)) return false;
    if (classType != aClass.classType) return false;
    if (!packageName.equals(aClass.packageName)) return false;
    return Objects.equals(parentClass, aClass.parentClass);
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (isMain ? 1 : 0);
    result = 31 * result + classType.hashCode();
    result = 31 * result + packageName.hashCode();
    result = 31 * result + (parentClass != null ? parentClass.hashCode() : 0);
    return result;
  }
}
