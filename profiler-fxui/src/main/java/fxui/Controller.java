package fxui;

import common.IO;
import fxui.model.Parameters;
import fxui.model.RunMode;
import fxui.util.BindingUtils;
import fxui.util.SystemUtils;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

public class Controller {

  @FXML
  private TextField txtProjectRoot;
  @FXML
  private ChoiceBox<RunMode> cbRunMode;
  @FXML
  private VBox boxMainFile;
  @FXML
  private TextField txtMainFile;
  @FXML
  private Button btnMainFile;
  @FXML
  private VBox boxProgramArgs;
  @FXML
  private TextField txtProgramArgs;
  @FXML
  private VBox boxSourcesDir;
  @FXML
  private TextField txtSourcesDir;
  @FXML
  private Button btnSourcesDir;
  @FXML
  private TextField txtOutputDir;
  @FXML
  private Button btnOutputDir;
  @FXML
  private HBox boxSyncCounters;
  @FXML
  private CheckBox cbSyncCounters;
  @FXML
  private Button btnOpenReport;
  @FXML
  private Button btnRunTool;

  private final Parameters parameters;

  public Controller() {
    parameters = new Parameters();
  }

  @FXML
  private void initialize() throws IOException {
    // project root dialog
    chooseProjectDirectory();
    // setup UI
    bindParameters();
    setOnClickActions();
    initRunModeControl();
    initDisabledPropertiesByMode();
    initButtonDisabledProperties();
    initBorderListeners();
    txtOutputDir.setPromptText(Path.of(".").resolve(IO.DEFAULT_OUT_DIR).toString());
  }

  private void chooseProjectDirectory() throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(ProjectController.class.getResource("project-view.fxml"));
    Scene scene = new Scene(fxmlLoader.load());
    ProjectController prjController = fxmlLoader.getController();
    prjController.bindProjectRootProperty(parameters.projectRoot);
    Stage projectState = new Stage();
    projectState.setTitle("Choose Project Root");
    projectState.setScene(scene);
    projectState.showAndWait();
  }

  private void bindParameters() {
    txtProjectRoot.textProperty().bindBidirectional(parameters.projectRoot);
    txtMainFile.textProperty().bindBidirectional(parameters.mainFile);
    txtProgramArgs.textProperty().bindBidirectional(parameters.programArgs);
    txtSourcesDir.textProperty().bindBidirectional(parameters.sourcesDir);
    txtOutputDir.textProperty().bindBidirectional(parameters.outputDir);
    cbSyncCounters.selectedProperty().bindBidirectional(parameters.syncCounters);
  }

  private void setOnClickActions() {
    btnMainFile.setOnAction(event -> SystemUtils.chooseFile(txtMainFile));
    btnSourcesDir.setOnAction(event -> SystemUtils.chooseDirectory(txtSourcesDir));
    btnOutputDir.setOnAction(event -> SystemUtils.chooseDirectory(txtOutputDir));
  }

  private void initRunModeControl() {
    cbRunMode.getItems().setAll(RunMode.values());
    cbRunMode.valueProperty().bindBidirectional(parameters.runMode);
  }

  private void initDisabledPropertiesByMode() {
    boxMainFile.disableProperty().bind(parameters.runMode.isEqualTo(RunMode.REPORT_ONLY));
    boxProgramArgs.disableProperty().bind(parameters.runMode.isNotEqualTo(RunMode.DEFAULT));
    boxSourcesDir.disableProperty().bind(parameters.runMode.isEqualTo(RunMode.REPORT_ONLY));
    boxSyncCounters.disableProperty().bind(parameters.runMode.isEqualTo(RunMode.REPORT_ONLY));
  }

  private void initButtonDisabledProperties() {
    btnOpenReport.disableProperty().bindBidirectional(parameters.invalidOutDirPath);
    BooleanBinding anyPathInvalid = parameters.invalidMainFilePath
        .or(parameters.invalidSourcesDirPath)
        .or(parameters.invalidOutDirPath);
    BooleanBinding instrumentWithoutTarget = parameters.runMode
        .isNotEqualTo(RunMode.REPORT_ONLY)
        .and(parameters.mainFile.isEmpty())
        .and(parameters.sourcesDir.isEmpty());
    btnRunTool.disableProperty().bind(anyPathInvalid.or(instrumentWithoutTarget));
  }

  private void initBorderListeners() {
    txtMainFile.borderProperty().bind(BindingUtils.createBorderBinding(parameters.mainFile, parameters.invalidMainFilePath));
    txtSourcesDir.borderProperty().bind(BindingUtils.createBorderBinding(parameters.sourcesDir, parameters.invalidSourcesDirPath));
    txtOutputDir.borderProperty().bind(BindingUtils.createBorderBinding(parameters.outputDir, parameters.invalidOutDirPath));
  }

  @FXML
  protected void onExecuteTool() {
    int exitCode = SystemUtils.executeToolInTerminal(parameters.getRunParameters());
    if (exitCode != 0) {
      throw new RuntimeException("error executing tool");
    }
  }

  @FXML
  protected void onOpenReport() {
    Path reportPath = IO.getReportIndexPath();
    SystemUtils.openWithDesktopApplication(reportPath);
  }

  @FXML
  protected void onSaveParameters() {
    parameters.exportParameters();
  }

  @FXML
  protected void onRestoreParameters() {
    parameters.importParameters();
  }
}