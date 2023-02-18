package instrument;

import common.JavaFile;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static java.lang.System.exit;

public class Instrumenter {
  JavaFile[] javaFiles;

  public Instrumenter(JavaFile... javaFiles) {
    this.javaFiles = javaFiles;
  }

  public void analyzeFiles() {
    for (JavaFile jFile : javaFiles) {
      System.out.println("Reading File: \"" + jFile.sourceFile + "\"");
      Parser parser = new Parser(new Scanner(jFile.sourceFile.toString()));
      parser.Parse();
      System.out.println();
      int errors = parser.errors.count;
      System.out.println("Errors found: " + errors);
      if (errors > 0) {
        System.out.println("aborting...");
        exit(1);
      }
      jFile.foundBlocks = parser.allBlocks;
      jFile.foundClasses = parser.classes;
    }
  }

  public void instrument() {
    try {
      Boolean initAndSafeInserted = false;
      for (JavaFile jFile : javaFiles) {
        List<CodeInsert> codeInserts = getCodeInserts(jFile, initAndSafeInserted);
        String fileContent = Files.readString(jFile.sourceFile);
        StringBuilder builder = new StringBuilder();
        int prevIdx = 0;
        for (CodeInsert codeInsert : codeInserts) {
          builder.append(fileContent, prevIdx, codeInsert.chPos);
          prevIdx = codeInsert.chPos;
          builder.append(codeInsert.code);
        }
        builder.append(fileContent.substring(prevIdx));
        Files.writeString(jFile.instrumentedFile, builder.toString());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static class CodeInsert {
    int chPos;
    String code;

    public CodeInsert(int chPos, String code) {
      this.chPos = chPos;
      this.code = code;
    }
  }

  List<CodeInsert> getCodeInserts(JavaFile javaFile, Boolean initAndSafeInserted) {
    List<CodeInsert> inserts = new ArrayList<>();
    inserts.add(new CodeInsert(0, "import profile.__Counter;"));
    for (int i = 0; i < javaFile.foundBlocks.size(); i++) {
      Parser.Block block = javaFile.foundBlocks.get(i);
      // insert order is important, because of same CodeInsert char positions
      if (!initAndSafeInserted && isCounterInitBlock(block)) {
        String initCode = String.format("__Counter.init(\"%s\");", javaFile.metadataFile.getFileName());
        String saveCode = String.format(
            "Runtime.getRuntime().addShutdownHook(new Thread(() -> {__Counter.save((\"%s\"));}));;",
            javaFile.resultsFile.getFileName()
        );
        inserts.add(new CodeInsert(block.begPos, initCode));
        inserts.add(new CodeInsert(block.begPos, saveCode));
        initAndSafeInserted = true;
      }
      if (block.insertBraces) {
        assert !block.isMethodBlock;
        inserts.add(new CodeInsert(block.begPos, "{"));
      }
      inserts.add(new CodeInsert(block.begPos, String.format("__Counter.inc(%d);", i)));
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
    for (JavaFile jFile : javaFiles) {
      StringBuilder builder = new StringBuilder();
      builder.append(jFile.foundBlocks.size()).append(" ");
      for (Parser.Class clazz : jFile.foundClasses) {
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
      try (FileOutputStream fos = new FileOutputStream(jFile.metadataFile.toFile())) {
        byte[] data = builder.toString().getBytes();
        fos.write(data, 0, data.length);
        fos.flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}