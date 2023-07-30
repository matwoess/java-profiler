package app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;

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
  private TextField txtMainFile;
  @FXML
  private Button btnMainFile;
  @FXML
  private HBox hbProgramArgs;
  @FXML
  private HBox hbSourcesDir;
  @FXML
  private TextField txtSourcesDir;
  @FXML
  private Button btnSourcesDir;
  @FXML
  private TextField txtOutputDir;
  @FXML
  private Button btnOutputDir;
  @FXML
  private CheckBox cbSyncCounters;
  @FXML
  private TextArea txtAreaOutput;

  @FXML
  private void initialize() {
    {
      ToggleGroup toggleGroup = new ToggleGroup();
      rbDefaultMode.setToggleGroup(toggleGroup);
      rbInstrumentOnly.setToggleGroup(toggleGroup);
      rbReportOnly.setToggleGroup(toggleGroup);
    }
    {
      hbMainFile.visibleProperty().bind(rbReportOnly.selectedProperty().not());
      hbProgramArgs.visibleProperty().bind(rbDefaultMode.selectedProperty());
      hbSourcesDir.visibleProperty().bind(rbDefaultMode.selectedProperty());
      cbSyncCounters.visibleProperty().bind(rbReportOnly.selectedProperty().not());
    }
    {
      btnMainFile.setOnAction(event -> chooseFile(txtMainFile));
      btnSourcesDir.setOnAction(event -> chooseDirectory(txtSourcesDir));
      btnOutputDir.setOnAction(event -> chooseDirectory(txtOutputDir));
    }
  }

  public void chooseFile(TextField pathField) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Choose File");
    File file = fileChooser.showOpenDialog(pathField.getScene().getWindow());
    if (file != null) {
      pathField.setText(file.toString());
    }
  }

  public void chooseDirectory(TextField pathField) {
    DirectoryChooser dirChooser = new DirectoryChooser();
    dirChooser.setTitle("Choose Directory");
    File dir = dirChooser.showDialog(pathField.getScene().getWindow());
    if (dir != null) {
      pathField.setText(dir.toString());
    }
  }

  @FXML
  protected void onExecuteTool() {
    String[] arguments = {"-h"};
    tool.Main.main(arguments);
    txtAreaOutput.setText("Profiling now.\nHere's the output.");
  }
}