package common;

import java.nio.file.Path;

public class Constants {
  public static final Path outputDir = Path.of("out/instrumented");
  public static final Path profilerRoot = outputDir.resolve("profile");

  static {
    if (outputDir.toFile().mkdirs()) {
      System.out.println("created output directory.");
    }
    if (profilerRoot.toFile().mkdir()) {
      System.out.println("created 'profile' package sub-directory.");
    }
  }
}
