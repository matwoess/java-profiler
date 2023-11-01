package tool.model;

import java.io.Serializable;

public record CodePosition(int line, int pos) implements Serializable, Comparable<CodePosition> {
  @Override
  public int compareTo(CodePosition other) {
    int lineComparison = Integer.compare(this.line, other.line);
    if (lineComparison != 0) return lineComparison;
    return Integer.compare(this.pos, other.pos);
  }

  @Override
  public String toString() {
    return "[l=%d,p=%d]".formatted(line, pos);
  }
}
