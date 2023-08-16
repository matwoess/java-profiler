package fxui.util;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.OutputStream;

public class SystemOutputWriter extends OutputStream {
  private final TextArea txtOutput;

  public SystemOutputWriter(TextArea txtOutput) {
    this.txtOutput = txtOutput;
  }

  @Override
  public void write(int b) {
    appendChar((char) b);
  }

  public void appendChar(char ch) {
    Platform.runLater(() -> txtOutput.appendText(String.valueOf(ch)));
  }
}