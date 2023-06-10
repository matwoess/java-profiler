package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Class implements Serializable {
  public String name;
  public boolean isMain;
  public ClassType classType;
  public List<Method> methods = new ArrayList<>();
  public List<Block> classBlocks = new ArrayList<>();

  public Class(String name) {
    this.name = name;
  }

  public Class(String name, boolean isMain) {
    this.name = name;
    this.isMain = isMain;
  }

  public int getAggregatedMethodBlockCounts() {
    return methods.stream()
        .flatMap(method -> method.blocks.stream())
        .filter(b -> b.blockType == BlockType.METHOD)
        .mapToInt(b -> b.hits)
        .sum();
  }

  public Path getReportMethodIndexPath() {
    return Constants.reportDir.resolve("index_" + name + ".html");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Class clazz = (Class) o;
    if (isMain != clazz.isMain) return false;
    return name.equals(clazz.name);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (isMain ? 1 : 0);
    return result;
  }
}
