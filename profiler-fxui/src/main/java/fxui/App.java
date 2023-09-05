package fxui;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

public class App extends Application {
  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
    ObjectProperty<Path> projectRootProperty = new SimpleObjectProperty<>();
    showProjectRootDialog(projectRootProperty);
    showMainApp(primaryStage, projectRootProperty);
  }

  private void showProjectRootDialog(ObjectProperty<Path> projectRootProperty) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(ProjectController.class.getResource("project-view.fxml"));
    Scene scene = new Scene(fxmlLoader.load());
    ProjectController prjController = fxmlLoader.getController();
    prjController.initProperties(projectRootProperty);
    Stage projectStage = new Stage();
    projectStage.setTitle("Open Java Project");
    projectStage.setScene(scene);
    projectStage.setOnCloseRequest((e) -> {
      Platform.exit();
      System.exit(0);
    });
    projectStage.showAndWait();
  }

  private void showMainApp(Stage stage, ObjectProperty<Path> projectRootProperty) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("app-view.fxml"));
    Scene scene = new Scene(fxmlLoader.load());
    stage.setTitle("Java Profiler");
    stage.setScene(scene);
    Controller controller = fxmlLoader.getController();
    controller.setProjectDirectory(projectRootProperty.get(), stage);
    stage.show();
  }
}