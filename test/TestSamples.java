import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestSamples {
  Path samplesFolder = Path.of("sample");

  @Test
  public void TestSimpleSample() {
    TestUtil.instrumentAndProfile(samplesFolder.resolve("Simple.java"));
  }

  @Test
  public void TestSimpleSample_Folder() {
    TestUtil.instrumentFolderAndProfile(samplesFolder, "Simple.java");
  }

  @Test
  public void TestBasicElementsSample() {
    TestUtil.instrumentAndProfile(samplesFolder.resolve("BasicElements.java"));
  }

  @Test
  public void TestBasicElementsSample_Folder() {
    TestUtil.instrumentFolderAndProfile(samplesFolder, "BasicElements.java");
  }

  @Test
  public void TestClassesSample() {
    TestUtil.instrumentAndProfile(samplesFolder.resolve("Classes.java"));
  }

  @Test
  public void TestClassesSample_Folder() {
    TestUtil.instrumentFolderAndProfile(samplesFolder, "Classes.java");
  }

  @Test
  public void TestInterfacesSample() {
    TestUtil.instrumentAndProfile(samplesFolder.resolve("Interfaces.java"));
  }

  @Test
  public void TestInterfacesSample_Folder() {
    TestUtil.instrumentFolderAndProfile(samplesFolder, "Interfaces.java");
  }

  @Test
  public void TestEnumsSample() {
    TestUtil.instrumentAndProfile(samplesFolder.resolve("Enums.java"));
  }

  @Test
  public void TestEnumsSample_Folder() {
    TestUtil.instrumentFolderAndProfile(samplesFolder, "Enums.java");
  }

  @Test
  public void TestMissingBracesSample() {
    TestUtil.instrumentAndProfile(samplesFolder.resolve("MissingBraces.java"));
  }

  @Test
  public void TestMissingBracesSample_Folder() {
    TestUtil.instrumentFolderAndProfile(samplesFolder, "MissingBraces.java");
  }

  @Test
  public void TestPackagesSample_MissingDependencies() {
    Path mainFile = samplesFolder.resolve("Packages.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtil.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void TestPackagesSample_Folder() {
    TestUtil.instrumentFolderAndProfile(samplesFolder, "Packages.java");
  }

  @Test
  public void TestSwitchesSample() {
    TestUtil.instrumentAndProfile(samplesFolder.resolve("Switches.java"));
  }

  @Test
  public void TestSwitchesSample_Folder() {
    TestUtil.instrumentFolderAndProfile(samplesFolder, "Switches.java");
  }

  @Test
  public void TestLambdasSample() {
    TestUtil.instrumentAndProfile(samplesFolder.resolve("Lambdas.java"));
  }

  @Test
  public void TestLambdasSample_Folder() {
    TestUtil.instrumentFolderAndProfile(samplesFolder, "Lambdas.java");
  }

  @Test
  public void TestAnonymousClassesSample_MissingDependencies() {
    Path mainFile = samplesFolder.resolve("AnonymousClasses.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtil.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void TestAnonymousClassesSample_Folder() {
    TestUtil.instrumentFolderAndProfile(samplesFolder, "AnonymousClasses.java");
  }

  @Test
  public void TestAnnotationsSample() {
    TestUtil.instrumentAndProfile(samplesFolder.resolve("Annotations.java"));
  }

  @Test
  public void TestAnnotationsSample_Folder() {
    TestUtil.instrumentFolderAndProfile(samplesFolder, "Annotations.java");
  }

  @Test
  public void TestAllSamplesSample() {
    Path mainFile = samplesFolder.resolve("AllSamples.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtil.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void TestAllSamplesSample_Folder() {
    TestUtil.instrumentFolderAndProfile(samplesFolder, "AllSamples.java");
  }
  
  
  @Test
  public void TestHelperSample_NoMainClass() {
    Path mainFile = samplesFolder.resolve("helper").resolve("Helper.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtil.instrumentAndProfile(mainFile));
    assertEquals("Error executing compiled class: Helper", ex.getMessage());
  }
}
