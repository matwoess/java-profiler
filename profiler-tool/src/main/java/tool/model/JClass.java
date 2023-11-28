package tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JClass implements Serializable, Component {
  public String name;
  public ClassType classType;
  public String packageName;
  public JClass parentClass;
  public List<JClass> innerClasses = new ArrayList<>();
  public List<Method> methods = new ArrayList<>();
  public List<Block> classBlocks = new ArrayList<>();

  public JClass(String name, ClassType type) {
    this.name = name;
    this.classType = type;
  }

  public void setParentClass(JClass parentClass) {
    if (parentClass == null) return;
    if (name == null) {
      long nextAnonymousClass = parentClass.innerClasses.stream().filter(c -> c.classType == ClassType.ANONYMOUS).count() + 1;
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

  public List<JClass> getClassesRecursive() {
    List<JClass> allClasses = new ArrayList<>(innerClasses);
    for (JClass clazz : innerClasses) {
      allClasses.addAll(clazz.getClassesRecursive());
    }
    return allClasses;
  }

  public List<Method> getMethodsRecursive() {
    List<Method> allMethods = new ArrayList<>(methods);
    for (JClass clazz : innerClasses) {
      allMethods.addAll(clazz.getMethodsRecursive());
    }
    return allMethods;
  }

  public int getAggregatedMethodBlockCounts() {
    return getMethodsRecursive().stream()
        .filter(m -> !m.isAbstract())
        .map(Method::getMethodBlock)
        .mapToInt(b -> b.hits)
        .sum();
  }

  public List<Block> getBlocksRecursive() {
    List<Block> allBlocks = new ArrayList<>();
    allBlocks.addAll(classBlocks);
    allBlocks.addAll(classBlocks.stream().flatMap(block -> block.getInnerBlocksRecursive().stream()).toList());
    allBlocks.addAll(methods.stream().flatMap(method -> method.getBlocksRecursive().stream()).toList());
    for (JClass clazz : innerClasses) {
      allBlocks.addAll(clazz.getBlocksRecursive());
    }
    return allBlocks;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JClass aClass = (JClass) o;
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
