package instrument;

import common.*;
import common.Class;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Util {
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
    return instrumenter.mainJavaFile.foundBlocks;
  }

  public static int getBeginOfImports(String content) {
    Instrumenter instrumenter = analyzeStringContent(content);
    return instrumenter.mainJavaFile.beginOfImports;
  }

  public static Block getBlock(Class clazz, Method meth, int beg, int end, int begPos, int endPos) {
    Block expected = new Block();
    expected.clazz = clazz;
    expected.method = meth;
    expected.beg = beg;
    expected.begPos = begPos;
    expected.end = end;
    expected.endPos = endPos;
    expected.blockType = BlockType.BLOCK;
    return expected;
  }

  public static Block getMethodBlock(Class clazz, Method meth, int beg, int end, int begPos, int endPos) {
    Block expected = getBlock(clazz, meth, beg, end, begPos, endPos);
    expected.blockType = BlockType.METHOD;
    return expected;
  }

  public static Block getStaticBlock(Class clazz, int beg, int end, int begPos, int endPos) {
    Block expected = getBlock(clazz, null, beg, end, begPos, endPos);
    expected.blockType = BlockType.STATIC;
    return expected;
  }

  public static Block getSingleStatementBlock(Class clazz, Method meth, int beg, int end, int begPos, int endPos) {
    Block expected = getBlock(clazz, meth, beg, end, begPos, endPos);
    expected.blockType = BlockType.SS_BLOCK;
    return expected;
  }

  public static Block getLambdaSSBlock(Class clazz, Method meth, int beg, int end, int begPos, int endPos) {
    Block expected = getBlock(clazz, meth, beg, end, begPos, endPos);
    expected.blockType = BlockType.SS_LAMBDA;
    return expected;
  }

  public static Block getSwitchExprSSCase(Class clazz, Method meth, int beg, int end, int begPos, int endPos) {
    Block expected = getBlock(clazz, meth, beg, end, begPos, endPos);
    expected.blockType = BlockType.SS_SWITCH_EXPR_CASE;
    return expected;
  }
}
