package instrument;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Stack;

import static java.lang.System.exit;

public class Instrumenter {
  List<Parser.Block> foundBlocks;
  Path sourceFile;

  public void analyzeFile(String inputFile) {
    System.out.println("Reading File: \"" + inputFile + "\"");
    Parser parser = new Parser(new Scanner(inputFile));
    sourceFile = Path.of(inputFile);
    parser.Parse();
    System.out.println();
    int errors = parser.errors.count;
    System.out.println("Errors found: " + errors);
    if (errors > 0) {
      System.out.println("aborting...");
      exit(1);
    }
    foundBlocks = parser.allBlocks;
  }

  public List<Parser.Block> getFoundBlocks() {
    return foundBlocks;
  }

  public Path instrument() {
    Path targetPath = Path.of("out/instrumented.java");
//    try {
//      Files.copy(sourceFile, targetPath, REPLACE_EXISTING);
//    } catch (IOException ex) {
//      System.out.println(ex);
//    }
    try {
      String fileContent = Files.readString(sourceFile);
      System.out.println(fileContent);
      StringBuilder builder = new StringBuilder();
      int prevIdx = 0;
      Stack<Parser.Block> blockStack = new Stack<>();
      for (Parser.Block block : foundBlocks) {
        if (!blockStack.empty() && blockStack.peek().endPos < block.begPos) {
          Parser.Block prevBlock = blockStack.pop();
          builder.append(fileContent.substring(prevIdx, prevBlock.endPos));
          prevIdx = prevBlock.endPos;
          if (prevBlock.insertBraces) {
            assert !prevBlock.isMethodBlock;
            builder.append('}');
          }
        }
        blockStack.push(block);
        builder.append(fileContent.substring(prevIdx, block.begPos));
        prevIdx = block.begPos;
        if (block.insertBraces) builder.append('{');
        builder.append(String.format("__Counter.inc(%d);", block.beg));
      }
      while (!blockStack.empty()) {
        Parser.Block prevBlock = blockStack.pop();
        if (prevBlock.endPos > prevIdx) {
          builder.append(fileContent.substring(prevIdx, prevBlock.endPos));
          prevIdx = prevBlock.endPos;
        }
        if (prevBlock.insertBraces) {
          assert !prevBlock.isMethodBlock;
          builder.append('}');
        }
      }
      builder.append(fileContent.substring(prevIdx));
      Files.writeString(targetPath, builder.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return targetPath;
  }
}