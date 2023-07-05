package instrument;

import misc.CodeInsert;
import misc.IO;
import model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Instrumenter {
  boolean syncCounters;
  JavaFile[] javaFiles;
  int blockCounter;
  String incRefAdd = "";

  public Instrumenter(boolean syncCounters, JavaFile... javaFiles) {
    assert javaFiles.length > 0;
    this.syncCounters = syncCounters;
    if (syncCounters) {
      incRefAdd = "Sync";
    }
    this.javaFiles = javaFiles;
  }

  public void analyzeFiles() {
    for (JavaFile javaFile : javaFiles) {
      analyze(javaFile);
    }
  }

  void analyze(JavaFile javaFile) {
    System.out.println("Reading File: \"" + javaFile.sourceFile + "\"");
    Parser parser = new Parser(new Scanner(javaFile.sourceFile.toString()));
    parser.Parse();
    int errors = parser.errors.count;
    System.out.printf("Errors found: %d\n\n", errors);
    if (errors > 0) {
      throw new RuntimeException("Abort due to parse errors.");
    }
    javaFile.packageName = parser.state.packageName;
    javaFile.beginOfImports = parser.state.beginOfImports;
    javaFile.foundBlocks = parser.state.allBlocks;
    javaFile.topLevelClasses = parser.state.topLevelClasses;
  }

  public void instrumentFiles() {
    IO.clearInstrumentDirIfExists();
    blockCounter = 0;
    try {
      for (JavaFile javaFile : javaFiles) {
        instrument(javaFile);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    IO.copyAuxiliaryFiles();
    System.out.println();
    System.out.println("Total block found: " + blockCounter);
  }

  void instrument(JavaFile javaFile) throws IOException {
    List<CodeInsert> codeInserts = getCodeInserts(javaFile);
    String fileContent = Files.readString(javaFile.sourceFile, StandardCharsets.ISO_8859_1);
    StringBuilder builder = new StringBuilder();
    int prevIdx = 0;
    for (CodeInsert codeInsert : codeInserts) {
      builder.append(fileContent, prevIdx, codeInsert.chPos());
      prevIdx = codeInsert.chPos();
      builder.append(codeInsert.code());
    }
    builder.append(fileContent.substring(prevIdx));
    IO.createDirectoriesIfNotExists(javaFile.instrumentedFile);
    Files.writeString(javaFile.instrumentedFile, builder.toString());
  }

  List<CodeInsert> getCodeInserts(JavaFile javaFile) {
    List<CodeInsert> inserts = new ArrayList<>();
    inserts.add(new CodeInsert(javaFile.beginOfImports, "import auxiliary.__Counter;"));
    for (Block block : javaFile.foundBlocks) {
      // insert order is important, in case of same CodeInsert char positions
      if (block.blockType.hasNoBraces() && block.blockType != BlockType.SS_LAMBDA) {
        assert block.blockType != BlockType.METHOD;
        inserts.add(new CodeInsert(block.begPos, "{"));
      }
      if (block.blockType == BlockType.SS_LAMBDA) {
        inserts.add(new CodeInsert(block.getIncInsertPos(), String.format("__Counter.incLambda%s(%d, () -> ", incRefAdd, blockCounter++)));
        inserts.add(new CodeInsert(block.endPos, ")"));
      } else {
        inserts.add(new CodeInsert(block.getIncInsertPos(), String.format("__Counter.inc%s(%d);", incRefAdd, blockCounter++)));
      }
      if (block.blockType == BlockType.SS_SWITCH_EXPR_ARROW_CASE && !block.startsWithThrow) {
        inserts.add(new CodeInsert(block.getIncInsertPos(), "yield "));
      }
      if (block.blockType.hasNoBraces() && block.blockType != BlockType.SS_LAMBDA) {
        inserts.add(new CodeInsert(block.endPos, "}"));
      }
    }
    inserts.sort(Comparator.comparing(CodeInsert::chPos));
    return inserts;
  }

  public void exportMetadata() {
    IO.exportMetadata(new IO.Metadata(blockCounter, javaFiles));
  }

}