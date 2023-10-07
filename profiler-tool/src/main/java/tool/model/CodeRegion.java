package tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeRegion implements Serializable {
  public int begPos;
  public int endPos;
  public List<Block> minusBlocks = new ArrayList<>();

  @Override
  public String toString() {
    return String.format(
        "CodeRegion (begPos=%s, endPos=%s, minusBlocks=%s)",
        begPos, endPos, Arrays.toString(minusBlocks.toArray())
    );
  }
}
