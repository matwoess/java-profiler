package tool;

import common.IO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import common.Util;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectsTest {
  private static final Path projectsRoot = Path.of("..", "projects");

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
    int compileResult = Util.runCommandInDir(IO.getInstrumentDir(),
        "javac",
        "-d", ".",
        "-source", "7",
        "-target", "7",
        "Trace.java", "Scanner.java", "Tab.java", "DFA.java", "ParserGen.java", "Parser.java", "Coco.java");
    assertEquals(0, compileResult);
    int runResult = Util.runCommand(
        "java",
        "-cp", IO.getInstrumentDir().toString(),
        "Coco/Coco",
        cocoAtg.toString());
    assertEquals(0, runResult);
    TestUtils.generateReport();
  }

  @Test
  public void testZip4j() {
    Path sourcesRoot = projectsRoot.resolve(Path.of("zip4j", "src", "main", "java"));
    TestUtils.instrumentFolderAndProfile(sourcesRoot, "Main.java");
  }

  @Test
  public void testJaCoCo_instrumentAndMock() {
    Path sourcesRoot = projectsRoot.resolve("jacoco");
    TestUtils.instrumentFolder(sourcesRoot);
    TestUtils.createMockCounterData();
    TestUtils.generateReport();
  }

  @Test
  public void testDaCapoBench_instrumentAndMock() {
    Path sourcesRoot = projectsRoot.resolve("dacapobench");
    TestUtils.instrumentFolder(sourcesRoot);
    TestUtils.createMockCounterData();
    TestUtils.generateReport();
  }


  @Test
  public void testJUnit5_instrumentAndMock() {
    Path sourcesRoot = projectsRoot.resolve("junit5");
    TestUtils.instrumentFolder(sourcesRoot);
    TestUtils.createMockCounterData();
    TestUtils.generateReport();
  }
}
