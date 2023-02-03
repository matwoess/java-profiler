package instrument;

import static java.lang.System.exit;

public class Instrumenter {

  public static void main(String[] args) {
    String[] inputFiles = new String[]{
        "TestProgram.java",
    };
    for (String inputFile : inputFiles) {
      instrumentFile(inputFile);
    }
  }

  private static void instrumentFile(String inputFile) {
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
  }
}