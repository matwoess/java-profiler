import misc.IO;
import misc.Util;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
  Path samplesFolder = Path.of("sample");
  Path simpleExampleFile = samplesFolder.resolve("Simple.java");
  Path fibonacciExampleFile = samplesFolder.resolve("Fibonacci.java");

  @Test
  public void testShowUsage_NoError() {
    Main.main(new String[]{"-h"});
    Main.main(new String[]{"--help"});
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
    Main.main(new String[]{fibonacciExampleFile.toString(), "10"});
  }

  @Test
  public void testInstrumentFolderAndProfileWithArgument() {
    Main.main(new String[]{"-d", samplesFolder.toString(), fibonacciExampleFile.toString(), "20"});
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
  public void testInstrumentAndProfile_Missing2ndArgument() {
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
  public void testInstrumentAFolderAndProfileAFile() {
    Main.main(new String[]{"-d", samplesFolder.toString(), simpleExampleFile.toString()});
    Main.main(new String[]{"--sources-directory", samplesFolder.toString(), simpleExampleFile.toString()});
  }

  @Test
  public void testInstrumentManualCompileThenCreateReportOnly() {
    Main.main(new String[]{"-i", samplesFolder.toString()});
    int exitCode = Util.runCommand(IO.instrumentDir, "javac", simpleExampleFile.getFileName().toString());
    assertEquals(0, exitCode);
    Main.main(new String[]{"-r"});
  }

  @Test
  public void testReportOnly_MissingMetadataOrCounts() {
    if (IO.resultsFile.toFile().exists()) {
      IO.resultsFile.toFile().delete();
    }
    if (IO.metadataFile.toFile().exists()) {
      IO.metadataFile.toFile().delete();
    }
    RuntimeException ex = assertThrows(RuntimeException.class, () -> Main.main(new String[]{"-r"}));
    assertTrue(ex.getMessage().contains("metadata.dat (No such file or directory)"));
    Main.main(new String[]{"-i", simpleExampleFile.toString()});
    ex = assertThrows(RuntimeException.class, () -> Main.main(new String[]{"-r"}));
    assertTrue(ex.getMessage().contains("counts.dat (No such file or directory)"));
  }
}
