package fxui;

import common.IO;
import fxui.model.RunMode;
import fxui.util.SystemOutputTextFlowWriter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.awt.Desktop;

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
  private TextFlow txtFlowOutput;

  private final ToggleGroup toggleGroup = new ToggleGroup();

  @FXML
  private void initialize() {
    {
      rbDefaultMode.setToggleGroup(toggleGroup);
      rbDefaultMode.setUserData(RunMode.DEFAULT);
      rbInstrumentOnly.setToggleGroup(toggleGroup);
      rbInstrumentOnly.setUserData(RunMode.INSTRUMENT_ONLY);
      rbReportOnly.setToggleGroup(toggleGroup);
      rbReportOnly.setUserData(RunMode.REPORT_ONLY);
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
    {
      PrintStream consoleOutput = new PrintStream(new SystemOutputTextFlowWriter(txtFlowOutput));
      System.setOut(consoleOutput);
      System.setErr(consoleOutput);
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
    txtFlowOutput.getChildren().clear();
    RunMode runMode = (RunMode) toggleGroup.getSelectedToggle().getUserData();
    List<String> arguments = new ArrayList<>();
    String outDir = txtOutputDir.textProperty().get();
    if (!outDir.isBlank()) {
      arguments.add("--out-directory");
      arguments.add(outDir);
    }
    boolean verbose = cbVerboseOutput.selectedProperty().get();
    if (verbose) {
      arguments.add("--verbose");
    }
    boolean syncCounters = cbSyncCounters.selectedProperty().get();
    if (syncCounters && runMode != RunMode.REPORT_ONLY) {
      arguments.add("--sync-counters");
    }
    switch (runMode) {
      case REPORT_ONLY -> {
        arguments.add("--generate-report");
      }
      case INSTRUMENT_ONLY -> {
        arguments.add("--instrument-only");
        arguments.add(txtMainFile.textProperty().get());
      }
      case DEFAULT -> {
        String sourcesDir = txtSourcesDir.textProperty().get();
        if (!sourcesDir.isBlank()) {
          arguments.add("--sources-directory");
          arguments.add(sourcesDir);
        }
        arguments.add(txtMainFile.textProperty().get());
        String programArgs = txtProgramArgs.textProperty().get();
        if (!programArgs.isBlank()) {
          arguments.addAll(Arrays.stream(programArgs.split(" ")).toList());
        }
      }
    }
    tool.Main.main(arguments.toArray(String[]::new));
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