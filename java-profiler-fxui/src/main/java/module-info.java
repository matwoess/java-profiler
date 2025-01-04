module javaprofiler.fxui {
  requires javafx.controls;
  requires javafx.fxml;
  requires atlantafx.base;
  requires java.desktop;
  requires javaprofiler.common;
  requires javaprofiler.tool;


  opens javaprofiler.fxui to javafx.fxml;
  exports javaprofiler.fxui;
  exports javaprofiler.fxui.tree;
  exports javaprofiler.fxui.model;
  opens javaprofiler.fxui.tree to javafx.fxml;
}