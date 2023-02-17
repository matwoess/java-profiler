package profiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class __Counter {
  private static int[] blockCounts;

  public static synchronized void inc(int n) {
    blockCounts[n]++;
  }

  public static synchronized void init(String fileName) {
    try {
      String fileContent = Files.readString(Path.of(fileName));
      // number of blocks is the first value of the metadata file
      String nBlockString = fileContent.substring(0, fileContent.indexOf(" "));
      int nBlocks = Integer.parseInt(nBlockString);
      blockCounts = new int[nBlocks];
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
  }

  public static synchronized void save(String fileName) {
    Path countsFile = Path.of(fileName);
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < blockCounts.length; i++) {
      int block = blockCounts[i];
      builder.append(block).append(" ");
    }
    try {
      Files.writeString(countsFile, builder.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
