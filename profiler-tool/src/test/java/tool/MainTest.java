package tool;

import common.IO;
import common.JavaCommandBuilder;
import common.JCompilerCommandBuilder;
import common.Util;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
  final Path samplesFolder = Path.of("..", "sample");
  final Path simpleExampleFile = samplesFolder.resolve("Simple.java");
  final Path lambdaExampleFile = samplesFolder.resolve("Lambdas.java");
  final Path algorithmsExampleFile = samplesFolder.resolve("Algorithms.java");

  @Test
  public void testShowUsage_NoError() {
    Main.main(new String[]{"-h"});
    Main.main(new String[]{"--help"});
  }

  @Test
  public void testNoArguments() {
    assertThrows(IllegalArgumentException.class, () -> Main.main(new String[0]));
  }

  @Test
  public void testInstrumentAndProfileOneFile() {
    Main.main(new String[]{simpleExampleFile.toString()});
  }

  @Test
  public void testInstrumentFolderAndProfileOneFile() {
    Main.main(new String[]{"-d", samplesFolder.toString(), simpleExampleFile.toString()});
  }

  @Test
  public void testInstrumentAndProfileWithArgument() {
    Main.main(new String[]{algorithmsExampleFile.toString(), "10"});
  }

  @Test
  public void testInstrumentFolderAndProfileWithArgument() {
    Main.main(new String[]{"-d", samplesFolder.toString(), algorithmsExampleFile.toString(), "20"});
  }

  @Test
  public void testInstrumentOnly_MissingArgument() {
    String[] args1 = new String[]{"-i"};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args1));
    String[] args2 = new String[]{"--instrument-only"};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args2));
  }

  @Test
  public void testInstrumentOnly_TooManyArguments() {
    String[] args1 = new String[]{"-i", "filePathString", "additionalArg"};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args1));
    String[] args2 = new String[]{"--instrument-only", "filePathString", "additionalArg"};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args2));
  }

  @Test
  public void testInstrumentFolderAndProfile_MissingMainArgument() {
    String[] args1 = new String[]{"-d", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args1));
    String[] args2 = new String[]{"--sources-directory", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args2));
  }

  @Test
  public void testCreateReportOnly_UnexpectedArgument() {
    String[] args1 = new String[]{"-r", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args1));
    String[] args2 = new String[]{"--generate-report", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args2));
  }

  @Test
  public void testInstrumentOnlyAndReportOnly_BothExclusiveModes() {
    String[] args1 = new String[]{"-r", "-i", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args1));
    String[] args2 = new String[]{"-v", "-s", "--generate-report", "--instrument-only", simpleExampleFile.toString()};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args2));
  }

  @Test
  public void testInstrumentOneFile() {
    Main.main(new String[]{"-i", simpleExampleFile.toString()});
    Main.main(new String[]{"--instrument-only", simpleExampleFile.toString()});
  }

  @Test
  public void testInstrumentAFolder() {
    Main.main(new String[]{"-i", samplesFolder.toString()});
    Main.main(new String[]{"--instrument-only", samplesFolder.toString()});
  }

  @Test
  public void testInstrumentFolderAndProfileAFile() {
    Main.main(new String[]{"-d", samplesFolder.toString(), simpleExampleFile.toString()});
    Main.main(new String[]{"--sources-directory", samplesFolder.toString(), simpleExampleFile.toString()});
  }

  @Test
  public void testInstrumentManualCompileThenCreateReportOnly() {
    Main.main(new String[]{"-i", samplesFolder.toString()});
    String filename = simpleExampleFile.getFileName().toString();
    Path instrDir = IO.getInstrumentDir();
    int exitCode = Util.runCommand(new JCompilerCommandBuilder()
        .setClassPath(instrDir)
        .setDirectory(instrDir)
        .addSourceFile(IO.getInstrumentDir().resolve(filename))
        .build());
    assertEquals(0, exitCode);
    exitCode = Util.runCommand(new JavaCommandBuilder()
        .setClassPath(instrDir)
        .setMainClass(filename.replace(".java", ""))
        .build());
    assertEquals(0, exitCode);
    Main.main(new String[]{"-r"});
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void testReportOnly_MissingMetadataOrCounts() {
    File metadataFile = IO.getMetadataPath().toFile();
    if (metadataFile.exists()) {
      metadataFile.delete();
    }
    File countsFile = IO.getCountsPath().toFile();
    if (countsFile.exists()) {
      countsFile.delete();
    }
    RuntimeException ex = assertThrows(RuntimeException.class, () -> Main.main(new String[]{"-r"}));
    assertTrue(ex.getMessage().contains("metadata.dat"));
    Main.main(new String[]{"-i", simpleExampleFile.toString()});
    ex = assertThrows(RuntimeException.class, () -> Main.main(new String[]{"-r"}));
    assertTrue(ex.getMessage().contains("counts.dat"));
  }

  @Test
  public void testSynchronizedOption_NoMoreArguments() {
    assertThrows(IllegalArgumentException.class, () -> Main.main(new String[]{"-s"}));
    assertThrows(IllegalArgumentException.class, () -> Main.main(new String[]{"--synchronized"}));
  }

  @Test
  public void testSynchronizedOption_Instrument() throws IOException {
    Main.main(new String[]{"-s", "-i", simpleExampleFile.toString()});
    String instrumentedContent = Files.readString(IO.getInstrumentDir().resolve(simpleExampleFile.getFileName()));
    assertTrue(instrumentedContent.contains("incSync("));
    assertFalse(instrumentedContent.contains("inc("));
    Main.main(new String[]{"--synchronized", "--verbose", "-i", lambdaExampleFile.toString()});
    instrumentedContent = Files.readString(IO.getInstrumentDir().resolve(lambdaExampleFile.getFileName()));
    assertTrue(instrumentedContent.contains("incSync("));
    assertTrue(instrumentedContent.contains("incLambdaSync("));
    assertFalse(instrumentedContent.contains("inc("));
    assertFalse(instrumentedContent.contains("incLambda("));
  }

  @Test
  public void testSynchronizedCompileAndExecute() {
    Main.main(new String[]{"-s", lambdaExampleFile.toString()});
    Main.main(new String[]{"-s", "-d", samplesFolder.toString(), lambdaExampleFile.toString()});
  }

  @Test
  public void testVerbose_MissingArguments() {
    assertThrows(IllegalArgumentException.class, () -> Main.main(new String[]{"-v"}));
    assertThrows(IllegalArgumentException.class, () -> Main.main(new String[]{"--verbose"}));
  }

  @Test
  public void testVerbose_CompileAndExecute() {
    Main.main(new String[]{"-v", lambdaExampleFile.toString()});
    Main.main(new String[]{"-v", "-d", samplesFolder.toString(), simpleExampleFile.toString()});
  }

}
