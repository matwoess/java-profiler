package fxui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.io.File;
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

  public static ObjectBinding<Border> createBorderBinding(StringProperty textProperty, BooleanProperty invalidityProperty) {
    return Bindings.createObjectBinding(
        () -> {
          if (textProperty.isEmpty().get()) return neutralBorder;
          if (invalidityProperty.get()) return invalidBorder;
          else return validBorder;
        },
        textProperty,
        invalidityProperty
    );
  }

  public static ObjectBinding<Border> createBorderBinding(ObjectProperty<Path> fileProperty, BooleanProperty invalidityProperty) {
    return Bindings.createObjectBinding(
        () -> {
          if (fileProperty.isNull().get()) return neutralBorder;
          if (invalidityProperty.get()) return invalidBorder;
          else return validBorder;
        },
        fileProperty,
        invalidityProperty
    );
  }

  public static BooleanBinding createIsJavaFileBinding(ObjectProperty<File> fileProperty) {
    return Bindings.createBooleanBinding(
        () -> common.Util.isJavaFile(fileProperty.get().toPath()),
        fileProperty
    );
  }

  public static BooleanBinding creatRelativeIsJavaFileBinding(ObjectProperty<Path> parentDirProperty, ObjectProperty<Path> fileProperty) {
    return Bindings.createBooleanBinding(
        () -> common.Util.isJavaFile(parentDirProperty.get().resolve(fileProperty.get())),
        fileProperty,
        parentDirProperty
    );
  }

  public static BooleanBinding createIsDirectoryBinding(ObjectProperty<Path> dirProperty) {
    return Bindings.createBooleanBinding(
        () -> dirProperty.get().toFile().isDirectory(),
        dirProperty
    );
  }

  public static BooleanBinding createRelativeIsDirectoryBinding(ObjectProperty<Path> parentDirProperty, ObjectProperty<Path> dirProperty) {
    return Bindings.createBooleanBinding(
        () -> parentDirProperty.get().resolve(dirProperty.get()).toFile().isDirectory(),
        dirProperty,
        parentDirProperty
    );
  }
}
