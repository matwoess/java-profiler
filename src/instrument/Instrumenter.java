package instrument;

import common.JavaFile;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static common.Constants.*;
import static java.lang.System.exit;

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
    javaFile.foundClasses = parser.classes;
  }

  public void instrumentFiles() {
    blockCounter = 0;
    try {
      instrument(mainJavaFile, true);
      for (JavaFile additionalFile : additionalJavaFiles) {
        instrument(additionalFile, false);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void instrument(JavaFile javaFile, boolean isMain) throws IOException {
    List<CodeInsert> codeInserts = getCodeInserts(javaFile, isMain);
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

  List<CodeInsert> getCodeInserts(JavaFile javaFile, boolean isMainFile) {
    boolean initAndSafeInserted = false;
    List<CodeInsert> inserts = new ArrayList<>();
    inserts.add(new CodeInsert(javaFile.beginOfImports, "import auxiliary.__Counter;"));
    for (Parser.Block block : javaFile.foundBlocks) {
      // insert order is important, in case of same CodeInsert char positions
      if (!initAndSafeInserted && isMainFile && isCounterInitBlock(block)) {
        String initCode = String.format("__Counter.init(\"%s\");", instrumentDir.relativize(metadataFile));
        String saveCode = String.format(
            "Runtime.getRuntime().addShutdownHook(new Thread(() -> {__Counter.save((\"%s\"));}));;",
            instrumentDir.relativize(countsFile)
        );
        inserts.add(new CodeInsert(block.begPos, initCode));
        inserts.add(new CodeInsert(block.begPos, saveCode));
        initAndSafeInserted = true;
      }
      if (block.insertBraces) {
        assert !block.isMethodBlock;
        inserts.add(new CodeInsert(block.begPos, "{"));
      }
      inserts.add(new CodeInsert(block.begPos, String.format("__Counter.inc(%d);", blockCounter++)));
      if (block.insertBraces) {
        inserts.add(new CodeInsert(block.endPos, "}"));
      }
    }
    inserts.sort(Comparator.comparing(insert -> insert.chPos));
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
    builder.append(blockCounter).append(" ");
    for (JavaFile jFile : additionalJavaFiles) {
      builder.append(jFile.sourceFile.toUri()).append(" ");
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