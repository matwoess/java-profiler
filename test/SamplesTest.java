import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SamplesTest {
  Path samplesFolder = Path.of("sample");

  @Test
  public void testSimpleSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Simple.java"));
  }

  @Test
  public void testSimpleSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Simple.java");
  }

  @Test
  public void testBasicElementsSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("BasicElements.java"));
  }

  @Test
  public void testBasicElementsSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "BasicElements.java");
  }

  @Test
  public void testClassesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Classes.java"));
  }

  @Test
  public void testClassesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Classes.java");
  }

  @Test
  public void testInterfacesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Interfaces.java"));
  }

  @Test
  public void testInterfacesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Interfaces.java");
  }

  @Test
  public void testEnumsSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Enums.java"));
  }

  @Test
  public void testEnumsSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Enums.java");
  }

  @Test
  public void testMissingBracesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("MissingBraces.java"));
  }

  @Test
  public void testMissingBracesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "MissingBraces.java");
  }

  @Test
  public void testPackagesSample_MissingDependencies() {
    Path mainFile = samplesFolder.resolve("Packages.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtils.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void testPackagesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Packages.java");
  }

  @Test
  public void testSwitchesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Switches.java"));
  }

  @Test
  public void testSwitchesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Switches.java");
  }

  @Test
  public void testLambdasSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Lambdas.java"));
  }

  @Test
  public void testLambdasSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Lambdas.java");
  }

  @Test
  public void testAnonymousClassesSample_MissingDependencies() {
    Path mainFile = samplesFolder.resolve("AnonymousClasses.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtils.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void testAnonymousClassesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "AnonymousClasses.java");
  }

  @Test
  public void testAnnotationsSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Annotations.java"));
  }

  @Test
  public void testAnnotationsSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Annotations.java");
  }

  @Test
  public void testAllSamplesSample() {
    Path mainFile = samplesFolder.resolve("AllSamples.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtils.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void testAllSamplesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "AllSamples.java");
  }
  
  
  @Test
  public void testHelperSample_NoMainClass() {
    Path mainFile = samplesFolder.resolve("helper").resolve("Helper.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtils.instrumentAndProfile(mainFile));
    assertEquals("Error executing compiled class: Helper", ex.getMessage());
  }

  @Test
  public void testNestedBlockTypesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("NestedBlockTypes.java"));
  }

  @Test
  public void testNestedBlockTypesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "NestedBlockTypes.java");
  }

  @Test
  public void testLocalClassesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("LocalClasses.java"));
  }

  @Test
  public void testLocalClassesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "LocalClasses.java");
  }
}
