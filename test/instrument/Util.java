package instrument;

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

  public static List<Parser.Block> getFoundBlocks(String content) {
    Path file = createTempFileWithContent(content);
    return Instrumenter.analyzeFile(file.toString());
  }

  public static Parser.Block getExpectedBlock(String clazz, String meth, int beg, int end, boolean isMethBlock) {
    Parser.Block expected = new Parser.Block();
    expected.clazz = clazz;
    expected.method = meth;
    expected.beg = beg;
    expected.end = end;
    expected.isMethodBlock = isMethBlock;
    return expected;
  }
}
