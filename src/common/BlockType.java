package common;

public enum BlockType {
  METHOD, BLOCK, STATIC, SS_BLOCK, LAMBDA, SS_LAMBDA, SS_SWITCH_EXPR_CASE;

  public boolean hasNoBraces() {
    return this == SS_BLOCK || this == SS_LAMBDA || this == SS_SWITCH_EXPR_CASE;
  }

  @Override
  public String toString() {
    return switch (this) {
      case METHOD -> "m";
      case BLOCK -> "b";
      case STATIC -> "static";
      case SS_BLOCK -> "ssb";
      case SS_SWITCH_EXPR_CASE -> "sssec";
      case LAMBDA -> "l";
      case SS_LAMBDA -> "ssl";
    };
  }
}
