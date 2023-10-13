package tool.model;

public enum BlockType {
  BLOCK, METHOD, CONSTRUCTOR, STATIC, LOOP, SS_LOOP, SS_BLOCK,
  LAMBDA, SS_LAMBDA, SWITCH_CASE, SS_SWITCH_EXPR_ARROW_CASE;

  public boolean hasNoBraces() {
    return this == SS_BLOCK || this == SS_LOOP || this == SWITCH_CASE || this == SS_LAMBDA || this == SS_SWITCH_EXPR_ARROW_CASE;
  }

  public String toString() {
    String prefix = switch (this) {
      case BLOCK -> "";
      case METHOD -> "method ";
      case CONSTRUCTOR -> "constructor ";
      case STATIC -> "static ";
      case LOOP -> "loop ";
      case SS_LOOP -> "single-statement loop ";
      case SS_BLOCK -> "single-statement ";
      case SWITCH_CASE -> "switch case ";
      case LAMBDA -> "lambda ";
      case SS_LAMBDA -> "single-statement lambda ";
      case SS_SWITCH_EXPR_ARROW_CASE -> "single-statement switch expression arrow case ";
    };
    return prefix + "block";
  }

  public boolean isLoop() {
    return this == LOOP || this == SS_LOOP;
  }
}
