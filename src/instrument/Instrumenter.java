package instrument;

import misc.CodeInsert;
import misc.Util;
import model.Class;
import model.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static misc.Constants.auxiliaryInstrumentDir;
import static misc.Constants.metadataFile;

public class Instrumenter {
  JavaFile[] javaFiles;
  int blockCounter;

  public Instrumenter(JavaFile... javaFiles) {
    assert javaFiles.length > 0;
    this.javaFiles = javaFiles;
  }

  public void analyzeFiles() {
    for (JavaFile additionalFile : javaFiles) {
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
    javaFile.beginOfImports = parser.state.beginOfImports;
    javaFile.foundBlocks = parser.state.allBlocks;
    javaFile.foundClasses = parser.state.allClasses;
  }

  public void instrumentFiles() {
    blockCounter = 0;
    try {
      for (JavaFile javaFile : javaFiles) {
        instrument(javaFile);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    copyAuxiliaryFiles();
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
    javaFile.instrumentedFile.getParent().toFile().mkdirs(); // make sure parent directory exists
    Files.writeString(javaFile.instrumentedFile, builder.toString());
  }

  List<CodeInsert> getCodeInserts(JavaFile javaFile) {
    List<CodeInsert> inserts = new ArrayList<>();
    inserts.add(new CodeInsert(javaFile.beginOfImports, "import auxiliary.__Counter;"));
    for (Block block : javaFile.foundBlocks) {
      if (block.blockType.isNotYetSupported()) {
        blockCounter++;
        continue; // not yet supported
      }
      // insert order is important, in case of same CodeInsert char positions
      if (block.blockType.hasNoBraces()) {
        assert block.blockType != BlockType.METHOD;
        inserts.add(new CodeInsert(block.begPos, "{"));
      }
      int counterIncrementPosition = block.begPos;
      if (block.blockType == BlockType.CONSTRUCTOR) {
        if (block.endOfSuperCall != 0) {
          counterIncrementPosition = block.endOfSuperCall;
        }
      }
      inserts.add(new CodeInsert(counterIncrementPosition, String.format("__Counter.inc(%d);", blockCounter++)));
      if (block.blockType.hasNoBraces()) {
        inserts.add(new CodeInsert(block.endPos, "}"));
      }
    }
    inserts.sort(Comparator.comparing(CodeInsert::chPos));
    return inserts;
  }

  public void exportBlockData() {
    StringBuilder builder = new StringBuilder();
    builder.append(blockCounter).append(" ");
    for (JavaFile jFile : javaFiles) {
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

  void copyAuxiliaryFiles() {
    String counterClass = "__Counter.class";
    Util.copyResource("/auxiliary/" + counterClass, auxiliaryInstrumentDir.resolve(Path.of(counterClass)));
  }

}