package tool.model;

public enum JumpStatement {
  BREAK, CONTINUE, RETURN, YIELD, THROW;

  public boolean propagateUntilLoop() {
    return this == BREAK || this == CONTINUE;
  }
}
