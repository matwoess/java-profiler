package misc;

import java.nio.file.Path;

public class Constants {

  public static final Path outputDir = Path.of("out", "profiler");
  public static final Path instrumentDir = outputDir.resolve("instrumented");
  public static final Path auxiliaryInstrumentDir = instrumentDir.resolve("auxiliary");
  public static final Path metadataFile = outputDir.resolve("metadata.txt");
  public static final Path resultsFile = outputDir.resolve("counts.txt");

  public static final Path reportDir = outputDir.resolve("report");
  public static final Path reportIndexFile = reportDir.resolve("index.html");
  public static final Path reportHighlighter = reportDir.resolve("highlighter.js");

}
