package fxui;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

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
    Stage projectStage = new Stage();
    projectStage.getIcons().add(new Image(Objects.requireNonNull(App.class.getResourceAsStream("logo.png"))));
    projectStage.setScene(new Scene(fxmlLoader.load()));
    ProjectController prjController = fxmlLoader.getController();
    prjController.initProperties(projectRootProperty);
    prjController.initUI(projectStage);
    projectStage.setOnCloseRequest((e) -> {
      Platform.exit();
      System.exit(0);
    });
    projectStage.showAndWait();
  }

  private void showMainApp(Stage primaryStage, ObjectProperty<Path> projectRootProperty) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("app-view.fxml"));
    primaryStage.getIcons().add(new Image(Objects.requireNonNull(App.class.getResourceAsStream("logo.png"))));
    primaryStage.setScene(new Scene(fxmlLoader.load()));
    AppController appController = fxmlLoader.getController();
    appController.initUI(primaryStage);
    appController.setProjectDirectory(projectRootProperty.get());
    primaryStage.show();
  }
}