package instrument;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.lang.System.exit;

public class Instrumenter {
  List<Parser.Class> foundClasses;
  List<Parser.Block> foundBlocks;
  Path sourceFile;
  Path instrumentedFile;
  Path metadataFile;
  Path countsFile;

  public Instrumenter(Path sourceFile) {
    this.sourceFile = sourceFile;
    Path profilerRoot = Path.of("out/profiler");
    profilerRoot.toFile().mkdirs();
    instrumentedFile = profilerRoot.resolve(sourceFile.getFileName());
    metadataFile = profilerRoot.resolve(sourceFile.getFileName().toString() + ".meta");
    countsFile = profilerRoot.resolve(sourceFile.getFileName().toString() + ".counts");
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
    try {
      List<CodeInsert> codeInserts = getCodeInserts();
      String fileContent = Files.readString(sourceFile);
      StringBuilder builder = new StringBuilder();
      int prevIdx = 0;
      for (CodeInsert codeInsert : codeInserts) {
        builder.append(fileContent, prevIdx, codeInsert.chPos);
        prevIdx = codeInsert.chPos;
        builder.append(codeInsert.code);
      }
      builder.append(fileContent.substring(prevIdx));
      Files.writeString(instrumentedFile, builder.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return metadataFile;
  }

  static class CodeInsert {
    int chPos;
    String code;

    public CodeInsert(int chPos, String code) {
      this.chPos = chPos;
      this.code = code;
    }
  }

  List<CodeInsert> getCodeInserts() {
    List<CodeInsert> inserts = new ArrayList<>();
    boolean initInserted = false, saveInserted = false;
    for (int i = 0; i < foundBlocks.size(); i++) {
      Parser.Block block = foundBlocks.get(i);
      // insert order is important, because of same CodeInsert char positions
      if (!initInserted && isCounterInitBlock(block)) {
        inserts.add(new CodeInsert(block.begPos, String.format("__Counter.init(\"%s\");", metadataFile.toString())));
        initInserted = true;
      }
      if (block.insertBraces) {
        assert !block.isMethodBlock;
        inserts.add(new CodeInsert(block.begPos, "{"));
      }
      inserts.add(new CodeInsert(block.begPos, String.format("__Counter.inc(%d);", i)));
      if (!saveInserted && block.isMethodBlock && block.method.isMain) {
        inserts.add(new CodeInsert(block.endPos - 1, String.format("__Counter.save(\"%s\");", countsFile.toString())));
        saveInserted = true;
      }
      if (block.insertBraces) {
        inserts.add(new CodeInsert(block.endPos, "}"));
      }
    }
    inserts.sort(Comparator.comparing(i -> i.chPos));
    return inserts;
  }

  boolean isCounterInitBlock(Parser.Block block) {
    if (!block.clazz.isMain) return false;
    boolean classHasStaticBlock = block.clazz.methods.stream().anyMatch(m -> m.name.equals("static"));
    if (classHasStaticBlock) {
      return block.method.name.equals("static");
    }
    return block.isMethodBlock && block.method.isMain;
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