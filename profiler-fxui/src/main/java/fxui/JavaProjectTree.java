package fxui;

import fxui.model.Parameters;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class JavaProjectTree {
  private final Parameters parameters;

  static Image folderIcon = new Image(Objects.requireNonNull(Controller.class.getResourceAsStream("folder-icon.png")));
  static Image jFileIcon = new Image(Objects.requireNonNull(Controller.class.getResourceAsStream("java-icon.png")));

  ObjectProperty<TreeItem<File>> selectedDirNode = new SimpleObjectProperty<>();
  ObjectProperty<TreeItem<File>> selectedMainNode = new SimpleObjectProperty<>();

  public JavaProjectTree(Parameters parameters, TreeView<File> treeProjectDir) {
    this.parameters = parameters;
    initTreeView(treeProjectDir);
  }

  private void initTreeView(TreeView<File> treeProjectDir) {
    File rootDir = Path.of(parameters.projectRoot.get()).toFile();
    TreeItem<File> root = populateTree(rootDir);
    treeProjectDir.setRoot(root);
    treeProjectDir.setShowRoot(false);
    treeProjectDir.setOnKeyPressed(event -> {
      TreeItem<File> selected = treeProjectDir.getSelectionModel().getSelectedItem();
      if (selected != null && event.getCode() == KeyCode.ENTER) {
        if (selected.getValue().isDirectory()) {
          setSourcesDir(selected.getValue().toPath());
          selectedDirNode.set(selected);
        } else {
          setMainFile(selected.getValue().toPath());
          selectedMainNode.set(selected);
        }
      }
    });
    treeProjectDir.setCellFactory((tv) -> new SelectableTreeCell(selectedDirNode, selectedMainNode));
  }

  public TreeItem<File> populateTree(File directory) {
    File[] itemsInDir = directory.listFiles();
    TreeItem<File> folder = new TreeItem<>(directory);
    if (itemsInDir == null) return folder;
    for (File item : itemsInDir) {
      if (item.isDirectory()) {
        TreeItem<File> subFolder = populateTree(item);
        if (!subFolder.getChildren().isEmpty()) {
          folder.getChildren().add(subFolder);
        }
      } else if (item.getName().endsWith(".java")) {
        folder.getChildren().add(new TreeItem<>(item));
      }
    }
    return folder;
  }

  private void setSourcesDir(Path dir) {
    Path relPath = Path.of(parameters.projectRoot.get()).relativize(dir);
    parameters.sourcesDir.set(relPath.toString());
  }

  private void setMainFile(Path jFile) {
    Path relPath = Path.of(parameters.projectRoot.get()).relativize(jFile);
    parameters.mainFile.set(relPath.toString());
  }

  static class SelectableTreeCell extends TreeCell<File> {

    private final BooleanBinding isSelectedDir;
    private final BooleanBinding isSelectedMain;


    public SelectableTreeCell(ObjectProperty<TreeItem<File>> selectedDir, ObjectProperty<TreeItem<File>> selectedMain) {
      isSelectedDir = Bindings.createBooleanBinding(
          () -> selectedDir.get() != null && selectedDir.get().equals(getTreeItem()),
          treeItemProperty(), selectedDir
      );
      isSelectedDir.addListener((obs, wasSelected, nowSelected) -> {
            setBackground(nowSelected ? Background.fill(Color.color(.1, .1, 1, .4)) : null);
          }
      );
      isSelectedMain = Bindings.createBooleanBinding(
          () -> selectedMain.get() != null && selectedMain.get().equals(getTreeItem()),
          treeItemProperty(), selectedMain
      );
      isSelectedMain.addListener((obs, wasSelected, nowSelected) -> {
        setBackground(nowSelected ? Background.fill(Color.color(.1, .1, 1, .4)) : null);
          }
      );
    }


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
  }

}
