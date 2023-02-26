package common;

public class Block {
  public Class clazz;
  public Method method;
  public int beg;
  public int end;
  public int begPos;
  public int endPos;
  public boolean isMethodBlock;
  public boolean insertBraces;

  public String toString() {
    return String.format("%s%s: {%d[%s]-%s[%s]}%s",
        clazz.name,
        method != null ? ("." + method.name) : "",
        beg,
        begPos,
        end != 0 ? end : "?",
        endPos != 0 ? endPos : "?",
        isMethodBlock ? " m" : insertBraces ? " ins" : ""
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
    if (isMethodBlock != block.isMethodBlock) return false;
    if (insertBraces != block.insertBraces) return false;
    if (!clazz.equals(block.clazz)) return false;
    return method.equals(block.method);
  }

  @Override
  public int hashCode() {
    int result = clazz.hashCode();
    result = 31 * result + method.hashCode();
    result = 31 * result + beg;
    result = 31 * result + end;
    result = 31 * result + begPos;
    result = 31 * result + endPos;
    result = 31 * result + (isMethodBlock ? 1 : 0);
    result = 31 * result + (insertBraces ? 1 : 0);
    return result;
  }
}
