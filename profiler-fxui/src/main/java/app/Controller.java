package app;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;

public class Controller {
  @FXML
  private TextArea programOutput;
  @FXML
  private RadioButton rbDefaultMode;
  @FXML
  private RadioButton rbInstrumentOnly;
  @FXML
  private RadioButton rbReportOnly;


  @FXML
  private void initialize() {
    ToggleGroup toggleGroup = new ToggleGroup();
    rbDefaultMode.setToggleGroup(toggleGroup);
    rbInstrumentOnly.setToggleGroup(toggleGroup);
    rbReportOnly.setToggleGroup(toggleGroup);
  }

  @FXML
  protected void onExecuteTool() {
    programOutput.setText("Profiling now.");
  }
}