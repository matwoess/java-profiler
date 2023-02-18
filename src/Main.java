import common.JavaFile;
import instrument.Instrumenter;
import profile.Profiler;

import java.util.Arrays;

public class Main {

  public static void main(String[] args) {
    if (args.length == 0) {
      args = new String[]{
          "sample/Simple.java",
          "sample/Classes.java",
          "sample/MissingBraces.java",
      };
    }
    JavaFile[] javaFiles = Arrays.stream(args).map(JavaFile::new).toArray(JavaFile[]::new);
    Instrumenter instrumenter = new Instrumenter(javaFiles);
    instrumenter.analyzeFiles();
    instrumenter.instrument();
    instrumenter.exportBlockData();
  }
}
