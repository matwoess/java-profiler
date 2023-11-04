package fxui;

import common.IO;
import fxui.util.BindingUtils;
import fxui.util.SystemUtils;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectController {
  public Stage projectStage;

  @FXML
  private Button btnProjectRoot;
  @FXML
  private TextField txtProjectRoot;
  @FXML
  private Button btnOpenProject;

  public BooleanBinding invalidProjectRootPath;

  public void initUI(Stage stage) {
    projectStage = stage;
    projectStage.setResizable(false);
    projectStage.setTitle("Open Java Project");
  }

  void initProperties(ObjectProperty<Path> projectRootProperty) {
    txtProjectRoot.textProperty().bindBidirectional(projectRootProperty, BindingUtils.pathStringConverter);
    invalidProjectRootPath = txtProjectRoot.textProperty()
        .isNotEmpty()
        .and(BindingUtils.createIsDirectoryBinding(projectRootProperty).not());
    btnProjectRoot.setOnAction(event -> onPickProjectRoot(projectRootProperty));
    txtProjectRoot.borderProperty().bind(
        BindingUtils.createBorderBinding(txtProjectRoot.textProperty(), invalidProjectRootPath));
    btnOpenProject.disableProperty().bind(txtProjectRoot.textProperty().isEmpty().or(invalidProjectRootPath));
    restoreLastProjectPath();
  }

  private void onPickProjectRoot(ObjectProperty<Path> projectRootProperty) {
    SystemUtils.chooseDirectory(projectRootProperty);
  }

  public void onOpenProject() {
    exportProjectPath();
    projectStage.close();
  }

  private void exportProjectPath() {
    try {
      Files.writeString(common.IO.lastProjectPath(), txtProjectRoot.textProperty().get());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void restoreLastProjectPath() {
    if (!IO.lastProjectPath().toFile().exists()) return;
    try {
      String lastProjectPathStr = Files.readString(common.IO.lastProjectPath());
      txtProjectRoot.textProperty().set(lastProjectPathStr);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }
}
