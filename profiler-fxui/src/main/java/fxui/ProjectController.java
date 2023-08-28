package fxui;

import fxui.util.BindingUtils;
import fxui.util.SystemUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ProjectController {

  public Button btnProjectRoot;
  public TextField txtProjectRoot;
  public Button btnOpenProject;
  public BooleanProperty invalidProjectRootPath = new SimpleBooleanProperty(false);

  void bindProjectRootProperty(StringProperty projectRoot) {
    txtProjectRoot.textProperty().bindBidirectional(projectRoot);
    invalidProjectRootPath.bind(txtProjectRoot.textProperty().isNotEmpty()
        .and(BindingUtils.createIsDirectoryBinding(txtProjectRoot.textProperty()).not())
    );
    btnProjectRoot.setOnAction(event -> SystemUtils.chooseDirectory(txtProjectRoot));
    txtProjectRoot.borderProperty().bind(BindingUtils.createBorderBinding(txtProjectRoot.textProperty(), invalidProjectRootPath));
    btnOpenProject.disableProperty().bind(invalidProjectRootPath);
  }

  public void onOpenProject() {
    Stage stage = (Stage) btnOpenProject.getScene().getWindow();
    stage.close();
  }
}
