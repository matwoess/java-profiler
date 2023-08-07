package fxui;

import common.IO;
import fxui.model.Parameters;
import fxui.model.RunMode;
import fxui.util.SystemOutputTextFlowWriter;
import javafx.beans.binding.Binding;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

public class Controller {
  private static final Border invalidBorder = new Border(new BorderStroke(Color.RED, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT));
  private static final Border validBorder = new Border(new BorderStroke(Color.GREEN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
  private static final Border neutralBorder = null;
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
  private TextField txtProgramArgs;
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
  private CheckBox cbVerboseOutput;
  @FXML
  private Button btnOpenReport;
  @FXML
  private Button btnRunTool;
  @FXML
  private TextFlow txtFlowOutput;

  private final Parameters parameters;

  public Controller() {
    parameters = new Parameters();
  }

  private final ToggleGroup toggleGroup = new ToggleGroup();

  @FXML
  private void initialize() {
    bindParameters();
    setOnClickActions();
    initConsoleOutput();
    initRunModeControl();
    initRunModeVisibilities();
    initButtonDisabledProperties();
    initBorderListeners();
  }

  private void bindParameters() {
    txtMainFile.textProperty().bindBidirectional(parameters.mainFile);
    txtProgramArgs.textProperty().bindBidirectional(parameters.programArgs);
    txtSourcesDir.textProperty().bindBidirectional(parameters.sourcesDir);
    txtOutputDir.textProperty().bindBidirectional(parameters.outputDir);
    cbVerboseOutput.selectedProperty().bindBidirectional(parameters.verboseOutput);
    cbSyncCounters.selectedProperty().bindBidirectional(parameters.syncCounters);
  }

  private void setOnClickActions() {
    btnMainFile.setOnAction(event -> chooseFile(txtMainFile));
    btnSourcesDir.setOnAction(event -> chooseDirectory(txtSourcesDir));
    btnOutputDir.setOnAction(event -> chooseDirectory(txtOutputDir));
  }

  private void initRunModeControl() {
    rbDefaultMode.setToggleGroup(toggleGroup);
    rbInstrumentOnly.setToggleGroup(toggleGroup);
    rbReportOnly.setToggleGroup(toggleGroup);
    rbDefaultMode.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) parameters.runMode.set(RunMode.DEFAULT);
    });
    rbInstrumentOnly.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) parameters.runMode.set(RunMode.INSTRUMENT_ONLY);
    });
    rbReportOnly.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) parameters.runMode.set(RunMode.REPORT_ONLY);
    });
  }

  private void initConsoleOutput() {
    PrintStream consoleOutput = new PrintStream(new SystemOutputTextFlowWriter(txtFlowOutput));
    System.setOut(consoleOutput);
    System.setErr(consoleOutput);
  }

  private void initRunModeVisibilities() {
    hbMainFile.visibleProperty().bind(rbReportOnly.selectedProperty().not());
    hbProgramArgs.visibleProperty().bind(rbDefaultMode.selectedProperty());
    hbSourcesDir.visibleProperty().bind(rbDefaultMode.selectedProperty());
    cbSyncCounters.visibleProperty().bind(rbReportOnly.selectedProperty().not());
  }

  private void initButtonDisabledProperties() {
    btnOpenReport.disableProperty().bindBidirectional(parameters.invalidOutDirPath);
    Binding<Boolean> anyPathInvalid = parameters.invalidMainFilePath
        .or(parameters.invalidSourcesDirPath)
        .or(parameters.invalidOutDirPath);
    btnRunTool.disableProperty().bind(anyPathInvalid);
  }

  private void initBorderListeners() {
    txtMainFile.textProperty().addListener((observable, oldValue, newValue) -> {
      Border border = newValue.isBlank() ? neutralBorder : parameters.invalidMainFilePath.get() ? invalidBorder : validBorder;
      txtMainFile.setBorder(border);
    });
    txtSourcesDir.textProperty().addListener((observable, oldValue, newValue) -> {
      Border border = newValue.isBlank() ? neutralBorder : parameters.invalidSourcesDirPath.get() ? invalidBorder : validBorder;
      txtSourcesDir.setBorder(border);
    });
    txtOutputDir.textProperty().addListener((observable, oldValue, newValue) ->{
      Border border = newValue.isBlank() ? neutralBorder : parameters.invalidOutDirPath.get() ? invalidBorder : validBorder;
      txtOutputDir.setBorder(border);
    });
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
    txtFlowOutput.getChildren().clear();
    tool.Main.main(parameters.getRunCommand());
  }

  @FXML
  protected void onOpenReport() {
    Path reportPath = IO.getReportIndexPath();
    SwingUtilities.invokeLater(() -> {
      try {
        Desktop.getDesktop().open(reportPath.toFile());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
}