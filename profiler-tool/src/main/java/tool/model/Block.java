package tool.model;

import java.io.Serializable;
import java.util.Objects;

public class Block implements Serializable, Component {
  public JClass clazz;
  public Method method;
  public int beg;
  public int end;
  public int begPos;
  public int endPos;
  public BlockType blockType;
  public boolean startsWithThrow = false;
  public boolean endsWithJumpStatement = false;

  public int incInsertPosition;
  transient public int hits;

  public Block(BlockType type) {
    blockType = type;
  }

  public void setParentMethod(Method method) {
    assert this.method == null;
    if (method != null) {
      this.method = method;
      method.blocks.add(this);
    }
  }

  public void setParentClass(JClass clazz) {
    assert this.clazz == null;
    this.clazz = clazz;
    if (method == null) {
      clazz.classBlocks.add(this);
    }
  }

  public String toString() {
    return String.format("%s%s: {%d[%s%s]-%s[%s]} (%s)%s%s",
        clazz.name,
        method != null ? ("." + method.name) : "",
        beg,
        begPos,
        incInsertPosition != 0 ? "(" + incInsertPosition + ")" : "",
        end != 0 ? end : "?",
        endPos != 0 ? endPos : "?",
        blockType.toString(),
        method == null ? " [class-level]" : "",
        startsWithThrow ? " [throw]" : ""
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Block block = (Block) o;
    if (beg != block.beg) return false;
    if (end != block.end) return false;
    if (begPos != block.begPos) return false;
    if (endPos != block.endPos) return false;
    if (startsWithThrow != block.startsWithThrow) return false;
    if (incInsertPosition != block.incInsertPosition) return false;
    if (!clazz.equals(block.clazz)) return false;
    if (!Objects.equals(method, block.method)) return false;
    return blockType == block.blockType;
  }

  @Override
  public int hashCode() {
    int result = clazz.hashCode();
    result = 31 * result + (method != null ? method.hashCode() : 0);
    result = 31 * result + beg;
    result = 31 * result + end;
    result = 31 * result + begPos;
    result = 31 * result + endPos;
    result = 31 * result + blockType.hashCode();
    result = 31 * result + (startsWithThrow ? 1 : 0);
    result = 31 * result + incInsertPosition;
    return result;
  }

  public int getIncInsertPos() {
    return (incInsertPosition != 0) ? incInsertPosition : begPos;
  }
}
