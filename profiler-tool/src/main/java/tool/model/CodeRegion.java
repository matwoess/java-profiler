package tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CodeRegion implements Serializable {
  public CodePosition beg;
  public CodePosition end;
  public List<Block> minusBlocks = new ArrayList<>();
}
