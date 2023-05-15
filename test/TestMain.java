import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Stream;

public class TestMain {
  @Test
  public void TestInstrumentAndProfileAllSamples() {
    Path samplesFolder = Path.of("sample");
    File[] sampleFiles = samplesFolder.toFile().listFiles();
    if (sampleFiles == null) {
      throw new RuntimeException("no sample files found");
    }
    Stream.of(sampleFiles)
        .filter(file -> !file.isDirectory())
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
