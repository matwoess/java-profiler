package tool.model;

import static tool.model.BlockType.*;

public enum JumpStatement {
  BREAK, CONTINUE, RETURN, YIELD, THROW;

  public boolean stopPropagationAt(Block block) {
    return switch (this) {
      case BREAK -> block.blockType == LOOP || block.isSwitchStatementCase();
      case CONTINUE -> block.blockType == LOOP;
      case YIELD -> block.isSwitchExpressionCase();
      case RETURN -> block.blockType == METHOD || block.blockType == LAMBDA;
      case THROW -> block.blockType == METHOD || block.blockType == TRY;
    };
  }

  public boolean abortPropagation(Block block) {
    return switch (this) {
      case RETURN -> block.blockType == LAMBDA;
      case BREAK -> block.isSwitchStatementCase();
      case YIELD -> block.isSwitchExpressionCase();
      case THROW -> block.blockType == TRY;
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
