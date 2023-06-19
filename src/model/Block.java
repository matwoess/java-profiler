package model;

import java.io.Serializable;
import java.util.Objects;

public class Block implements Serializable, Component {
  public Class clazz;
  public Method method;
  public int beg;
  public int end;
  public int begPos;
  public int endPos;
  public BlockType blockType;
  public boolean startsWithThrow = false;

  public int incInsertPosition;
  transient public int hits;

  public String toString() {
    return String.format("%s%s: {%d[%s%s]-%s[%s]} (%s)%s",
        clazz.name,
        method != null ? ("." + method.name) : "",
        beg,
        begPos,
        incInsertPosition != 0 ? "(" + incInsertPosition + ")" : "",
        end != 0 ? end : "?",
        endPos != 0 ? endPos : "?",
        blockType.toString(),
        method == null ? " [class-level]" : ""
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
    result = 31 * result + incInsertPosition;
    return result;
  }

  public int getIncInsertPos() {
    return (incInsertPosition != 0) ? incInsertPosition : begPos;
  }
}
