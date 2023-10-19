package tool.model;

public enum BlockType {
  BLOCK, METHOD, CONSTRUCTOR, STATIC, LOOP, TRY, LAMBDA, SWITCH_STMT, SWITCH_EXPR, COLON_CASE, ARROW_CASE;

  public String toString() {
    String prefix = switch (this) {
      case BLOCK -> "";
      case METHOD -> "method ";
      case CONSTRUCTOR -> "constructor ";
      case STATIC -> "static ";
      case LOOP -> "loop ";
      case TRY -> "try ";
      case LAMBDA -> "lambda ";
      case SWITCH_STMT -> "switch statement ";
      case SWITCH_EXPR -> "switch expression ";
      case COLON_CASE -> "colon case ";
      case ARROW_CASE -> "arrow case ";
    };
    return prefix + "block";
  }

  public boolean isSwitch() {
    return this == SWITCH_STMT || this == SWITCH_EXPR;
  }

  public boolean isSwitchCase() {
    return this == COLON_CASE || this == ARROW_CASE;
  }

  public boolean hasNoCounter() {
    return isSwitch();
  }
}
