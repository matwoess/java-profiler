package fxui;

import fxui.util.BindingUtils;
import fxui.util.SystemUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.nio.file.Path;

public class ProjectController {

  public Button btnProjectRoot;
  public TextField txtProjectRoot;
  public Button btnOpenProject;
  public BooleanProperty invalidProjectRootPath = new SimpleBooleanProperty(false);

  void bindProjectRootProperty(ObjectProperty<Path> projectRoot) {
    txtProjectRoot.textProperty().bindBidirectional(projectRoot, BindingUtils.pathStringConverter);
    invalidProjectRootPath.bind(txtProjectRoot.textProperty().isNotEmpty()
        .and(BindingUtils.createIsDirectoryBinding(projectRoot).not())
    );
    btnProjectRoot.setOnAction(event -> SystemUtils.chooseDirectory(txtProjectRoot, System.getProperty("user.home")));
    txtProjectRoot.borderProperty().bind(BindingUtils.createBorderBinding(txtProjectRoot.textProperty(), invalidProjectRootPath));
    btnOpenProject.disableProperty().bind(txtProjectRoot.textProperty().isEmpty().or(invalidProjectRootPath));
  }

  public void onOpenProject() {
    Stage stage = (Stage) btnOpenProject.getScene().getWindow();
    stage.close();
  }
}
