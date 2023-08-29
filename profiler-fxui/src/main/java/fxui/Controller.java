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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class Controller {
  @FXML
  private TreeView<File> treeProjectDir;
  @FXML
  private TextField txtProjectRoot;
  @FXML
  private ChoiceBox<RunMode> cbRunMode;
  @FXML
  private VBox boxSourcesDir;
  @FXML
  private TextField txtSourcesDir;
  @FXML
  private Button btnSourcesDir;
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
  private HBox boxSyncCounters;
  @FXML
  private CheckBox cbSyncCounters;
  @FXML
  private Button btnOpenReport;
  @FXML
  private Button btnRunTool;

  private final Parameters parameters;

  static Image folderIcon = new Image(Objects.requireNonNull(Controller.class.getResourceAsStream("folder-icon.png")));
  static Image jFileIcon = new Image(Objects.requireNonNull(Controller.class.getResourceAsStream("java-icon.png")));

  public Controller() {
    parameters = new Parameters();
  }

  @FXML
  private void initialize() throws IOException {
    // project root dialog
    chooseProjectDirectory();
    // setup UI
    initTreeView();
    bindParameters();
    setOnClickActions();
    initRunModeControl();
    initDisabledPropertiesByMode();
    initButtonDisabledProperties();
    initBorderListeners();
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

  private void initTreeView() {
    File rootDir = Path.of(parameters.projectRoot.get()).toFile();
    TreeItem<File> root = new TreeItem<>(rootDir);
    populateTree(rootDir, root);
    treeProjectDir.setRoot(root);
    treeProjectDir.setShowRoot(false);
    treeProjectDir.setOnKeyPressed(event -> {
      TreeItem<File> selected = treeProjectDir.getSelectionModel().getSelectedItem();
      if (selected != null && event.getCode() == KeyCode.ENTER) {
        if (selected.getValue().isDirectory()) {
          setSourcesDir(selected.getValue().toPath());
        } else {
          setMainFile(selected.getValue().toPath());
        }
      }
    });
    treeProjectDir.setCellFactory(new Callback<>() {
      public TreeCell<File> call(TreeView<File> tv) {
        return new TreeCell<>() {
          @Override
          protected void updateItem(File item, boolean empty) {
            super.updateItem(item, empty);
            setText((empty || item == null) ? "" : item.getName());
            if (empty || item == null) return;
            if (item.isDirectory()) {
              setGraphic(new ImageView(folderIcon));
            } else {
              setGraphic(new ImageView(jFileIcon));
            }
          }
        };
      }
    });
  }

  public void populateTree(File directory, TreeItem<File> parent) {
    File[] itemsInDir = directory.listFiles();
    if (itemsInDir == null) return;
    for (File item : itemsInDir) {
      if (item.isDirectory()) {
        TreeItem<File> dirItem = new TreeItem<>(item);
        parent.getChildren().add(dirItem);
        populateTree(item, dirItem);
      } else if (item.getName().endsWith(".java")) {
        parent.getChildren().add(new TreeItem<>(item));
      }
    }
  }

  private void setSourcesDir(Path dir) {
    Path relPath = Path.of(parameters.projectRoot.get()).relativize(dir);
    txtSourcesDir.textProperty().set(relPath.toString());
  }

  private void setMainFile(Path jFile) {
    Path relPath = Path.of(parameters.projectRoot.get()).relativize(jFile);
    txtMainFile.textProperty().set(relPath.toString());
  }

  private void bindParameters() {
    txtProjectRoot.textProperty().bindBidirectional(parameters.projectRoot);
    txtMainFile.textProperty().bindBidirectional(parameters.mainFile);
    txtProgramArgs.textProperty().bindBidirectional(parameters.programArgs);
    txtSourcesDir.textProperty().bindBidirectional(parameters.sourcesDir);
    cbSyncCounters.selectedProperty().bindBidirectional(parameters.syncCounters);
  }

  private void setOnClickActions() {
    btnMainFile.setOnAction(event -> SystemUtils.chooseFile(txtMainFile, parameters.projectRoot.get()));
    btnSourcesDir.setOnAction(event -> SystemUtils.chooseDirectory(txtSourcesDir, parameters.projectRoot.get()));
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