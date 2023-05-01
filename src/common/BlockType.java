package common;

public enum BlockType {
  METHOD, BLOCK, STATIC, SS_BLOCK, LAMBDA, SS_LAMBDA, SS_SWITCH_EXPR_CASE;

  public boolean hasNoBraces() {
    return this == SS_BLOCK || this == SS_LAMBDA || this == SS_SWITCH_EXPR_CASE;
  }

  @Override
  public String toString() {
    return switch (this) {
      case BLOCK -> "b";
      case METHOD -> "m";
      case STATIC -> "static";
      case SS_BLOCK -> "ssb";
      case SS_SWITCH_EXPR_CASE -> "sssec";
      case LAMBDA -> "l";
      case SS_LAMBDA -> "ssl";
    };
  }

  public String describe() {
    String prefix = switch (this) {
      case BLOCK -> "";
      case METHOD -> "method ";
      case STATIC -> "static ";
      case SS_BLOCK -> "single-statement ";
      case LAMBDA -> "lambda ";
      case SS_LAMBDA -> "single-statement lambda ";
      case SS_SWITCH_EXPR_CASE -> "single-statement switch-expression case ";
    };
    return prefix + "block";
  }
}
