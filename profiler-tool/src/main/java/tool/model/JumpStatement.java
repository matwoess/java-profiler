package tool.model;

public enum JumpStatement {
  BREAK, CONTINUE, RETURN, YIELD, THROW;

  public boolean stopPropagationAt(BlockType blockType) {
    return switch (this) {
      case BREAK -> blockType.isLoop() || blockType == BlockType.SWITCH_CASE;
      case CONTINUE -> blockType.isLoop();
      case YIELD -> blockType == BlockType.SWITCH_CASE; // TODO: new type SWITCH_EXPRESSION_ARROW_CASE?
      case RETURN -> blockType == BlockType.METHOD; // TODO: new block type LAMBDA
      case THROW -> blockType == BlockType.METHOD; // TODO: new block type TRY
    };
  }
}
