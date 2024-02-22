package cli;

import common.RunMode;
import org.junit.jupiter.api.Test;
import tool.cli.Arguments;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tool.cli.Arguments.getUsage;

public class ArgumentsTest {
  final Path samplesFolder = Path.of("..", "sample");
  final Path simpleExampleFile = samplesFolder.resolve("Simple.java");
  final Path lambdaExampleFile = samplesFolder.resolve("Lambdas.java");
  final Path algorithmsExampleFile = samplesFolder.resolve("Algorithms.java");

  @Test
  public void testShowUsage_NoError() {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    Arguments.parse(new String[]{"-h"});
    assertEquals(getUsage() + "\n", outContent.toString());
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    Arguments.parse(new String[]{"--help"});
    assertEquals(getUsage() + "\n", outContent.toString());
    // not as first argument
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    Arguments.parse(new String[]{"-s", "-v", "-d", samplesFolder.toString(), "--help"});
    assertEquals(getUsage() + "\n", outContent.toString());
    System.setOut(originalOut);
  }

  @Test
  public void testNoArguments() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Arguments.parse(new String[0]),
        "No arguments specified."
    );
  }

  @Test
  public void testInstrumentAndProfileOneFileArguments() {
    Arguments args = Arguments.parse(new String[]{simpleExampleFile.toString()});
    Arguments expected = new Arguments(RunMode.DEFAULT, simpleExampleFile, null, false, false, null);
    assertEquals(expected, args);
  }

  @Test
  public void testInstrumentFolderAndProfileOneFile() {
    Arguments args = Arguments.parse(new String[]{"-d", samplesFolder.toString(), simpleExampleFile.toString()});
    Arguments expected = new Arguments(RunMode.DEFAULT, simpleExampleFile, samplesFolder, false, false, null);
    assertEquals(expected, args);
  }

  @Test
  public void testInstrumentAndProfileWithArgument() {
    Arguments args = Arguments.parse(new String[]{algorithmsExampleFile.toString(), "10"});
    Arguments expected = new Arguments(RunMode.DEFAULT, algorithmsExampleFile, null, false, false, new String[]{"10"});
    assertEquals(expected, args);
  }

  @Test
  public void testInstrumentFolderAndProfileWithArgument() {
    Arguments args = Arguments.parse(new String[]{"-d", samplesFolder.toString(), algorithmsExampleFile.toString(), "20"});
    Arguments expected = new Arguments(RunMode.DEFAULT, algorithmsExampleFile, samplesFolder, false, false, new String[]{"20"});
    assertEquals(expected, args);
  }

  @Test
  public void testInstrumentOnly_MissingArgument() {
    String[] args1 = new String[]{"-i"};
    assertThrows(
        IllegalArgumentException.class,
        () -> Arguments.parse(args1),
        "No target file or directory specified."
    );
    String[] args2 = new String[]{"--instrument-only"};
    assertThrows(
        IllegalArgumentException.class,
        () -> Arguments.parse(args2),
        "No target file or directory specified."
    );
  }

  @Test
  public void testInstrumentOnly_TooManyArguments() {
    String[] args1 = new String[]{"-i", "filePathString", "additionalArg"};
    assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args1), "Too many arguments.");
    String[] args2 = new String[]{"--instrument-only", "filePathString", "additionalArg"};
    assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args2), "Too many arguments.");
  }

  @Test
  public void testInstrumentFolderAndProfile_MissingMainArgument() {
    String[] args1 = new String[]{"-d", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args1), "No target file or directory specified.");
    String[] args2 = new String[]{"--sources-directory", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args2), "No target file or directory specified.");
  }

  @Test
  public void testCreateReportOnly_UnexpectedArgument() {
    String[] args1 = new String[]{"-r", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args1), "Unexpected argument: " + samplesFolder);
    String[] args2 = new String[]{"--generate-report", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args2), "Unexpected argument: " + samplesFolder);
  }

  @Test
  public void testInstrumentOnlyAndReportOnly_BothExclusiveModes() {
    String[] args1 = new String[]{"-r", "-i", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args1), "Multiple run modes specified.");
    String[] args2 = new String[]{"-v", "-s", "--generate-report", "--instrument-only", simpleExampleFile.toString()};
    assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args2), "Multiple run modes specified.");
  }

  @Test
  public void testInstrumentOneFile() {
    Arguments.parse(new String[]{"-i", simpleExampleFile.toString()});
    Arguments.parse(new String[]{"--instrument-only", simpleExampleFile.toString()});
  }

  @Test
  public void testInstrumentAFolder() {
    Arguments.parse(new String[]{"-i", samplesFolder.toString()});
    Arguments.parse(new String[]{"--instrument-only", samplesFolder.toString()});
  }

  @Test
  public void testInstrumentFolderAndProfileAFile() {
    Arguments.parse(new String[]{"-d", samplesFolder.toString(), simpleExampleFile.toString()});
    Arguments.parse(new String[]{"--sources-directory", samplesFolder.toString(), simpleExampleFile.toString()});
  }

  @Test
  public void testSynchronizedOption_NoMoreArguments() {
    assertThrows(IllegalArgumentException.class, () -> Arguments.parse(new String[]{"-s"}));
    assertThrows(IllegalArgumentException.class, () -> Arguments.parse(new String[]{"--synchronized"}));
  }

  @Test
  public void testSynchronizedCompileAndExecute() {
    Arguments.parse(new String[]{"-s", lambdaExampleFile.toString()});
    Arguments.parse(new String[]{"-s", "-d", samplesFolder.toString(), lambdaExampleFile.toString()});
  }

  @Test
  public void testVerbose_MissingArguments() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Arguments.parse(new String[]{"-v"}),
        "No target file or directory specified."
    );
    assertThrows(IllegalArgumentException.class,
        () -> Arguments.parse(new String[]{"--verbose"}),
        "No target file or directory specified."
    );
  }

  @Test
  public void testVerbose_CompileAndExecute() {
    Arguments args = Arguments.parse(new String[]{"-v", lambdaExampleFile.toString()});
    Arguments expected = new Arguments(RunMode.DEFAULT, lambdaExampleFile, null, false, true, null);
    assertEquals(expected, args);
    args = Arguments.parse(new String[]{"-v", "-d", samplesFolder.toString(), simpleExampleFile.toString()});
    expected = new Arguments(RunMode.DEFAULT, simpleExampleFile, samplesFolder, false, true, null);
    assertEquals(expected, args);
  }

}
