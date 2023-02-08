package instrument;

import java.util.List;

import static java.lang.System.exit;

public class Instrumenter {
  public List<Parser.Block> analyzeFile(String inputFile) {
    System.out.println("Reading File: \"" + inputFile + "\"");
    Parser parser = new Parser(new Scanner(inputFile));
    parser.Parse();
    System.out.println();
    int errors = parser.errors.count;
    System.out.println("Errors found: " + errors);
    if (errors > 0) {
      System.out.println("aborting...");
      exit(1);
    }
    return parser.allBlocks;
  }
}