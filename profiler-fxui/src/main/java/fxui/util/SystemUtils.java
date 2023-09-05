package fxui.util;

import common.Util;
import javafx.beans.property.ObjectProperty;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class SystemUtils {
  public static void chooseFile(ObjectProperty<Path> fileProperty) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Choose File");
    fileChooser.setInitialDirectory(fileProperty.get().toFile());
    File filePath = fileChooser.showOpenDialog(new Stage());
    if (filePath != null) {
      fileProperty.set(filePath.toPath());
    }
  }

  public static void chooseDirectory(ObjectProperty<Path> dirProperty) {
    DirectoryChooser dirChooser = new DirectoryChooser();
    dirChooser.setTitle("Choose Directory");
    dirChooser.setInitialDirectory(dirProperty.get().toFile());
    File dirPath = dirChooser.showDialog(new Stage());
    if (dirPath != null) {
      dirProperty.set(dirPath.toPath());
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

  public static int executeToolInTerminal(Path cwd, String... parameters) {
    String toolJar = tool.Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    String commonJar = common.Util.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    String[] mainCmd = {"java", "-cp", toolJar + ":" + commonJar, "tool.Main"};
    String[] fullCmd = Util.prependToArray(parameters, mainCmd);
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
    return Util.runCommand(cwd, command);
  }
}
