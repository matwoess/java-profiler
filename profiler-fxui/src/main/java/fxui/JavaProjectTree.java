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

  static Image folderIcon = new Image(Objects.requireNonNull(JavaProjectTree.class.getResourceAsStream("folder-icon.png")));
  static Image jFileIcon = new Image(Objects.requireNonNull(JavaProjectTree.class.getResourceAsStream("java-icon.png")));

  ObjectProperty<TreeItem<File>> selectedDirNode = new SimpleObjectProperty<>();
  ObjectProperty<TreeItem<File>> selectedMainNode = new SimpleObjectProperty<>();

  public JavaProjectTree(Parameters parameters, TreeView<File> treeProjectDir) {
    this.parameters = parameters;
    initTreeView(treeProjectDir);
  }

  private void initTreeView(TreeView<File> treeProjectDir) {
    File rootDir = parameters.projectRoot.get().toFile();
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
    Path relPath = parameters.projectRoot.get().relativize(dir);
    parameters.sourcesDir.set(relPath);
  }

  private void setMainFile(Path jFile) {
    Path relPath = parameters.projectRoot.get().relativize(jFile);
    parameters.mainFile.set(relPath);
  }

  static class SelectableTreeCell extends TreeCell<File> {

    public SelectableTreeCell(ObjectProperty<TreeItem<File>> selectedDir, ObjectProperty<TreeItem<File>> selectedMain) {
      BooleanBinding isSelectedDir = Bindings.createBooleanBinding(
          () -> selectedDir.isNotNull().get() && selectedDir.get().equals(getTreeItem()),
          treeItemProperty(), selectedDir
      );
      BooleanBinding isSelectedMain = Bindings.createBooleanBinding(
          () -> selectedMain.isNotNull().get() && selectedMain.get().equals(getTreeItem()),
          treeItemProperty(), selectedMain
      );
      graphicProperty().bind(Bindings.createObjectBinding(
              () -> {
                if (itemProperty().isNull().get()) {
                  return null;
                }
                return itemProperty().get().isDirectory() ? new ImageView(folderIcon) : new ImageView(jFileIcon);
              },
              itemProperty()
          )
      );
      backgroundProperty().bind(Bindings.createObjectBinding(
          () -> {
            if (isSelectedDir.get()) {
              if (isSelected()) {
                return Background.fill(Color.color(.1, .2, .8, .3));
              } else {
                return Background.fill(Color.color(.1, .2, .8, .2));
              }
            }
            if (isSelectedMain.get()) {
              if (isSelected()) {
                return Background.fill(Color.color(.1, .5, .1, .3));
              } else {
                return Background.fill(Color.color(.1, .5, .1, .2));
              }
            }
            if (selectedProperty().get()) {
              return Background.fill(Color.color(.9, .9, .9, .15));
            }
            return null;
          },
          isSelectedDir, isSelectedMain, selectedProperty()
      ));
    }

    @Override
    protected void updateItem(File item, boolean empty) {
      super.updateItem(item, empty);
      setText((empty || item == null) ? "" : item.getName());
    }
  }

}
