package fxui;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("app-view.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), 600, 500);
    stage.setTitle("Java Profiler");
    stage.setScene(scene);
    stage.show();
  }
}