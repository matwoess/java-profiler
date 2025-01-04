module fxui {
  requires javafx.controls;
  requires javafx.fxml;
  requires atlantafx.base;
  requires java.desktop;
  requires common;
  requires tool;


  opens javaprofiler.fxui to javafx.fxml;
  exports javaprofiler.fxui;
  exports javaprofiler.fxui.tree;
  exports javaprofiler.fxui.model;
  opens javaprofiler.fxui.tree to javafx.fxml;
}