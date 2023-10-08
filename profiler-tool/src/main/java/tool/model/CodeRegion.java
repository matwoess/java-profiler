package tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeRegion implements Serializable {
  public CodePosition beg;
  public CodePosition end;
  public List<Block> minusBlocks = new ArrayList<>();

  @Override
  public String toString() {
    return String.format(
        "CodeRegion (beg=%s, end=%s, minusBlocks=%s)",
        beg, end, Arrays.toString(minusBlocks.toArray())
    );
  }
}
