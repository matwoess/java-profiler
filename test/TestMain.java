import misc.IO;
import misc.Util;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestMain {
  Path samplesFolder = Path.of("sample");
  Path simpleExampleFile = samplesFolder.resolve("Simple.java");

  @Test
  public void TestShowUsage_NoError() {
    Main.main(new String[]{"-h"});
    Main.main(new String[]{"--help"});
  }

  @Test
  public void TestInstrumentOnly_MissingArgument() {
    String[] args1 = new String[]{"-i"};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args1));
    String[] args2 = new String[]{"--instrument-only"};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args2));
  }

  @Test
  public void TestInstrumentOnly_TooManyArguments() {
    String[] args1 = new String[]{"-i", "filePathString", "additionalArg"};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args1));
    String[] args2 = new String[]{"--instrument-only", "filePathString", "additionalArg"};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args2));
  }

  @Test
  public void TestInstrumentAndProfile_Missing2ndArgument() {
    String[] args1 = new String[]{"-d", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args1));
    String[] args2 = new String[]{"--sources-directory", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args2));
  }

  @Test
  public void TestCreateReportOnly_UnexpectedArgument() {
    String[] args1 = new String[]{"-r", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args1));
    String[] args2 = new String[]{"--generate-report", samplesFolder.toString()};
    assertThrows(IllegalArgumentException.class, () -> Main.main(args2));
  }

  @Test
  public void TestInstrumentOneFile() {
    Main.main(new String[]{"-i", simpleExampleFile.toString()});
    Main.main(new String[]{"--instrument-only", simpleExampleFile.toString()});
  }

  @Test
  public void TestInstrumentAFolder() {
    Main.main(new String[]{"-i", samplesFolder.toString()});
    Main.main(new String[]{"--instrument-only", samplesFolder.toString()});
  }

  @Test
  public void TestInstrumentAFolderAndProfileAFile() {
    Main.main(new String[]{"-d", samplesFolder.toString(), simpleExampleFile.toString()});
    Main.main(new String[]{"--sources-directory", samplesFolder.toString(), simpleExampleFile.toString()});
  }

  @Test
  public void TestInstrumentManualCompileThenCreateReportOnly() {
    Main.main(new String[]{"-i", samplesFolder.toString()});
    int exitCode = Util.runCommand(IO.instrumentDir, "javac", simpleExampleFile.getFileName().toString());
    assertEquals(0, exitCode);
    Main.main(new String[]{"-r"});
  }
}
