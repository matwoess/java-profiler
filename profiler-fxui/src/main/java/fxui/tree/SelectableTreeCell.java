package fxui.tree;

import common.IO;
import fxui.model.AppState;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

class SelectableTreeCell extends TreeCell<File> {

  static final Image folderIcon = new Image(Objects.requireNonNull(JavaProjectTree.class.getResourceAsStream("folder-icon.png")));
  static final Image jFileIcon = new Image(Objects.requireNonNull(JavaProjectTree.class.getResourceAsStream("java-icon.png")));

  static final Background selectedItemColor = Background.fill(Color.color(.9, .9, .9, .15));
  static final Background outDirColor = Background.fill(Color.color(.9, .4, .1, .2));
  static final Background outDirSelColor = Background.fill(Color.color(.9, .4, .1, .3));
  static final Background srcDirColor = Background.fill(Color.color(.1, .2, .8, .2));
  static final Background srcDirSelColor = Background.fill(Color.color(.1, .2, .8, .3));
  static final Background mainFileColor = Background.fill(Color.color(.1, .5, .1, .2));
  static final Background mainFileSelColor = Background.fill(Color.color(.1, .5, .1, .3));


  public SelectableTreeCell(AppState appState) {
    BooleanBinding isSelectedDir = Bindings.createBooleanBinding(
        () -> {
          Path srcDir = appState.sourcesDir.get();
          if (srcDir == null) return false;
          TreeItem<File> item = this.getTreeItem();
          if (item == null) return false;
          File dir = item.getValue();
          if (dir == null) return false;
          return appState.projectRoot.get().resolve(srcDir).equals(dir.toPath());
        },
        treeItemProperty(), appState.sourcesDir
    );
    BooleanBinding isSelectedMain = Bindings.createBooleanBinding(
        () -> {
          Path mainFile = appState.mainFile.get();
          if (mainFile == null) return false;
          TreeItem<File> item = this.getTreeItem();
          if (item == null) return false;
          File file = item.getValue();
          if (file == null) return false;
          return appState.projectRoot.get().resolve(mainFile).equals(file.toPath());
        },
        treeItemProperty(), appState.mainFile
    );
    graphicProperty().bind(Bindings.createObjectBinding(
            this::getItemGraphic,
            itemProperty()
        )
    );
    backgroundProperty().bind(Bindings.createObjectBinding(
        () -> getItemBackgroundColor(appState.projectRoot.get(), isSelectedDir.get(), isSelectedMain.get()),
        isSelectedDir, isSelectedMain, selectedProperty(), itemProperty()
    ));
  }

  @Override
  protected void updateItem(File item, boolean empty) {
    super.updateItem(item, empty);
    setText((empty || item == null) ? "" : item.getName());
  }

  private ImageView getItemGraphic() {
    if (itemProperty().isNotNull().get()) {
      return itemProperty().get().isDirectory() ? new ImageView(folderIcon) : new ImageView(jFileIcon);
    }
    return null;
  }

  public Background getItemBackgroundColor(Path projectRoot, boolean isSelectedDir, boolean isSelectedMain) {
    if (itemProperty().isNotNull().get()) {
      Path itemPath = itemProperty().get().toPath();
      if (itemPath.equals(projectRoot.resolve(IO.getOutputDir()))) {
        return isSelected() ? outDirSelColor : outDirColor;
      }
    }
    if (isSelectedDir) {
      return isSelected() ? srcDirSelColor : srcDirColor;
    }
    if (isSelectedMain) {
      return isSelected() ? mainFileSelColor : mainFileColor;
    }
    if (isSelected()) {
      return selectedItemColor;
    }
    return null;
  }
}
