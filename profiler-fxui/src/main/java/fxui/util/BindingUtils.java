package fxui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.nio.file.Path;

public class BindingUtils {
  private static final Border invalidBorder = new Border(
      new BorderStroke(
          Color.RED,
          BorderStrokeStyle.DASHED,
          CornerRadii.EMPTY,
          BorderWidths.DEFAULT
      )
  );
  private static final Border validBorder = new Border(
      new BorderStroke(
          Color.GREEN,
          BorderStrokeStyle.SOLID,
          CornerRadii.EMPTY,
          BorderWidths.DEFAULT
      )
  );
  private static final Border srcDirBorder = new Border(
      new BorderStroke(
          Color.BLUE,
          BorderStrokeStyle.SOLID,
          CornerRadii.EMPTY,
          BorderWidths.DEFAULT
      )
  );
  private static final Border mainFileBorder = validBorder;
  private static final Border outDirBorder = new Border(
      new BorderStroke(
          Color.BROWN,
          BorderStrokeStyle.SOLID,
          CornerRadii.EMPTY,
          BorderWidths.DEFAULT
      )
  );
  private static final Border neutralBorder = null;

  public static final StringConverter<Path> pathStringConverter = new StringConverter<>() {
    @Override
    public String toString(Path object) {
      return object != null ? object.toString() : "";
    }

    @Override
    public Path fromString(String string) {
      return Path.of(string);
    }
  };

  public static ObjectBinding<Border> createBorderBinding(StringProperty textProperty, BooleanBinding invalidityBinding) {
    return Bindings.createObjectBinding(
        () -> {
          if (textProperty.isEmpty().get()) return neutralBorder;
          if (invalidityBinding.get()) return invalidBorder;
          else return validBorder;
        },
        textProperty,
        invalidityBinding
    );
  }

  public static ObjectBinding<Border> createBorderBinding(ObjectProperty<Path> fileProperty, BooleanBinding invalidityBinding) {
    return Bindings.createObjectBinding(
        () -> {
          if (fileProperty.isNull().get()) return neutralBorder;
          if (invalidityBinding.get()) return invalidBorder;
          else return validBorder;
        },
        fileProperty,
        invalidityBinding
    );
  }

  public static BooleanBinding creatRelativeIsJavaFileBinding(ObjectProperty<Path> parentDirProperty, ObjectProperty<Path> fileProperty) {
    return Bindings.createBooleanBinding(
        () -> {
          if (parentDirProperty.isNull().get()) return false;
          if (fileProperty.isNull().get()) return false;
          return common.Util.isJavaFile(parentDirProperty.get().resolve(fileProperty.get()));
        },
        fileProperty,
        parentDirProperty
    );
  }

  public static BooleanBinding createIsDirectoryBinding(ObjectProperty<Path> dirProperty) {
    return Bindings.createBooleanBinding(
        () -> {
          if (dirProperty.isNull().get()) return false;
          return dirProperty.get().toFile().isDirectory();
        },
        dirProperty
    );
  }

  public static BooleanBinding createRelativeIsDirectoryBinding(ObjectProperty<Path> parentDirProperty, ObjectProperty<Path> dirProperty) {
    return Bindings.createBooleanBinding(
        () -> {
          if (parentDirProperty.isNull().get()) return false;
          if (dirProperty.isNull().get()) return false;
          return parentDirProperty.get().resolve(dirProperty.get()).toFile().isDirectory();
        },
        dirProperty,
        parentDirProperty
    );
  }

  public static BooleanBinding creatRelativeFileExistsBinding(ObjectProperty<Path> parentDirProperty, Path relFilePath) {
    return Bindings.createBooleanBinding(
        () -> {
          if (parentDirProperty.isNull().get()) return false;
          return parentDirProperty.get().resolve(relFilePath).toFile().exists();
        },
        parentDirProperty
    );
  }

  public static ObjectBinding<Border> createSelectedTreeItemBorderBinding(BooleanBinding isOutDir, BooleanBinding isSelectedDir, BooleanBinding isSelectedMain) {
    return Bindings.createObjectBinding(
        () -> {
          if (isOutDir.get()) return outDirBorder;
          if (isSelectedDir.get()) return srcDirBorder;
          if (isSelectedMain.get()) return mainFileBorder;
          else return neutralBorder;
        },
        isOutDir,
        isSelectedDir,
        isSelectedMain
    );
  }
}
