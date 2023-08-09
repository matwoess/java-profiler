package fxui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

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

  public static ObjectBinding<Border> createBorderBinding(StringProperty textProperty, BooleanProperty invalidityProperty) {
    return Bindings.createObjectBinding(
        () -> {
          if (textProperty.get().isBlank()) return neutralBorder;
          if (invalidityProperty.get()) return invalidBorder;
          else return validBorder;
        },
        textProperty,
        invalidityProperty
    );
  }

  public static BooleanBinding createIsJavaFileBinding(StringProperty filePathProperty) {
    return Bindings.createBooleanBinding(
        () -> common.Util.isJavaFile(Path.of(filePathProperty.get())),
        filePathProperty
    );
  }

  public static BooleanBinding createIsDirectoryBinding(StringProperty dirPathProperty) {
    return Bindings.createBooleanBinding(
        () -> Path.of(dirPathProperty.get()).toFile().isDirectory(),
        dirPathProperty
    );
  }
}
