package tool;

import common.IO;
import common.JCompilerCommand;
import common.JavaCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import common.Util;
import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ProjectsTest.ExtensionContextParameterResolver.class)
public class ProjectsTest {
  private static final Path projectsRoot = Path.of("..", "projects");

  /**
   * Custom annotation to specify a GitHub repo needed for a project test.
   * The repository name should be specified in the "Owner/Name" format.
   * The destination folder is the name for the new folder inside projectsRoot.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @interface GithubRepoSource {
    String repositoryName();

    String destinationFolder();
  }

  /**
   * Detect our own GithubRepoSource annotation before executing Project tests
   * and automatically download it from GitHub if missing.
   *
   * @param context used for getting the method reference to the executing test
   */
  @BeforeEach
  public void downloadGithubRepoIfNecessary(ExtensionContext context) {
    Method testMethod = context.getRequiredTestMethod();
    if (testMethod.isAnnotationPresent(GithubRepoSource.class)) {
      GithubRepoSource annotation = testMethod.getAnnotation(GithubRepoSource.class);
      String localFolder = annotation.destinationFolder();
      String repositoryUrl = annotation.repositoryName();
      if (!projectsRoot.resolve(localFolder).toFile().exists()) {
        Path zip = TestUtils.downloadGithubRepoZip(projectsRoot, localFolder, repositoryUrl);
        if (zip != null) {
          TestUtils.unzipRepo(zip, projectsRoot, localFolder);
        }
      }
    }
  }

  /**
   * Instrumenting Coco/R using Coco/R, then calling Coco/R on Coco/R.
   */
  @Test
  @GithubRepoSource(repositoryName = "SSW-CocoR/CocoR-Java", destinationFolder = "CocoR")
  public void testCocoR() {
    Path sourcesRoot = projectsRoot.resolve("CocoR").resolve("src");
    Path cocoAtg = sourcesRoot.resolve("Coco.atg");
    // re-generate Parser and Scanner once before instrumentation
    // Coco removes the copyright notice when (re-)creating its own Parser and Scanner
    // leading to suddenly 27 lines less and mismatching metadata
    int genScannerParserResult = Util.runCommand(new JavaCommand()
        .setClassPath(Path.of("..", "lib", "Coco.jar")) // should be present, use script 'generate-parser.sh' if not
        .setMainClass("Coco/Coco")
        .addArgs(cocoAtg.toString())
        .build());
    assertEquals(0, genScannerParserResult);
    // only instrument
    TestUtils.instrumentFolder(sourcesRoot);
    // manual compile, because we need the legacy -source and -target parameters with the Java 17 compiler
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
    // run Coco on Coco with the Coco ATG
    int runResult = Util.runCommand(new JavaCommand()
        .setClassPath(IO.getInstrumentDir())
        .setMainClass("Coco/Coco")
        .addArgs(cocoAtg.toString())
        .build());
    assertEquals(0, runResult);
    // manually generate report
    TestUtils.generateReport();
  }

  /**
   * Does not have a main file, but we create one for getting a valid report easily.
   */
  @Test
  @GithubRepoSource(repositoryName = "srikanth-lingala/zip4j", destinationFolder = "zip4j")
  public void testZip4j() throws IOException {
    Path sourcesRoot = projectsRoot.resolve(Path.of("zip4j", "src", "main", "java"));
    Path mainFile = sourcesRoot.resolve("Main.java");
    if (!mainFile.toFile().exists()) {
      Files.writeString(mainFile, """
          import net.lingala.zip4j.ZipFile;
          import java.io.IOException;
          import java.nio.file.Files;
          import java.nio.file.Path;
                    
          public class Main {
            public static void main(String[] args) {
              try (ZipFile zipFile = new ZipFile(Files.createTempFile(null, "zip").toFile())) {
                zipFile.addFolder(Path.of(".profiler", "instrumented").toFile());
              } catch (IOException e) {throw new RuntimeException(e);}
            }
          }
          """);
    }
    TestUtils.instrumentFolderAndProfile(sourcesRoot, "Main.java");
  }

  /**
   * Uses maven to build. Only included to test grammar stability.
   */
  @Test
  @GithubRepoSource(repositoryName = "jacoco/jacoco", destinationFolder = "jacoco")
  public void testJaCoCo_instrumentAndMock() {
    Path sourcesRoot = projectsRoot.resolve("jacoco");
    TestUtils.instrumentFolder(sourcesRoot);
    TestUtils.createMockCounterData();
    TestUtils.generateReport();
  }

  /**
   * Uses ant to build. Only included to test grammar stability.
   */
  @Test
  @GithubRepoSource(repositoryName = "dacapobench/dacapobench", destinationFolder = "dacapobench")
  public void testDaCapoBench_instrumentAndMock() {
    Path sourcesRoot = projectsRoot.resolve("dacapobench");
    TestUtils.instrumentFolder(sourcesRoot);
    TestUtils.createMockCounterData();
    TestUtils.generateReport();
  }


  /**
   * Uses gradle to build. Only included to test grammar stability.
   */
  @Test
  @GithubRepoSource(repositoryName = "junit-team/junit5", destinationFolder = "junit5")
  public void testJUnit5_instrumentAndMock() {
    Path sourcesRoot = projectsRoot.resolve("junit5");
    TestUtils.instrumentFolder(sourcesRoot);
    TestUtils.createMockCounterData();
    TestUtils.generateReport();
  }


  /**
   * Needed for JUnit to resolve ExtensionContext context in @BeforeEach methods
   */
  static class ExtensionContextParameterResolver implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException {
      return parameterContext.getParameter().getType() == ExtensionContext.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException {
      return extensionContext;
    }
  }

}
