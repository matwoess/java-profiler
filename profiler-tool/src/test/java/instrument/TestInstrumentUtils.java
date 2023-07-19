package instrument;

import model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestInstrumentUtils {
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
      List<String> fileContent = Arrays.asList(content.split(misc.Util.getOS().lineSeparator()));
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
    Instrumenter instrumenter = new Instrumenter(false, true, javaFile);
    instrumenter.analyzeFiles();
    return instrumenter;
  }

  public static JavaFile parseJavaFile(String content) {
    Instrumenter instrumenter = analyzeStringContent(content);
    return instrumenter.javaFiles[0];
  }

  public static void assertResultEquals(JavaFile expected, JavaFile actual) {
    assertEquals(expected.foundBlocks.size(), actual.foundBlocks.size());
    assertEquals(expected.topLevelClasses.size(), actual.topLevelClasses.size());
    assertIterableEquals(expected.topLevelClasses, actual.topLevelClasses);
    assertIterableEquals(expected.foundBlocks, actual.foundBlocks);
    assertEquals(expected.beginOfImports, actual.beginOfImports);
  }
}
