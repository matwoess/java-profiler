package tool.model;

import static tool.model.BlockType.*;

public enum JumpStatement {
  BREAK, CONTINUE, RETURN, YIELD, THROW;
  String label = null;

  public boolean stopPropagationAt(Block block) {
    boolean matchesLabelOrNone = label == null || block.labels.contains(label);
    return switch (this) {
      case BREAK -> block.blockType == LOOP && matchesLabelOrNone || block.isSwitchStatementCase();
      case CONTINUE -> block.blockType == LOOP && matchesLabelOrNone;
      case YIELD -> block.isSwitchExpressionCase();
      case RETURN -> block.blockType == METHOD || block.blockType == LAMBDA;
      case THROW -> block.blockType == METHOD || block.blockType == TRY;
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

  public static JumpStatement fromTokenWithLabel(String tokenValue, String label) {
    assert tokenValue.equals("break") || tokenValue.equals("continue");
    JumpStatement jumpStatement = switch (tokenValue) {
      case "break" -> JumpStatement.BREAK;
      case "continue" -> JumpStatement.CONTINUE;
      default -> throw new RuntimeException("unknown jump statement '" + tokenValue + "'");
    };
    jumpStatement.label = label;
    return jumpStatement;
  }
}
