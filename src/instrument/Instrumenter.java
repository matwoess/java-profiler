package instrument;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Stack;

import static java.lang.System.exit;

public class Instrumenter {
  List<Parser.Class> foundClasses;
  List<Parser.Block> foundBlocks;
  Path sourceFile;
  Path instrumentedFile;
  Path metadataFile;

  public Instrumenter(Path sourceFile) {
    this.sourceFile = sourceFile;
    Path profilerRoot = Path.of("out/profiler");
    profilerRoot.toFile().mkdirs();
    instrumentedFile = profilerRoot.resolve(sourceFile.getFileName());
    metadataFile = profilerRoot.resolve(sourceFile.getFileName().toString() + ".meta");
  }

  public void analyzeFile() {
    System.out.println("Reading File: \"" + sourceFile + "\"");
    Parser parser = new Parser(new Scanner(sourceFile.toString()));
    parser.Parse();
    System.out.println();
    int errors = parser.errors.count;
    System.out.println("Errors found: " + errors);
    if (errors > 0) {
      System.out.println("aborting...");
      exit(1);
    }
    foundBlocks = parser.allBlocks;
    foundClasses = parser.classes;
  }

  public List<Parser.Class> getFoundClasses() {
    return foundClasses;
  }

  public List<Parser.Block> getFoundBlocks() {
    return foundBlocks;
  }

  public Path instrument() {
//    try {
//      Files.copy(sourceFile, targetPath, REPLACE_EXISTING);
//    } catch (IOException ex) {
//      System.out.println(ex);
//    }
    try {
      String fileContent = Files.readString(sourceFile);
      StringBuilder builder = new StringBuilder();
      int prevIdx = 0;
      Stack<Parser.Block> blockStack = new Stack<>();
      for (int i = 0; i < foundBlocks.size(); i++) {
        Parser.Block block = foundBlocks.get(i);
        if (!blockStack.empty() && blockStack.peek().endPos < block.begPos) {
          Parser.Block prevBlock = blockStack.pop();
          builder.append(fileContent, prevIdx, prevBlock.endPos);
          prevIdx = prevBlock.endPos;
          if (prevBlock.insertBraces) {
            assert !prevBlock.isMethodBlock;
            builder.append('}');
          }
        }
        blockStack.push(block);
        builder.append(fileContent, prevIdx, block.begPos);
        prevIdx = block.begPos;
        if (block.insertBraces) {
          builder.append('{');
        }
        builder.append(String.format("__Counter.inc(%d);", i));
      }
      while (!blockStack.empty()) {
        Parser.Block prevBlock = blockStack.pop();
        if (prevBlock.endPos > prevIdx) {
          builder.append(fileContent, prevIdx, prevBlock.endPos);
          prevIdx = prevBlock.endPos;
        }
        if (prevBlock.insertBraces) {
          assert !prevBlock.isMethodBlock;
          builder.append('}');
        }
      }
      builder.append(fileContent.substring(prevIdx));
      Files.writeString(instrumentedFile, builder.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return metadataFile;
  }

  public void exportBlockData() {
    StringBuilder builder = new StringBuilder();
    builder.append(foundBlocks.size()).append(" ");
    for (Parser.Class clazz : foundClasses) {
      builder.append(clazz.name).append(" ");
      for (Parser.Method meth : clazz.methods) {
        builder.append(meth.name).append(" ");
        for (Parser.Block block : meth.blocks) {
          builder.append(block.beg).append(" ");
          builder.append(block.end).append(" ");
          builder.append(block.isMethodBlock ? 1 : 0).append(" ");
        }
      }
      builder.append("#").append(" ");
    }
    try (FileOutputStream fos = new FileOutputStream(metadataFile.toFile())) {
      byte[] data = builder.toString().getBytes();
      fos.write(data, 0, data.length);
      fos.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}