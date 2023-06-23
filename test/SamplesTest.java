import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SamplesTest {
  Path samplesFolder = Path.of("sample");

  @Test
  public void TestSimpleSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Simple.java"));
  }

  @Test
  public void TestSimpleSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Simple.java");
  }

  @Test
  public void TestBasicElementsSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("BasicElements.java"));
  }

  @Test
  public void TestBasicElementsSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "BasicElements.java");
  }

  @Test
  public void TestClassesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Classes.java"));
  }

  @Test
  public void TestClassesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Classes.java");
  }

  @Test
  public void TestInterfacesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Interfaces.java"));
  }

  @Test
  public void TestInterfacesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Interfaces.java");
  }

  @Test
  public void TestEnumsSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Enums.java"));
  }

  @Test
  public void TestEnumsSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Enums.java");
  }

  @Test
  public void TestMissingBracesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("MissingBraces.java"));
  }

  @Test
  public void TestMissingBracesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "MissingBraces.java");
  }

  @Test
  public void TestPackagesSample_MissingDependencies() {
    Path mainFile = samplesFolder.resolve("Packages.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtils.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void TestPackagesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Packages.java");
  }

  @Test
  public void TestSwitchesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Switches.java"));
  }

  @Test
  public void TestSwitchesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Switches.java");
  }

  @Test
  public void TestLambdasSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Lambdas.java"));
  }

  @Test
  public void TestLambdasSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Lambdas.java");
  }

  @Test
  public void TestAnonymousClassesSample_MissingDependencies() {
    Path mainFile = samplesFolder.resolve("AnonymousClasses.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtils.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void TestAnonymousClassesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "AnonymousClasses.java");
  }

  @Test
  public void TestAnnotationsSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Annotations.java"));
  }

  @Test
  public void TestAnnotationsSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Annotations.java");
  }

  @Test
  public void TestAllSamplesSample() {
    Path mainFile = samplesFolder.resolve("AllSamples.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtils.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void TestAllSamplesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "AllSamples.java");
  }
  
  
  @Test
  public void TestHelperSample_NoMainClass() {
    Path mainFile = samplesFolder.resolve("helper").resolve("Helper.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtils.instrumentAndProfile(mainFile));
    assertEquals("Error executing compiled class: Helper", ex.getMessage());
  }
}
