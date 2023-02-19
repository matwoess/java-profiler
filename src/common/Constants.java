package common;

import java.nio.file.Path;

public class Constants {

  public static final Path outputDir = Path.of("out", "profiler");
  public static final Path instrumentDir = outputDir.resolve("instrumented");
  public static final Path auxiliaryInstrumentDir = instrumentDir.resolve("auxiliary");
  public static final Path metadataFile = outputDir.resolve("metadata.txt");
  public static final Path countsFile = outputDir.resolve("counts.txt");

  public static final Path auxiliarySourceDir = Path.of("src", "auxiliary");

  static {
    if (outputDir.toFile().mkdirs()) {
      System.out.println("created output directory.");
    }
    if (instrumentDir.toFile().mkdir()) {
      System.out.println("created instrumented directory.");
    }
    if (auxiliaryInstrumentDir.toFile().mkdir()) {
      System.out.println("created auxiliary package directory.");
    }
  }
}
