package fxui.tree;

import fxui.model.AppState;
import fxui.util.DirectoryBeforeFileComparator;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

public class JavaProjectTree {
  private static final Comparator<File> treeComparator = new DirectoryBeforeFileComparator();
  private final AppState appState;

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
      if (event.getCode() == KeyCode.ENTER) {
        selectTreeItem(treeProjectDir.getSelectionModel().getSelectedItem());
      }
    });
    initContextMenu(treeProjectDir);
    treeProjectDir.setCellFactory(tv -> new SelectableTreeCell(appState));
  }

  private void initContextMenu(TreeView<File> treeProjectDir) {
    ContextMenu contextMenu = new ContextMenu();
    MenuItem selectItem = new MenuItem();
    selectItem.setOnAction(event -> selectTreeItem(treeProjectDir.getSelectionModel().getSelectedItem()));
    selectItem.textProperty().bind(Bindings.createStringBinding(
        () -> {
          TreeItem<File> item = treeProjectDir.getSelectionModel().getSelectedItem();
          if (item == null) return "";
          return item.getValue().isDirectory() ? "Select as sources root" : "Select as main file";
        },
        treeProjectDir.getSelectionModel().selectedItemProperty()
    ));
    contextMenu.getItems().add(selectItem);
    treeProjectDir.setContextMenu(contextMenu);
  }

  public TreeItem<File> populateTree(File directory) {
    File[] itemsInDir = directory.listFiles();
    TreeItem<File> folder = new TreeItem<>(directory);
    if (itemsInDir == null) return folder;
    Arrays.sort(itemsInDir, treeComparator);
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

  private void selectTreeItem(TreeItem<File> selected) {
    if (selected == null) return;
    File value = selected.getValue();
    Path relPath = appState.projectRoot.get().relativize(value.toPath());
    if (value.isDirectory()) {
      appState.sourcesDir.set(relPath);
    } else {
      appState.mainFile.set(relPath);
    }
  }

}
