module fxui {
  requires javafx.controls;
  requires javafx.fxml;
  requires atlantafx.base;
  requires tool;
  requires java.desktop;
  requires common;


  opens fxui to javafx.fxml;
  exports fxui;
}