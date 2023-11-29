package fxui;

import common.IO;
import common.Util;
import fxui.model.AppState;
import fxui.model.RunMode;
import fxui.model.Terminal;
import fxui.tree.JavaProjectTree;
import fxui.util.BindingUtils;
import fxui.util.RecursiveDirectoryWatcher;
import fxui.util.SystemUtils;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class AppController implements RecursiveDirectoryWatcher.FileEventListener {

  private Stage applicationStage;

  @FXML
  private TreeView<File> treeProjectDir;
  @FXML
  private ChoiceBox<RunMode> cbRunMode;
  @FXML
  private VBox boxSourcesDir;
  @FXML
  private TextField txtSourcesDir;
  @FXML
  private Button btnClearSourcesDir;
  @FXML
  private VBox boxMainFile;
  @FXML
  private TextField txtMainFile;
  @FXML
  private Label requiredHintMainFile;
  @FXML
  private Button btnClearMainFile;
  @FXML
  private VBox boxProgramArgs;
  @FXML
  private TextField txtProgramArgs;
  @FXML
  private HBox boxSyncCounters;
  @FXML
  private CheckBox cbSyncCounters;
  @FXML
  private ChoiceBox<Terminal> cbTerminalEmulator;
  @FXML
  private TextField txtJavaVersion;
  @FXML
  private Button btnCommandPreview;
  @FXML
  private Button btnOpenReport;
  @FXML
  private Button btnRunTool;
  @FXML
  private Button btnRestoreParameters;

  private final AppState appState;

  private JavaProjectTree projectTree;
  private RecursiveDirectoryWatcher recursiveDirectoryWatcher;

  private final static String JAVA_VERSION_NOT_RECOGNIZED = "Unable to determine";

  public AppController() {
    appState = new AppState();
  }

  @FXML
  private void initialize() {
    bindParameters();
    initRunModeControl();
    initTerminalEmulatorControl();
    initDisabledPropertiesByMode();
    initButtonProperties();
    initRunToolButton();
    initBorderListeners();
    initRecognizedJavaVersionControl();
  }

  void initUI(Stage stage) {
    applicationStage = stage;
    applicationStage.setResizable(false);
    applicationStage.setTitle("Java Profiler");
  }

  public void setProjectDirectory(Path projectRootPath) {
    IO.outputDir = projectRootPath.resolve(IO.outputDir);
    appState.projectRoot.set(projectRootPath);
    applicationStage.setTitle(applicationStage.getTitle() + " - " + projectRootPath);
    projectTree = new JavaProjectTree(appState, treeProjectDir);
    initDirectoryWatcher(projectRootPath);
  }

  private void initDirectoryWatcher(Path projectRootPath) {
    try {
      recursiveDirectoryWatcher = new RecursiveDirectoryWatcher(this, projectRootPath, IO.getOutputDir(), IO.getReportDir());
      Thread watcherThread = new Thread(() -> recursiveDirectoryWatcher.processEvents());
      watcherThread.setDaemon(true);
      watcherThread.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void bindParameters() {
    txtMainFile.textProperty().bindBidirectional(appState.mainFile, BindingUtils.pathStringConverter);
    txtProgramArgs.textProperty().bindBidirectional(appState.programArgs);
    txtSourcesDir.textProperty().bindBidirectional(appState.sourcesDir, BindingUtils.pathStringConverter);
    cbSyncCounters.selectedProperty().bindBidirectional(appState.syncCounters);
  }

  private void initRunModeControl() {
    cbRunMode.getItems().setAll(RunMode.values());
    cbRunMode.valueProperty().bindBidirectional(appState.runMode);
  }

  private void initTerminalEmulatorControl() {
    cbTerminalEmulator.getItems().setAll(Terminal.getSystemTerminalOptions());
    cbTerminalEmulator.valueProperty().bindBidirectional(appState.terminal);
  }

  private void initDisabledPropertiesByMode() {
    boxMainFile.visibleProperty().bind(appState.runMode.isNotEqualTo(RunMode.REPORT_ONLY));
    boxProgramArgs.visibleProperty().bind(appState.runMode.isEqualTo(RunMode.DEFAULT));
    boxProgramArgs.managedProperty().bind(boxProgramArgs.visibleProperty());
    boxSourcesDir.visibleProperty().bind(appState.runMode.isNotEqualTo(RunMode.REPORT_ONLY));
    boxSyncCounters.visibleProperty().bind(appState.runMode.isNotEqualTo(RunMode.REPORT_ONLY));
    requiredHintMainFile.visibleProperty().bind(appState.runMode.isEqualTo(RunMode.DEFAULT));
    requiredHintMainFile.managedProperty().bind(requiredHintMainFile.visibleProperty());
  }

  private void initButtonProperties() {
    btnClearSourcesDir.visibleProperty().bind(appState.sourcesDir.isNotNull());
    btnClearSourcesDir.managedProperty().bind(btnClearSourcesDir.visibleProperty());
    btnClearMainFile.visibleProperty().bind(appState.mainFile.isNotNull());
    btnClearMainFile.managedProperty().bind(btnClearMainFile.visibleProperty());
    btnRestoreParameters.visibleProperty().bind(appState.parametersFileExists);
    btnOpenReport.visibleProperty().bind(appState.reportIndexFileExists);
  }

  private void initRunToolButton() {
    BooleanBinding anyPathInvalid = appState.invalidMainFilePath.or(appState.invalidSourcesDirPath);
    BooleanBinding instrumentWithoutTarget = appState.runMode
        .isNotEqualTo(RunMode.REPORT_ONLY)
        .and(appState.mainFile.isNull())
        .and(appState.sourcesDir.isNull());
    BooleanBinding runWithoutMainFile = appState.runMode
        .isEqualTo(RunMode.DEFAULT)
        .and(appState.mainFile.isNull());
    BooleanBinding generateWithMissingData = appState.runMode
        .isEqualTo(RunMode.REPORT_ONLY)
        .and(appState.metadataFileExists.not().or(appState.countsFileExists.not()));
    btnRunTool.disableProperty().bind(
        anyPathInvalid.or(instrumentWithoutTarget).or(runWithoutMainFile).or(generateWithMissingData)
    );
    btnCommandPreview.disableProperty().bind(btnRunTool.disableProperty());
  }

  private void initBorderListeners() {
    txtMainFile.borderProperty().bind(BindingUtils.createBorderBinding(appState.mainFile, appState.invalidMainFilePath));
    txtSourcesDir.borderProperty().bind(BindingUtils.createBorderBinding(appState.sourcesDir, appState.invalidSourcesDirPath));
  }

  public void onClearSourcesDir() {
    appState.sourcesDir.set(null);
  }

  public void onClearMainFile() {
    appState.mainFile.set(null);
  }

  @FXML
  protected void onExecuteTool() {
    int exitCode = SystemUtils.executeToolWithParameters(appState);
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
  protected void showRunCommand() throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(ProjectController.class.getResource("command-view.fxml"));
    Stage cmdStage = new Stage();
    cmdStage.setScene(new Scene(fxmlLoader.load()));
    cmdStage.initOwner(applicationStage);
    cmdStage.initModality(Modality.APPLICATION_MODAL);
    CommandController cmdController = fxmlLoader.getController();
    cmdController.initUI(cmdStage);
    cmdController.setRunCommand(SystemUtils.getJavaRunCommand(appState.getProgramArguments()));
    cmdStage.showAndWait();
  }

  private void initRecognizedJavaVersionControl() {
    Platform.runLater(() -> {
      String[] command = new String[]{"java", "-version"};
      String result = JAVA_VERSION_NOT_RECOGNIZED;
      try {
        String[] output = Util.runCommandAndGetOutput(appState.projectRoot.get(), command);
        if (output.length > 0) {
          result = output[0];
        }
      } catch (Exception ignored) {
      }
      txtJavaVersion.textProperty().set(result);
    });
  }

  @FXML
  protected void onRebuildTree() {
    projectTree = new JavaProjectTree(appState, treeProjectDir);
  }

  @FXML
  protected void onSaveParameters() {
    appState.exportParameters();
  }

  @FXML
  protected void onRestoreParameters() {
    appState.importParameters();
  }

  @Override
  public void onFileCreated(Path path) {
    appState.invalidateFileBindings();
  }

  @Override
  public void onFileModified(Path path) {
  }

  @Override
  public void onFileDeleted(Path path) {
    appState.invalidateFileBindings();
  }
}