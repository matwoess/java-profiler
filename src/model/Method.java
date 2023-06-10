package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Method implements Serializable {
  public String name;
  public boolean isMain;
  public List<Block> blocks = new ArrayList<>();

  public Method(String name) {
    this.name = name;
  }

  public Method(String name, boolean isMain) {
    this.name = name;
    this.isMain = isMain;
  }

  public boolean isAbstract() {
    return blocks.size() == 0;
  }

  public Block getMethodBlock() {
    Block methodBlock = blocks.get(0);
    assert methodBlock.blockType == BlockType.METHOD || methodBlock.blockType == BlockType.CONSTRUCTOR;
    return methodBlock;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Method method = (Method) o;
    if (isMain != method.isMain) return false;
    return name.equals(method.name);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (isMain ? 1 : 0);
    return result;
  }
}
