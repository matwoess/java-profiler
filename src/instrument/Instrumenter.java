package instrument;

import common.*;
import common.Class;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static common.Constants.*;

public class Instrumenter {
  JavaFile mainJavaFile;
  JavaFile[] additionalJavaFiles;
  int blockCounter;

  public Instrumenter(JavaFile mainJavaFile, JavaFile... additionalJavaFiles) {
    this.mainJavaFile = mainJavaFile;
    this.additionalJavaFiles = additionalJavaFiles;
  }

  public void analyzeFiles() {
    analyze(mainJavaFile);
    for (JavaFile additionalFile : additionalJavaFiles) {
      analyze(additionalFile);
    }
  }

  void analyze(JavaFile javaFile) {
    System.out.println("Reading File: \"" + javaFile.sourceFile + "\"");
    Parser parser = new Parser(new Scanner(javaFile.sourceFile.toString()));
    parser.Parse();
    System.out.println();
    int errors = parser.errors.count;
    System.out.println("Errors found: " + errors);
    if (errors > 0) {
      throw new RuntimeException("Abort due to parse errors.");
    }
    javaFile.beginOfImports = parser.beginOfImports;
    javaFile.foundBlocks = parser.allBlocks;
    javaFile.foundClasses = parser.allClasses;
  }

  public void instrumentFiles() {
    blockCounter = 0;
    try {
       instrument(mainJavaFile);
      for (JavaFile additionalFile : additionalJavaFiles) {
        instrument(additionalFile);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void instrument(JavaFile javaFile) throws IOException {
    List<CodeInsert> codeInserts = getCodeInserts(javaFile);
    String fileContent = Files.readString(javaFile.sourceFile);
    StringBuilder builder = new StringBuilder();
    int prevIdx = 0;
    for (CodeInsert codeInsert : codeInserts) {
      builder.append(fileContent, prevIdx, codeInsert.chPos);
      prevIdx = codeInsert.chPos;
      builder.append(codeInsert.code);
    }
    builder.append(fileContent.substring(prevIdx));
    javaFile.instrumentedFile.getParent().toFile().mkdirs(); // make sure parent directory exists
    Files.writeString(javaFile.instrumentedFile, builder.toString());
  }

  static class CodeInsert {
    int chPos;
    String code;

    public CodeInsert(int chPos, String code) {
      this.chPos = chPos;
      this.code = code;
    }
  }

  List<CodeInsert> getCodeInserts(JavaFile javaFile) {
    List<CodeInsert> inserts = new ArrayList<>();
    inserts.add(new CodeInsert(javaFile.beginOfImports, "import auxiliary.__Counter;"));
    for (Block block : javaFile.foundBlocks) {
      // insert order is important, in case of same CodeInsert char positions
      if (block.blockType.hasNoBraces()) {
        assert block.blockType != BlockType.METHOD;
        inserts.add(new CodeInsert(block.begPos, "{"));
      }
      inserts.add(new CodeInsert(block.begPos, String.format("__Counter.inc(%d);", blockCounter++)));
      if (block.blockType == BlockType.SS_SWITCH_EXPR_CASE) {
        inserts.add(new CodeInsert(block.begPos, "yield "));
      }
      if (block.blockType.hasNoBraces()) {
        inserts.add(new CodeInsert(block.endPos, "}"));
      }
    }
    inserts.sort(Comparator.comparing(insert -> insert.chPos));
    return inserts;
  }

  public void exportBlockData() {
    StringBuilder builder = new StringBuilder();
    builder.append(blockCounter).append(" ");
    for (JavaFile jFile : additionalJavaFiles) {
      builder.append(jFile.sourceFile.toUri()).append(" ");
      for (Class clazz : jFile.foundClasses) {
        builder.append(clazz.name).append(" ");
        for (Method meth : clazz.methods) {
          builder.append(meth.name).append(" ");
          for (Block block : meth.blocks) {
            builder.append(block.beg).append(" ");
            builder.append(block.end).append(" ");
            builder.append(block.blockType.ordinal()).append(" ");
          }
        }
        builder.append("#").append(" ");
      }
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