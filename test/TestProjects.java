import misc.IO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestProjects {
  private static final Path projectsRoot = Path.of("projects");

  @BeforeAll
  public static void assertProjectsRootExists() {
    if (!projectsRoot.toFile().exists()) {
      throw new RuntimeException("folder " + projectsRoot + " does not exists");
    }
  }

  @Test
  public void TestCocoR() throws IOException, InterruptedException {
    Path cocoRoot = projectsRoot.resolve("CocoR").resolve("src");
    Path cocoAtg = cocoRoot.resolve("Coco.atg");
    TestUtil.instrumentFolder(cocoRoot);
    int compileResult = new ProcessBuilder().inheritIO().directory(IO.instrumentDir.toFile()).command(
        "javac -source 7 -target 7 -d . Trace.java Scanner.java Tab.java DFA.java ParserGen.java Parser.java Coco.java".split(" ")
    ).start().waitFor();
    assertEquals(0, compileResult);
    int runResult = new ProcessBuilder().inheritIO().directory(IO.instrumentDir.toFile()).command(
        "java", "Coco/Coco", IO.instrumentDir.relativize(cocoAtg).toString()
    ).start().waitFor();
    assertEquals(0, runResult);
    TestUtil.generateReport();
  }
}
