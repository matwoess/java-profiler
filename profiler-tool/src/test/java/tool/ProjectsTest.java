package tool;

import common.IO;
import common.JCompilerCommand;
import common.JavaCommand;
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
    Path instrDir = IO.getInstrumentDir();
    int compileResult = Util.runCommand(new JCompilerCommand()
        .setDirectory(instrDir)
        .setClassPath(instrDir)
        .addCompileArg("-source", "7")
        .addCompileArg("-target", "7")
        .addSourceFile(instrDir.resolve("Trace.java"))
        .addSourceFile(instrDir.resolve("Scanner.java"))
        .addSourceFile(instrDir.resolve("Tab.java"))
        .addSourceFile(instrDir.resolve("DFA.java"))
        .addSourceFile(instrDir.resolve("ParserGen.java"))
        .addSourceFile(instrDir.resolve("Parser.java"))
        .addSourceFile(instrDir.resolve("Coco.java"))
        .build());
    assertEquals(0, compileResult);
    int runResult = Util.runCommand(new JavaCommand()
        .setClassPath(IO.getInstrumentDir())
        .setMainClass("Coco/Coco")
        .addArgs(cocoAtg.toString())
        .build());
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
