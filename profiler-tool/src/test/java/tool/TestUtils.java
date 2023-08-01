package tool;

import java.nio.file.Path;

public class TestUtils {
  static void instrumentAndProfile(Path mainFile) {
    String[] args = new String[]{"-v", mainFile.toString()};
    Main.main(args);
  }

  static void instrumentFolderAndProfile(Path sourcesDir, String mainFile) {
    String[] args = new String[]{"-v", "-d", sourcesDir.toString(), sourcesDir.resolve(mainFile).toString()};
    Main.main(args);
  }

  static void instrumentFolder(Path sourcesDir) {
    Main.main(new String[]{"-v", "-i", sourcesDir.toString()});
  }

  static void generateReport() {
    Main.main(new String[]{"-v", "-r"});
  }
}
