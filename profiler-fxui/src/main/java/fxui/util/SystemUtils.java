package fxui.util;

import common.IO;
import common.Util;
import fxui.model.AppState;
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
    Path initialDir = dirProperty.get();
    if (initialDir == null || !initialDir.toFile().isDirectory()) {
      initialDir = IO.getUserHomeDir();
    }
    dirChooser.setInitialDirectory(initialDir.toFile());
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

  public static int executeToolWithParameters(AppState appState) {
    String[] terminalCommand = getTerminalCommand(appState);
    return Util.runCommand(appState.projectRoot.get(), terminalCommand);
  }

  public static String[] getTerminalCommand(AppState appState) {
    String javaCommand = getJavaRunCommand(appState.getProgramArguments());
    return appState.terminal.get().wrapWithTerminalCommand(javaCommand, appState.projectRoot.get());
  }

  public static String getJavaRunCommand(String[] toolArguments) {
    String toolJar = tool.Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    String commonJar = Util.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    String classPath = toolJar;
    if (!commonJar.equals(toolJar)) {
      classPath += Util.getOS().pathSeparator() + commonJar;
    }
    String[] toolMainCmd = {"java", "-cp", '"' + classPath + '"', "tool.Main"};
    String[] fullCmd = Util.prependToArray(toolArguments, toolMainCmd);
    return String.join(" ", fullCmd);
  }
}
