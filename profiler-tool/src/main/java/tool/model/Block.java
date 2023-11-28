package tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Block implements Serializable, Component {
  public int id;
  public JClass clazz;
  public Method method;
  public Block parentBlock;
  public CodePosition beg;
  public CodePosition end;
  public BlockType blockType;
  public boolean isSingleStatement;
  public List<Block> innerBlocks = new ArrayList<>();

  public JumpStatement jumpStatement = null;
  public List<String> labels = new ArrayList<>();
  public List<CodeRegion> codeRegions = new ArrayList<>();

  public int incInsertOffset;

  transient public int hits;
  public transient List<Block> innerJumpBlocks = new ArrayList<>();

  public Block(BlockType type) {
    blockType = type;
  }

  public void setParentMethod(Method method) {
    assert this.method == null;
    if (method != null) {
      this.method = method;
      if (blockType.isMethod()) {
        method.setMethodBlock(this);
      }
    }
  }

  public void setParentClass(JClass clazz) {
    assert this.clazz == null;
    this.clazz = clazz;
    if (method == null) {
      clazz.classBlocks.add(this);
    }
  }

  public void setParentBlock(Block block) {
    if (block == null) return;
    parentBlock = block;
    parentBlock.innerBlocks.add(this);
  }

  public void registerInnerJumpBlock(Block jumpBlock) {
    innerJumpBlocks.add(jumpBlock);
  }

  public void addCodeRegion(CodeRegion region) {
    region.id = codeRegions.size();
    codeRegions.add(region);
  }

  public List<Block> getInnerBlocksRecursive() {
    List<Block> blocks = new ArrayList<>();
    for (Block b : innerBlocks) {
      blocks.add(b);
      blocks.addAll(b.getInnerBlocksRecursive());
    }
    return blocks;
  }

  public boolean isSwitchStatementCase() {
    return blockType.isSwitchCase()
        && parentBlock != null
        && parentBlock.blockType == BlockType.SWITCH_STMT;
  }

  public boolean isSwitchExpressionCase() {
    return blockType.isSwitchCase()
        && parentBlock != null
        && parentBlock.blockType == BlockType.SWITCH_EXPR;
  }

  public int getIncInsertPos() {
    return beg.pos() + incInsertOffset;
  }

  public boolean isActiveInLine(int lineNr) {
    return beg.line() <= lineNr && end.line() >= lineNr;
  }

  public boolean hasNoBraces() {
    return isSingleStatement || blockType.hasNoBraces();
  }

  public String toString() {
    return String.format("%s%s%s: {%d[%s%s]-%s[%s]} (%s%s)%s%s",
        labels.isEmpty() ? "" : String.join(": ", labels) + ": ",
        clazz.name,
        method != null ? ("." + method.name) : "",
        beg.line(),
        beg.pos(),
        incInsertOffset != 0 ? "(+" + incInsertOffset + ")" : "",
        end != null ? end.line() : "?",
        end != null ? end.pos() : "?",
        blockType.toString(),
        isSingleStatement ? ", SS" : "",
        method == null ? " [class-level]" : "",
        jumpStatement != null ? " [" + jumpStatement + "]" : ""
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Block block = (Block) o;
    if (!Objects.equals(beg, block.beg)) return false;
    if (!Objects.equals(end, block.end)) return false;
    if (incInsertOffset != block.incInsertOffset) return false;
    if (!clazz.equals(block.clazz)) return false;
    if (!Objects.equals(method, block.method)) return false;
    if (!Objects.equals(jumpStatement, block.jumpStatement)) return false;
    return blockType == block.blockType;
  }

  @Override
  public int hashCode() {
    int result = clazz.hashCode();
    result = 31 * result + (method != null ? method.hashCode() : 0);
    result = 31 * result + beg.hashCode();
    result = 31 * result + end.hashCode();
    result = 31 * result + blockType.hashCode();
    result = 31 * result + incInsertOffset;
    result = 31 * result + (jumpStatement != null ? jumpStatement.hashCode() : 0);
    return result;
  }
}
