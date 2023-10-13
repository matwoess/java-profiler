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
}
