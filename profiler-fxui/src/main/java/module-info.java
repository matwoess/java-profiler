module fxui {
  requires javafx.controls;
  requires javafx.fxml;
  requires atlantafx.base;
  requires java.desktop;
  requires common;
  requires tool;


  opens fxui to javafx.fxml;
  exports fxui;
  exports fxui.tree;
  exports fxui.model;
  opens fxui.tree to javafx.fxml;
}