package tool.model;

import java.io.Serializable;

import static tool.model.BlockType.*;
import static tool.model.JumpStatement.Kind.*;

public record JumpStatement(Kind kind, String label) implements Serializable {

  public enum Kind {BREAK, CONTINUE, RETURN, YIELD, THROW}

  public boolean stopPropagationAt(Block block) {
    if (label != null) return block.labels.contains(label);
    return switch (kind) {
      case BREAK -> block.blockType == LOOP || block.isSwitchStatementCase();
      case CONTINUE -> block.blockType == LOOP;
      case YIELD -> block.isSwitchExpressionCase();
      case RETURN -> block.blockType == METHOD || block.blockType == LAMBDA;
      case THROW -> block.blockType == METHOD || block.blockType == TRY;
    };
  }

  public static JumpStatement fromToken(String tokenValue) {
    Kind type = switch (tokenValue) {
      case "break" -> BREAK;
      case "continue" -> CONTINUE;
      case "return" -> RETURN;
      case "yield" -> YIELD;
      case "throw" -> THROW;
      default -> throw new RuntimeException("unknown jump statement '" + tokenValue + "'");
    };
    return new JumpStatement(type, null);
  }

  public static JumpStatement fromTokenWithLabel(String tokenValue, String label) {
    assert tokenValue.equals("break") || tokenValue.equals("continue");
    Kind type = switch (tokenValue) {
      case "break" -> BREAK;
      case "continue" -> CONTINUE;
      default -> throw new RuntimeException("unknown jump statement '" + tokenValue + "'");
    };
    return new JumpStatement(type, label);
  }

  @Override
  public String toString() {
    return kind.name() + (label == null ? "" : " " + label);
  }
}
