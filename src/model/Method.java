package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Method implements Serializable, Component {
  public String name;
  public Class parentClass;
  public List<Block> blocks = new ArrayList<>();

  public Method(String name) {
    this.name = name;
  }

  public void setParentClass(Class parentClass) {
    assert this.parentClass == null;
    this.parentClass = parentClass;
    parentClass.methods.add(this);
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
  public String toString() {
    return name;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Method method = (Method) o;

    if (!name.equals(method.name)) return false;
    return parentClass.equals(method.parentClass);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + parentClass.hashCode();
    return result;
  }
}
