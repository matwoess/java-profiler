package tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeRegion implements Serializable, Component {
  public int id;
  public CodePosition beg;
  public CodePosition end;
  public Block block;
  public List<Block> minusBlocks = new ArrayList<>();

  public int getHitCount() {
    return block.hits - minusBlocks.stream().mapToInt(b -> b.hits).sum();
  }

  public boolean isActiveInLine(int lineNr) {
    return beg.line() <= lineNr && end.line() >= lineNr;
  }

  @Override
  public String toString() {
    return String.format(
        "CodeRegion (beg=%s, end=%s, minusBlocks=%s)",
        beg, end, Arrays.toString(minusBlocks.toArray())
    );
  }
}
