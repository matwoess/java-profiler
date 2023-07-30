module app {
  requires javafx.controls;
  requires javafx.fxml;
  requires atlantafx.base;
  requires tool;


  opens app to javafx.fxml;
  exports app;
}