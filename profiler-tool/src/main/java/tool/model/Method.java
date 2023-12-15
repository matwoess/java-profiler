package tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Method implements Serializable, Component {
  public final String name;
  public JClass parentClass;
  public Block methodBlock = null;

  public Method(String name) {
    this.name = name;
  }

  public void setParentClass(JClass parentClass) {
    assert this.parentClass == null;
    this.parentClass = parentClass;
    parentClass.methods.add(this);
  }

  public void setMethodBlock(Block block) {
    assert methodBlock == null && block.blockType.isMethod();
    methodBlock = block;
  }

  public Block getMethodBlock() {
    return methodBlock;
  }

  public List<Block> getBlocksRecursive() {
    if (isAbstract()) return List.of();
    List<Block> blocks = new ArrayList<>();
    blocks.add(methodBlock);
    blocks.addAll(methodBlock.getInnerBlocksRecursive());
    return blocks;
  }

  public boolean isAbstract() {
    return methodBlock == null;
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
