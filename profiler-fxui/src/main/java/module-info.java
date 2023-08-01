module fxui {
  requires javafx.controls;
  requires javafx.fxml;
  requires atlantafx.base;
  requires tool;


  opens fxui to javafx.fxml;
  exports fxui;
}