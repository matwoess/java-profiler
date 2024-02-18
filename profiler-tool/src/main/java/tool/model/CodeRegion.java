package tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for a group of statements, inside a block, sharing one hit-count value.
 * <p>
 * Used to correctly determine the hit-count for a line of code and for visualization in the report.
 * <p>
 * A list of dependent jump blocks is used to correctly calculate the effective hit-count of the current region.
 * When a block is reentered after a jump statement block, we need to subtract the hits of this block
 * to calculate the real count of the new region.
 * This is because we do not have an actual counter after <code>break</code>s,
 * <code>continue</code>s and <code>return</code>s, etc.
 */
public class CodeRegion implements Serializable, Component {
  public int id;
  public CodePosition beg;
  public CodePosition end;
  /**
   * The block this region belongs to.
   */
  public Block block;
  /**
   * The list of dependent jump blocks, used to calculate the effective hit-count of the current region.
   */
  public final List<Block> dependantJumps = new ArrayList<>();

  /**
   * Gets the effective hit-count of this region.
   * @return the parent blocks hits minus the sum of hits form all dependent jump blocks
   */
  public long getHitCount() {
    return block.hits - dependantJumps.stream().mapToLong(b -> b.hits).sum();
  }

  /**
   * Checks whether this region is active in a given line.
   * @param lineNr the line number to check
   * @return true if the given line is between the start and end line of this region
   */
  public boolean isActiveInLine(int lineNr) {
    return beg.line() <= lineNr && end.line() >= lineNr;
  }

  @Override
  public String toString() {
    return String.format(
        "CodeRegion (beg=%s, end=%s, minusBlocks=%s)",
        beg, end, Arrays.toString(dependantJumps.toArray())
    );
  }
}
