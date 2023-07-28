package app;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

public class Controller {
  @FXML
  private RadioButton rbDefaultMode;
  @FXML
  private RadioButton rbInstrumentOnly;
  @FXML
  private RadioButton rbReportOnly;
  @FXML
  private HBox hbMainFile;
  @FXML
  private HBox hbProgramArgs;
  @FXML
  private HBox hbSourcesDir;
  @FXML
  private CheckBox cbSyncCounters;
  @FXML
  private TextArea txtAreaOutput;


  @FXML
  private void initialize() {
    ToggleGroup toggleGroup = new ToggleGroup();
    rbDefaultMode.setToggleGroup(toggleGroup);
    rbInstrumentOnly.setToggleGroup(toggleGroup);
    rbReportOnly.setToggleGroup(toggleGroup);
    hbMainFile.visibleProperty().bind(rbReportOnly.selectedProperty().not());
    hbProgramArgs.visibleProperty().bind(rbDefaultMode.selectedProperty());
    hbSourcesDir.visibleProperty().bind(rbDefaultMode.selectedProperty());
    cbSyncCounters.visibleProperty().bind(rbReportOnly.selectedProperty().not());

  }

  @FXML
  protected void onExecuteTool() {
    txtAreaOutput.setText("Profiling now.\nHere's the output.");
  }
}