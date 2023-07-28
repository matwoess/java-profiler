module app {
  requires javafx.controls;
  requires javafx.fxml;
  requires atlantafx.base;


  opens app to javafx.fxml;
  exports app;
}