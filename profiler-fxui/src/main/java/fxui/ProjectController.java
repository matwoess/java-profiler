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

  void initProperties(ObjectProperty<Path> projectRootProperty) {
    txtProjectRoot.textProperty().bindBidirectional(projectRootProperty, BindingUtils.pathStringConverter);
    invalidProjectRootPath.bind(txtProjectRoot.textProperty().isNotEmpty()
        .and(BindingUtils.createIsDirectoryBinding(projectRootProperty).not())
    );
    btnProjectRoot.setOnAction(event -> onPickProjectRoot(projectRootProperty));
    txtProjectRoot.borderProperty().bind(BindingUtils.createBorderBinding(txtProjectRoot.textProperty(), invalidProjectRootPath));
    btnOpenProject.disableProperty().bind(txtProjectRoot.textProperty().isEmpty().or(invalidProjectRootPath));
  }

  private void onPickProjectRoot(ObjectProperty<Path> projectRootProperty) {
    SystemUtils.chooseDirectory(projectRootProperty);
  }

  public void onOpenProject() {
    Stage stage = (Stage) btnOpenProject.getScene().getWindow();
    stage.close();
  }
}
