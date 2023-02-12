import instrument.Instrumenter;
import instrument.Parser;

import java.nio.file.Path;
import java.util.List;

public class Main {

  public static void main(String[] args) {
    String[] inputFiles = new String[]{
         "sample/Classes.java",
    };
    for (String inputFile : inputFiles) {
      Path inputFilePath = Path.of(inputFile);
      Instrumenter instrumenter = new Instrumenter(inputFilePath);
      instrumenter.analyzeFile();
      List<Parser.Block> blocks = instrumenter.getFoundBlocks();
      System.out.println("\n\nFound blocks:");
      blocks.forEach(System.out::println);
      instrumenter.instrument();
      instrumenter.exportBlockData();
    }
  }
}
