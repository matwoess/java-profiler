import misc.Util;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Stream;

public class TestMain {
  Path samplesFolder = Path.of("sample");
  Path simpleExampleFile = samplesFolder.resolve("Simple.java");

  @Test
  public void TestShowUsage() {
    Main.main(new String[]{"-h"});
    Main.main(new String[]{"--help"});
    Main.main(new String[]{"--instrument-only", "filePathString", "additionalArg"});
  }

  @Test
  public void TestInstrumentNoFileOrFolder() {
    Main.main(new String[]{"-i"});
    Main.main(new String[]{"--instrument-only"});
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
  public void TestInstrumentAFolderAndProfileNoFile_MissingArgument() {
    Main.main(new String[]{"-d", samplesFolder.toString()});
    Main.main(new String[]{"--sources-directory", samplesFolder.toString()});
  }

  @Test
  public void TestInstrumentAndProfileAllSamplesIndividually() {
    File[] sampleFiles = samplesFolder.toFile().listFiles();
    if (sampleFiles == null) {
      throw new RuntimeException("no sample files found");
    }
    Stream.of(sampleFiles)
        .filter(file -> Util.isJavaFile(file.toPath()))
        .forEach(jFile -> {
              String[] args = new String[]{
                  "-d",
                  samplesFolder.toString(),
                  jFile.toString(),
              };
              Main.main(args);
            }
        );
  }
}
