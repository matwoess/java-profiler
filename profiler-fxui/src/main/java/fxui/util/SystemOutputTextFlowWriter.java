package fxui.util;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SystemOutputTextFlowWriter extends OutputStream {
  private final TextFlow textFlow;
  public SystemOutputTextFlowWriter(TextFlow textFlow) {
    this.textFlow = textFlow;
  }
  Font defaultFont = new Font("Consolas", 12);
  StringBuilder curLine = new StringBuilder();

  @Override
  public void write(int b) {
    appendChar((char)b);
  }

  public void appendChar(char ch) {
    curLine.append(ch);
    if (ch == '\n') {
      String lineStr = curLine.toString();
      curLine = new StringBuilder();
      List<Text> segments = getTextSegments(lineStr);
      Platform.runLater(() -> textFlow.getChildren().addAll(segments));
    }
  }

  private List<Text> getTextSegments(String lineStr) {
    List<Text> segments = new ArrayList<>();
    int escapeSeqIdx = lineStr.indexOf("\u001B");
    if (escapeSeqIdx == -1) {
      segments.add(getTextSegment(lineStr, null));
      return segments;
    }
    Color textColor = null;
    while (escapeSeqIdx != -1) {
      if (escapeSeqIdx != 0) {
        segments.add(getTextSegment(lineStr.substring(0, escapeSeqIdx), textColor));
      }
      lineStr = lineStr.substring(escapeSeqIdx+1);
      int effectEndIndex = lineStr.indexOf("m");
      String effect = lineStr.substring(0, effectEndIndex+1);
      lineStr = lineStr.substring(effectEndIndex+1);
      switch (effect) {
        case "[32m" -> textColor = Color.GREEN;
        case "[31m" -> textColor = Color.RED;
        case "[97m" -> textColor = Color.WHITE;
        case "[0m" -> textColor = null;
      }
      escapeSeqIdx = lineStr.indexOf("\u001B");
    }
    segments.add(getTextSegment(lineStr, null));
    return segments;
  }

  private Text getTextSegment(String content, Color textColor) {
    Text seg = new Text(content);
    seg.setFont(defaultFont);
    if (textColor != null) {
      seg.setFill(textColor);
    }
    return seg;
  }
}