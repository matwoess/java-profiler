package fxui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class CommandController {
  Stage dialogStage;

  @FXML
  private TextArea txtRunCommand;
  @FXML
  private Button btnCopyToClipboard;

  private static final Border clickedBorder = new Border(
      new BorderStroke(
          Color.GREEN,
          BorderStrokeStyle.SOLID,
          CornerRadii.EMPTY,
          BorderWidths.DEFAULT
      )
  );


  public void initUI(Stage stage) {
    this.dialogStage = stage;
    stage.setTitle("Run Command Preview");
    stage.getScene().setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        stage.close();
      }
    });
  }

  public void setCommand(String[] runCommand) {
    txtRunCommand.textProperty().set(String.join(" ", runCommand));
  }

  @FXML
  private void copyCommandToClipboard() {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    content.putString(txtRunCommand.textProperty().get());
    clipboard.setContent(content);
    btnCopyToClipboard.borderProperty().set(clickedBorder);
  }

  @FXML
  private void closeDialog() {
    dialogStage.close();
  }
}
