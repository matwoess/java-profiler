package tool.model;

import java.io.Serializable;

import static tool.model.BlockType.*;
import static tool.model.JumpStatement.Kind.*;

/**
 * This class is used to represent a jump statement.
 * @param kind the kind of jump statement (one of {@link Kind})
 * @param label the target label of the jump statement, if any
 */
public record JumpStatement(Kind kind, String label) implements Serializable {

  /**
   * The kind of jump statement.
   */
  public enum Kind {BREAK, CONTINUE, RETURN, YIELD, THROW}

  /**
   * Decide when to stop propagating a jump statement at a given parent block.
   * @param block the block to check
   * @return true if this kind of jump statement should be propagated only until the given block
   */
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

  /**
   * Create a new jump statement from a keyword token value.
   * @param tokenValue the keyword of the jump statement
   * @return a new jump statement
   */
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

  /**
   * Create a new jump statement from a keyword token value and a label.
   * @param tokenValue the keyword of the jump statement
   * @param label the target label of the jump statement
   * @return a new jump statement with the given label
   */
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
