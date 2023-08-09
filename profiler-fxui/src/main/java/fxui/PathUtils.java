package fxui;

import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PathUtils {
  public static void chooseFile(TextField pathField) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Choose File");
    File file = fileChooser.showOpenDialog(pathField.getScene().getWindow());
    if (file != null) {
      pathField.setText(file.toString());
    }
  }

  public static void chooseDirectory(TextField pathField) {
    DirectoryChooser dirChooser = new DirectoryChooser();
    dirChooser.setTitle("Choose Directory");
    File dir = dirChooser.showDialog(pathField.getScene().getWindow());
    if (dir != null) {
      pathField.setText(dir.toString());
    }
  }

  public static void openWithDesktopApplication(Path path) {
    SwingUtilities.invokeLater(() -> {
      try {
        Desktop.getDesktop().open(path.toFile());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
