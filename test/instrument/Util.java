package instrument;

import model.*;
import model.Class;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Util {
  public static final String baseTemplate = """
      public class Main {
        public static void main(String[] args) {
          %s
        }
         %s
      }
      """;

  private static Path createTempFileWithContent(String content) {
    try {
      List<String> fileContent = Arrays.asList(content.split("\n"));
      Path tempFile = Files.createTempFile(null, null);
      Files.write(tempFile, fileContent);
      return tempFile;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Instrumenter analyzeStringContent(String content) {
    Path file = createTempFileWithContent(content);
    JavaFile javaFile = new JavaFile(file, file.getParent());
    Instrumenter instrumenter = new Instrumenter(javaFile);
    instrumenter.analyzeFiles();
    return instrumenter;
  }

  public static List<Block> getFoundBlocks(String content) {
    Instrumenter instrumenter = analyzeStringContent(content);
    return instrumenter.javaFiles[0].foundBlocks;
  }

  public static int getBeginOfImports(String content) {
    Instrumenter instrumenter = analyzeStringContent(content);
    return instrumenter.javaFiles[0].beginOfImports;
  }

  public static Block getBlock(BlockType blockType, Class clazz, Method meth, int beg, int end, int begPos, int endPos) {
    Block block = new Block();
    block.blockType = blockType;
    block.clazz = clazz;
    block.method = meth;
    block.beg = beg;
    block.begPos = begPos;
    block.end = end;
    block.endPos = endPos;
    return block;
  }
}
