package fxui;

import common.IO;
import fxui.model.Parameters;
import fxui.model.RunMode;
import fxui.tree.JavaProjectTree;
import fxui.util.BindingUtils;
import fxui.util.RecursiveDirectoryWatcher;
import fxui.util.SystemUtils;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class AppController implements RecursiveDirectoryWatcher.FileEventListener {
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
  private Button btnOpenReport;
  @FXML
  private Button btnRunTool;
  @FXML
  private Button btnRestoreParameters;

  private final Parameters parameters;

  private JavaProjectTree projectTree;
  private RecursiveDirectoryWatcher recursiveDirectoryWatcher;

  public AppController() {
    parameters = new Parameters();
  }

  @FXML
  private void initialize() {
    bindParameters();
    initRunModeControl();
    initDisabledPropertiesByMode();
    initButtonDisabledProperties();
    initBorderListeners();
  }

  public void setProjectDirectory(Path projectRootPath, Stage stage) {
    parameters.projectRoot.set(projectRootPath);
    IO.outputDir = projectRootPath.resolve(IO.DEFAULT_OUT_DIR);
    stage.setTitle(stage.getTitle() + " - " + projectRootPath);
    btnRestoreParameters.visibleProperty().set(IO.getUIParametersPath().toFile().exists());
    btnOpenReport.visibleProperty().set(IO.getReportIndexPath().toFile().exists());
    projectTree = new JavaProjectTree(parameters, treeProjectDir);
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
    txtMainFile.textProperty().bindBidirectional(parameters.mainFile, BindingUtils.pathStringConverter);
    txtProgramArgs.textProperty().bindBidirectional(parameters.programArgs);
    txtSourcesDir.textProperty().bindBidirectional(parameters.sourcesDir, BindingUtils.pathStringConverter);
    cbSyncCounters.selectedProperty().bindBidirectional(parameters.syncCounters);
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
    requiredHintMainFile.visibleProperty().bind(parameters.runMode.isEqualTo(RunMode.DEFAULT));
    requiredHintMainFile.managedProperty().bind(requiredHintMainFile.visibleProperty());
  }

  private void initButtonDisabledProperties() {
    btnClearSourcesDir.visibleProperty().bind(parameters.sourcesDir.isNotNull());
    btnClearSourcesDir.managedProperty().bind(btnClearSourcesDir.visibleProperty());
    btnClearMainFile.visibleProperty().bind(parameters.mainFile.isNotNull());
    btnClearMainFile.managedProperty().bind(btnClearMainFile.visibleProperty());
    BooleanBinding anyPathInvalid = parameters.invalidMainFilePath.or(parameters.invalidSourcesDirPath);
    BooleanBinding instrumentWithoutTarget = parameters.runMode
        .isNotEqualTo(RunMode.REPORT_ONLY)
        .and(parameters.mainFile.isNull())
        .and(parameters.sourcesDir.isNull());
    BooleanBinding runWithoutMainFile = parameters.runMode
        .isEqualTo(RunMode.DEFAULT)
        .and(parameters.mainFile.isNull());
    btnRunTool.disableProperty().bind(anyPathInvalid.or(instrumentWithoutTarget).or(runWithoutMainFile));
  }

  private void initBorderListeners() {
    txtMainFile.borderProperty().bind(BindingUtils.createBorderBinding(parameters.mainFile, parameters.invalidMainFilePath));
    txtSourcesDir.borderProperty().bind(BindingUtils.createBorderBinding(parameters.sourcesDir, parameters.invalidSourcesDirPath));
  }

  public void onClearSourcesDir() {
    parameters.sourcesDir.set(null);
  }

  public void onClearMainFile() {
    parameters.mainFile.set(null);
  }

  @FXML
  protected void onExecuteTool() {
    int exitCode = SystemUtils.executeToolInTerminal(parameters.projectRoot.get(), parameters.getRunParameters());
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

  @Override
  public void onFileCreated(Path path) {
    if (path.equals(IO.getReportIndexPath())) {
      btnOpenReport.setVisible(true);
    } else if (path.equals(IO.getReportDir())) { // needed because child event might not be reported
      btnOpenReport.setVisible(IO.getReportIndexPath().toFile().exists());
    } else if (path.equals(IO.getUIParametersPath())) {
      btnRestoreParameters.setVisible(true);
    } else if (path.equals(IO.getOutputDir())) { // needed because child event might not be reported
      btnRestoreParameters.setVisible(IO.getUIParametersPath().toFile().exists());
    }
  }

  @Override
  public void onFileModified(Path path) {
  }

  @Override
  public void onFileDeleted(Path path) {
    if (path.equals(IO.getReportIndexPath()) || IO.isChildPath(IO.getReportIndexPath(), path)) {
      btnOpenReport.setVisible(false);
    }
    if (path.equals(IO.getUIParametersPath()) || IO.isChildPath(IO.getUIParametersPath(), path)) {
      btnRestoreParameters.setVisible(false);
    }
  }
}