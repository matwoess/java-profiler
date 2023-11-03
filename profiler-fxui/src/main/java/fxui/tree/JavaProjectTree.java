package fxui.tree;

import fxui.model.AppState;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.nio.file.Path;

public class JavaProjectTree {
  private final AppState appState;

  ObjectProperty<TreeItem<File>> selectedDirNode = new SimpleObjectProperty<>();
  ObjectProperty<TreeItem<File>> selectedMainNode = new SimpleObjectProperty<>();

  public JavaProjectTree(AppState appState, TreeView<File> treeProjectDir) {
    this.appState = appState;
    initTreeView(treeProjectDir);
  }

  private void initTreeView(TreeView<File> treeProjectDir) {
    File rootDir = appState.projectRoot.get().toFile();
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
    treeProjectDir.setCellFactory(tv -> new SelectableTreeCell(appState));
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
    Path relPath = appState.projectRoot.get().relativize(dir);
    appState.sourcesDir.set(relPath);
  }

  private void setMainFile(Path jFile) {
    Path relPath = appState.projectRoot.get().relativize(jFile);
    appState.mainFile.set(relPath);
  }

}
