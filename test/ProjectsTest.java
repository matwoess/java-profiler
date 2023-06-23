import misc.IO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectsTest {
  private static final Path projectsRoot = Path.of("projects");

  @BeforeAll
  public static void assertProjectsRootExists() {
    if (!projectsRoot.toFile().exists()) {
      throw new RuntimeException("folder " + projectsRoot + " does not exists");
    }
  }

  @Test
  public void testCocoR() {
    Path sourcesRoot = projectsRoot.resolve("CocoR").resolve("src");
    Path cocoAtg = sourcesRoot.resolve("Coco.atg");
    TestUtils.instrumentFolder(sourcesRoot);
    String[] command = "javac -source 7 -target 7 -d . Trace.java Scanner.java Tab.java DFA.java ParserGen.java Parser.java Coco.java".split(" ");
    int compileResult = misc.Util.runCommand(IO.instrumentDir, command);
    assertEquals(0, compileResult);
    command = new String[]{"java", "Coco/Coco", IO.instrumentDir.relativize(cocoAtg).toString()};
    int runResult = misc.Util.runCommand(IO.instrumentDir, command);
    assertEquals(0, runResult);
    TestUtils.generateReport();
  }

  @Test
  public void testZip4J() {
    Path sourcesRoot = projectsRoot.resolve(Path.of("zip4j", "src", "main", "java"));
    TestUtils.instrumentFolderAndProfile(sourcesRoot, "Main.java");
  }

  @Test
  public void testJacoco_Instrument() {
    Path sourcesRoot = projectsRoot.resolve(Path.of("zip4j", "src", "main", "java"));
    TestUtils.instrumentFolderAndProfile(sourcesRoot, "Main.java");
  }
}
