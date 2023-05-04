package common;

public enum BlockType {
  BLOCK, METHOD, STATIC, SS_BLOCK, SWITCH_CASE, SS_LAMBDA, SS_SWITCH_EXPR_ARROW_CASE;

  public boolean hasNoBraces() {
    return this == SS_BLOCK || this == SWITCH_CASE || this == SS_LAMBDA || this == SS_SWITCH_EXPR_ARROW_CASE;
  }

  public boolean isNotYetSupported() {
    return this == SS_LAMBDA || this == SS_SWITCH_EXPR_ARROW_CASE;
  }

  public String toString() {
    String prefix = switch (this) {
      case BLOCK -> "";
      case METHOD -> "method ";
      case STATIC -> "static ";
      case SS_BLOCK -> "single-statement ";
      case SWITCH_CASE -> "switch case ";
      case SS_LAMBDA -> "single-statement lambda ";
      case SS_SWITCH_EXPR_ARROW_CASE -> "single-statement switch expression arrow case ";
    };
    return prefix + "block";
  }
}
