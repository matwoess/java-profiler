package instrument;

import common.JavaFile;

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

  public static List<Parser.Block> getFoundBlocks(String content) {
    Instrumenter instrumenter = analyzeStringContent(content);
    return instrumenter.mainJavaFile.foundBlocks;
  }

  public static int getBeginOfImports(String content) {
    Instrumenter instrumenter = analyzeStringContent(content);
    return instrumenter.mainJavaFile.beginOfImports;
  }

  public static Parser.Block getBlock(Parser.Class clazz, Parser.Method meth, int beg, int end, int begPos, int endPos) {
    Parser.Block expected = new Parser.Block();
    expected.clazz = clazz;
    expected.method = meth;
    expected.beg = beg;
    expected.begPos = begPos;
    expected.end = end;
    expected.endPos = endPos;
    return expected;
  }

  public static Parser.Block getMethodBlock(Parser.Class clazz, Parser.Method meth, int beg, int end, int begPos, int endPos) {
    Parser.Block expected = getBlock(clazz, meth, beg, end, begPos, endPos);
    expected.isMethodBlock = true;
    return expected;
  }

  public static Parser.Block getSingleStatementBlock(Parser.Class clazz, Parser.Method meth, int beg, int end, int begPos, int endPos) {
    Parser.Block expected = getBlock(clazz, meth, beg, end, begPos, endPos);
    expected.insertBraces = true;
    return expected;
  }
}
