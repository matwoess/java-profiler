package tool;

import common.IO;

import java.io.*;
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

  static void createMockCounterData() {
    int nBlocks;
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(IO.getMetadataPath().toFile()))) {
      nBlocks = ois.readInt(); // number of blocks is the first value of the metadata file
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
    try (DataOutputStream dis = new DataOutputStream(new FileOutputStream(IO.getCountsPath().toFile()))) {
      dis.writeInt(nBlocks);
      for (int i = 0; i < nBlocks; i++) {
        dis.writeInt(1);
      }
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
  }
}
