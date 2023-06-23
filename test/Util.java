import java.nio.file.Path;

public class Util {
  static void instrumentAndProfile(Path mainFile) {
    String[] args = new String[]{mainFile.toString()};
    Main.main(args);
  }

  static void instrumentFolderAndProfile(Path sourcesDir, String mainFile) {
    String[] args = new String[]{"-d", sourcesDir.toString(), sourcesDir.resolve(mainFile).toString()};
    Main.main(args);
  }

  static void instrumentFolder(Path sourcesDir) {
    Main.main(new String[]{"-i", sourcesDir.toString()});
  }

  static void generateReport() {
    Main.main(new String[]{"-r"});
  }
}
