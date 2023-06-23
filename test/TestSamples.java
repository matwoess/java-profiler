import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestSamples {
  Path samplesFolder = Path.of("sample");

  @Test
  public void TestSimpleSample() {
    Util.instrumentAndProfile(samplesFolder.resolve("Simple.java"));
  }

  @Test
  public void TestSimpleSample_Folder() {
    Util.instrumentFolderAndProfile(samplesFolder, "Simple.java");
  }

  @Test
  public void TestBasicElementsSample() {
    Util.instrumentAndProfile(samplesFolder.resolve("BasicElements.java"));
  }

  @Test
  public void TestBasicElementsSample_Folder() {
    Util.instrumentFolderAndProfile(samplesFolder, "BasicElements.java");
  }

  @Test
  public void TestClassesSample() {
    Util.instrumentAndProfile(samplesFolder.resolve("Classes.java"));
  }

  @Test
  public void TestClassesSample_Folder() {
    Util.instrumentFolderAndProfile(samplesFolder, "Classes.java");
  }

  @Test
  public void TestInterfacesSample() {
    Util.instrumentAndProfile(samplesFolder.resolve("Interfaces.java"));
  }

  @Test
  public void TestInterfacesSample_Folder() {
    Util.instrumentFolderAndProfile(samplesFolder, "Interfaces.java");
  }

  @Test
  public void TestEnumsSample() {
    Util.instrumentAndProfile(samplesFolder.resolve("Enums.java"));
  }

  @Test
  public void TestEnumsSample_Folder() {
    Util.instrumentFolderAndProfile(samplesFolder, "Enums.java");
  }

  @Test
  public void TestMissingBracesSample() {
    Util.instrumentAndProfile(samplesFolder.resolve("MissingBraces.java"));
  }

  @Test
  public void TestMissingBracesSample_Folder() {
    Util.instrumentFolderAndProfile(samplesFolder, "MissingBraces.java");
  }

  @Test
  public void TestPackagesSample_MissingDependencies() {
    Path mainFile = samplesFolder.resolve("Packages.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> Util.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void TestPackagesSample_Folder() {
    Util.instrumentFolderAndProfile(samplesFolder, "Packages.java");
  }

  @Test
  public void TestSwitchesSample() {
    Util.instrumentAndProfile(samplesFolder.resolve("Switches.java"));
  }

  @Test
  public void TestSwitchesSample_Folder() {
    Util.instrumentFolderAndProfile(samplesFolder, "Switches.java");
  }

  @Test
  public void TestLambdasSample() {
    Util.instrumentAndProfile(samplesFolder.resolve("Lambdas.java"));
  }

  @Test
  public void TestLambdasSample_Folder() {
    Util.instrumentFolderAndProfile(samplesFolder, "Lambdas.java");
  }

  @Test
  public void TestAnonymousClassesSample_MissingDependencies() {
    Path mainFile = samplesFolder.resolve("AnonymousClasses.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> Util.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void TestAnonymousClassesSample_Folder() {
    Util.instrumentFolderAndProfile(samplesFolder, "AnonymousClasses.java");
  }

  @Test
  public void TestAnnotationsSample() {
    Util.instrumentAndProfile(samplesFolder.resolve("Annotations.java"));
  }

  @Test
  public void TestAnnotationsSample_Folder() {
    Util.instrumentFolderAndProfile(samplesFolder, "Annotations.java");
  }

  @Test
  public void TestAllSamplesSample() {
    Path mainFile = samplesFolder.resolve("AllSamples.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> Util.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void TestAllSamplesSample_Folder() {
    Util.instrumentFolderAndProfile(samplesFolder, "AllSamples.java");
  }
  
  
  @Test
  public void TestHelperSample_NoMainClass() {
    Path mainFile = samplesFolder.resolve("helper").resolve("Helper.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> Util.instrumentAndProfile(mainFile));
    assertEquals("Error executing compiled class: Helper", ex.getMessage());
  }
}
