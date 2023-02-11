package instrument;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static java.lang.System.exit;

public class Instrumenter {
  List<Parser.Block> foundBlocks;
  Path sourceFile;
  Path targetFile = Path.of("out/profiler/Classes.java");;

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
//    try {
//      Files.copy(sourceFile, targetPath, REPLACE_EXISTING);
//    } catch (IOException ex) {
//      System.out.println(ex);
//    }
    try {
      Path.of("out/profiler").toFile().mkdirs();
      String fileContent = Files.readString(sourceFile);
      System.out.println(fileContent);
      StringBuilder builder = new StringBuilder();
      int prevIdx = 0;
      Stack<Parser.Block> blockStack = new Stack<>();
      for (int i = 0; i < foundBlocks.size(); i++) {
        Parser.Block block = foundBlocks.get(i);
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
        builder.append(String.format("__Counter.inc(%d);", i));
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
      Files.writeString(targetFile, builder.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return targetFile;
  }

  public void exportBlockData() {
    List<Class> classes = new ArrayList<>();
    for (Parser.Block block : foundBlocks) {
      Class clazz;
      if (classes.isEmpty() || !classes.get(classes.size() - 1).name.equals(block.clazz)) {
        clazz = new Class();
        clazz.name = block.clazz;
        classes.add(clazz);
      } else {
        clazz = classes.get(classes.size()-1);
      }
      Method meth;
      if (clazz.methods.isEmpty() || !clazz.methods.get(clazz.methods.size() - 1).name.equals(block.method)) {
        meth = new Method();
        meth.name = block.method;
        clazz.methods.add(meth);
      } else {
        meth = clazz.methods.get(classes.size()-1);
      }
      Block newBlock = new Block();
      newBlock.beg = block.begPos;
      newBlock.end = block.endPos;
      newBlock.isMethodBlock = block.isMethodBlock;
      meth.blocks.add(newBlock);
    }
    StringBuilder builder = new StringBuilder();
    builder.append(foundBlocks.size()).append(" ");
    for (Class clazz : classes) {
      builder.append(clazz.name).append(" ");
      for (Method meth: clazz.methods) {
        builder.append(meth.name).append(" ");
        for (Block block : meth.blocks) {
          builder.append(block.beg).append(" ");
          builder.append(block.end).append(" ");
          builder.append(block.isMethodBlock ? 1 : 0).append(" ");
        }
      }
      builder.append("#").append(" ");
    }
    Path metaDataFile = Path.of(targetFile.toString() + ".meta");
    try (FileOutputStream fos = new FileOutputStream(metaDataFile.toFile())) {
      byte[] data = builder.toString().getBytes();
      fos.write(data, 0, data.length);
      fos.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  class Class {
    String name;
    List<Method> methods = new ArrayList<>();
  }
  class Method {
    String name;
    List<Block> blocks = new ArrayList<>();
  }
  class Block {
    int beg, end;
    boolean isMethodBlock;
  }
}