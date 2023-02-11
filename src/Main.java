import instrument.Instrumenter;
import instrument.Parser;

import java.util.List;

public class Main {

  public static void main(String[] args) {
    String[] inputFiles = new String[]{
         "sample/Classes.java",
    };
    for (String inputFile : inputFiles) {
      Instrumenter instrumenter = new Instrumenter();
      instrumenter.analyzeFile(inputFile);
      List<Parser.Block> blocks = instrumenter.getFoundBlocks();
      instrumenter.instrument();
      instrumenter.exportBlockData();
      System.out.println("\n\nFound blocks:");
      blocks.forEach(System.out::println);
    }
  }
}
