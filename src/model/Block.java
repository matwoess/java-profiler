package model;

import java.util.Objects;

public class Block {
  public Class clazz;
  public Method method;
  public int beg;
  public int end;
  public int begPos;
  public int endPos;
  public BlockType blockType;

  public int hits;
  public int endOfSuperCall;

  public String toString() {
     return String.format("%s%s: {%d[%s]-%s[%s]} (%s)%s",
        clazz.name,
        method != null ? ("." + method.name) : "",
        beg,
        begPos,
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
    return result;
  }
}
