package fxui.util;

import common.Util;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class SystemUtils {
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

  public static int executeToolInTerminal(String... parameters) {
    String executedJar = SystemUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    String[] jarCmd = {"java", "-cp", executedJar, "tool.Main"};
    String[] fullCmd = Util.prependToArray(parameters, jarCmd);
    String cmdString = String.join(" ", fullCmd);
    String[] command = switch (Util.getOS()) {
      case WINDOWS -> new String[]{
          "cmd.exe", "/c",
          "start cmd.exe /c \"%s\" && exit".formatted(cmdString)
      };
      case LINUX -> new String[]{ // TODO: works only on GNOME by now
          "/bin/sh", "-c",
          "gnome-terminal -- bash -c \"%s; echo Done - Press enter to exit; read\" ".formatted(cmdString)
      };
      case MAC -> new String[]{ // TODO: check
          "osascript", "-e", """
          'tell app "Terminal"
              do script "%s"
          end tell'
          """.formatted(cmdString)
      };
      case SOLARIS -> throw new RuntimeException("unsupported operating system");
    };
    return Util.runCommand(Path.of("."), command);
  }
}
