package tool.model;

public enum BlockType {
  BLOCK, METHOD, CONSTRUCTOR, STATIC, LOOP, LAMBDA, SWITCH_CASE, SWITCH_EXPR_CASE;

  public String toString() {
    String prefix = switch (this) {
      case BLOCK -> "";
      case METHOD -> "method ";
      case CONSTRUCTOR -> "constructor ";
      case STATIC -> "static ";
      case LOOP -> "loop ";
      case LAMBDA -> "lambda ";
      case SWITCH_CASE -> "switch case ";
      case SWITCH_EXPR_CASE -> "switch expression case ";
    };
    return prefix + "block";
  }
}
