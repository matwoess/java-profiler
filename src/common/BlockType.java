package common;

public enum BlockType {
  BLOCK, METHOD, STATIC, SS_BLOCK, SWITCH_CASE, SWITCH_ARROW_CASE, SS_SWITCH_ARROW_CASE, LAMBDA, SS_LAMBDA;

  public boolean hasNoBraces() {
    return this == SS_BLOCK || this == SWITCH_CASE || this == SS_LAMBDA || this == SS_SWITCH_ARROW_CASE;
  }

  public String toString() {
    String prefix = switch (this) {
      case BLOCK -> "";
      case METHOD -> "method ";
      case STATIC -> "static ";
      case SWITCH_CASE -> "switch case ";
      case SWITCH_ARROW_CASE -> "switch arrow case ";
      case SS_BLOCK -> "single-statement ";
      case LAMBDA -> "lambda ";
      case SS_LAMBDA -> "single-statement lambda ";
      case SS_SWITCH_ARROW_CASE -> "single-statement switch arrow case ";
    };
    return prefix + "block";
  }
}
