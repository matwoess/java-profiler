package fxui;

import fxui.model.Parameters;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class JavaProjectTree {
  private final Parameters parameters;

  static Image folderIcon = new Image(Objects.requireNonNull(Controller.class.getResourceAsStream("folder-icon.png")));
  static Image jFileIcon = new Image(Objects.requireNonNull(Controller.class.getResourceAsStream("java-icon.png")));

  public JavaProjectTree(Parameters parameters, TreeView<File> treeProjectDir) {
    this.parameters = parameters;
    initTreeView(treeProjectDir);
  }

  private void initTreeView(TreeView<File> treeProjectDir) {
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
    parameters.sourcesDir.set(relPath.toString());
  }

  private void setMainFile(Path jFile) {
    Path relPath = Path.of(parameters.projectRoot.get()).relativize(jFile);
    parameters.mainFile.set(relPath.toString());
  }

}
