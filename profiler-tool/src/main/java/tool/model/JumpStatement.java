package tool.model;

import static tool.model.BlockType.*;

public enum JumpStatement {
  BREAK, CONTINUE, RETURN, YIELD, THROW;

  public boolean stopPropagationAt(BlockType blockType) {
    return switch (this) {
      case BREAK -> blockType == LOOP || blockType == SWITCH_CASE;
      case CONTINUE -> blockType == LOOP;
      case YIELD -> blockType == SWITCH_EXPR_CASE;
      case RETURN -> blockType == METHOD || blockType == LAMBDA;
      case THROW -> blockType == METHOD || blockType == TRY;
    };
  }

  public boolean abortPropagation(BlockType blockType) {
    return switch (this) {
      case RETURN -> blockType == LAMBDA;
      case BREAK -> blockType == SWITCH_CASE;
      case YIELD -> blockType == SWITCH_EXPR_CASE;
      case THROW -> blockType == TRY;
      default -> false;
    };
  }

  public static JumpStatement fromToken(String tokenValue) {
    return switch (tokenValue) {
      case "break" -> JumpStatement.BREAK;
      case "continue" -> JumpStatement.CONTINUE;
      case "return" -> JumpStatement.RETURN;
      case "yield" -> JumpStatement.YIELD;
      case "throw" -> JumpStatement.THROW;
      default -> throw new RuntimeException("unknown jump statement '" + tokenValue + "'");
    };
  }
}
