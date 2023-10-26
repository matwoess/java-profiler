package fxui;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class CommandController {
  @FXML
  private TextArea txtRunCommand;

  Stage dialogStage;

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
  }

  @FXML
  private void closeDialog() {
    dialogStage.close();
  }
}
