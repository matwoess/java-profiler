package fxui.tree;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.Objects;

class SelectableTreeCell extends TreeCell<File> {

  static final Image folderIcon = new Image(Objects.requireNonNull(JavaProjectTree.class.getResourceAsStream("folder-icon.png")));
  static final Image jFileIcon = new Image(Objects.requireNonNull(JavaProjectTree.class.getResourceAsStream("java-icon.png")));

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
