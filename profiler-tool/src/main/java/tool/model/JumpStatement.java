package tool.model;

import static tool.model.BlockType.*;

public enum JumpStatement {
  BREAK, CONTINUE, RETURN, YIELD, THROW;

  public boolean stopPropagationAt(BlockType blockType) {
    return switch (this) {
      case BREAK -> blockType.isLoop() || blockType == SWITCH_CASE;
      case CONTINUE -> blockType.isLoop();
      case YIELD -> blockType == SWITCH_CASE;
      case RETURN -> blockType == METHOD || blockType == LAMBDA;
      case THROW -> blockType == METHOD; // TODO: new block type TRY
    };
  }

  public boolean abortPropagation(BlockType blockType) {
    return switch (this) {
      case RETURN -> blockType == LAMBDA;
      case YIELD -> blockType == SWITCH_CASE;
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
