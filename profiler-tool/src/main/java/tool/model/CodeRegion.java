package tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeRegion implements Serializable {
  public CodePosition beg;
  public CodePosition end;
  public Block block;
  public List<Block> minusBlocks = new ArrayList<>();

  public int getHitCount() {
    return block.hits - minusBlocks.stream().mapToInt(b -> b.hits).sum();
  }

  @Override
  public String toString() {
    return String.format(
        "CodeRegion (beg=%s, end=%s, minusBlocks=%s)",
        beg, end, Arrays.toString(minusBlocks.toArray())
    );
  }
}
