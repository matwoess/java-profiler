import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class TestSamples {
  Path samplesFolder = Path.of("sample");

  private void instrumentAndProfile(Path mainFile) {
    String[] args = new String[]{mainFile.toString()};
    Main.main(args);
  }

  private void instrumentFolderAndProfile(Path sourcesDir, String mainFile) {
    String[] args = new String[]{"-d", sourcesDir.toString(), sourcesDir.resolve(mainFile).toString()};
    Main.main(args);
  }

  @Test
  public void TestSimpleSample() {
    instrumentAndProfile(samplesFolder.resolve("Simple.java"));
  }

  @Test
  public void TestSimpleSample_Folder() {
    instrumentFolderAndProfile(samplesFolder, "Simple.java");
  }

  @Test
  public void TestBasicElementsSample() {
    instrumentAndProfile(samplesFolder.resolve("BasicElements.java"));
  }

  @Test
  public void TestBasicElementsSample_Folder() {
    instrumentFolderAndProfile(samplesFolder, "BasicElements.java");
  }

  @Test
  public void TestClassesSample() {
    instrumentAndProfile(samplesFolder.resolve("Classes.java"));
  }

  @Test
  public void TestClassesSample_Folder() {
    instrumentFolderAndProfile(samplesFolder, "Classes.java");
  }

  @Test
  public void TestInterfacesSample() {
    instrumentAndProfile(samplesFolder.resolve("Interfaces.java"));
  }

  @Test
  public void TestInterfacesSample_Folder() {
    instrumentFolderAndProfile(samplesFolder, "Interfaces.java");
  }

  @Test
  public void TestEnumsSample() {
    instrumentAndProfile(samplesFolder.resolve("Enums.java"));
  }

  @Test
  public void TestEnumsSample_Folder() {
    instrumentFolderAndProfile(samplesFolder, "Enums.java");
  }

  @Test
  public void TestMissingBracesSample() {
    instrumentAndProfile(samplesFolder.resolve("MissingBraces.java"));
  }

  @Test
  public void TestMissingBracesSample_Folder() {
    instrumentFolderAndProfile(samplesFolder, "MissingBraces.java");
  }

  @Test
  public void TestPackagesSample() {
    instrumentAndProfile(samplesFolder.resolve("Packages.java"));
  }

  @Test
  public void TestPackagesSample_Folder() {
    instrumentFolderAndProfile(samplesFolder, "Packages.java");
  }

  @Test
  public void TestSwitchesSample() {
    instrumentAndProfile(samplesFolder.resolve("Switches.java"));
  }

  @Test
  public void TestSwitchesSample_Folder() {
    instrumentFolderAndProfile(samplesFolder, "Switches.java");
  }

  @Test
  public void TestLambdasSample() {
    instrumentAndProfile(samplesFolder.resolve("Lambdas.java"));
  }

  @Test
  public void TestLambdasSample_Folder() {
    instrumentFolderAndProfile(samplesFolder, "Lambdas.java");
  }

  @Test
  public void TestAnonymousClassesSample() {
    instrumentAndProfile(samplesFolder.resolve("AnonymousClasses.java"));
  }

  @Test
  public void TestAnonymousClassesSample_Folder() {
    instrumentFolderAndProfile(samplesFolder, "AnonymousClasses.java");
  }

  @Test
  public void TestAnnotationsSample() {
    instrumentAndProfile(samplesFolder.resolve("Annotations.java"));
  }

  @Test
  public void TestAnnotationsSample_Folder() {
    instrumentFolderAndProfile(samplesFolder, "Annotations.java");
  }

  @Test
  public void TestAllSamplesSample() {
    instrumentAndProfile(samplesFolder.resolve("AllSamples.java"));
  }

  @Test
  public void TestAllSamplesSample_Folder() {
    instrumentFolderAndProfile(samplesFolder, "AllSamples.java");
  }
}
