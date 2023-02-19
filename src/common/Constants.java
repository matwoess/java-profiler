package common;

import java.nio.file.Path;

public class Constants {
  public static final Path outputDir = Path.of("out/instrumented");
  public static final Path profilerRoot = outputDir.resolve("profile");
  public static final Path metadataFile = outputDir.resolve("metadata.txt");
  public static final Path resultsFile = outputDir.resolve("results.txt");

  static {
    if (outputDir.toFile().mkdirs()) {
      System.out.println("created output directory.");
    }
    if (profilerRoot.toFile().mkdir()) {
      System.out.println("created 'profile' package sub-directory.");
    }
  }
}
