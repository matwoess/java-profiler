package profile;

import misc.CodeInsert;
import model.Block;
import model.JavaFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static misc.Constants.*;

public class ReportSourceWriter extends AbstractHtmlWriter {
  JavaFile javaFile;

  public ReportSourceWriter(JavaFile javaFile, String title) {
    this.javaFile = javaFile;
    this.title = title;
    includeScripts = new String[]{
        "https://ajax.googleapis.com/ajax/libs/jquery/3.6.3/jquery.min.js"
    };
    cssStyle = """
        td.lNr {
          text-align: right;
          background-color: #eee;
        }
        td.hits {
          background-color: #ddd;
        }
        """;
    bodyScripts = new String[]{
        javaFile.getReportHtmlFile().getParent().relativize(reportHighlighter).toString()
    };
  }

  public void codeDiv() {
    content.append("<pre>\n");
    content.append("<code>\n");
    try {
      String sourceCode = Files.readString(javaFile.sourceFile, StandardCharsets.ISO_8859_1);
      StringBuilder builder = new StringBuilder();
      int prevIdx = 0;
      List<CodeInsert> tagInserts = getTagInserts(sourceCode);
      for (CodeInsert tagInsert : tagInserts) {
        builder.append(sourceCode, prevIdx, tagInsert.chPos());
        prevIdx = tagInsert.chPos();
        builder.append(tagInsert.code());
      }
      builder.append(sourceCode.substring(prevIdx));
      String annotatedCode = builder.toString();
      String tabledCode = getCodeTable(annotatedCode);
      content.append(tabledCode);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    content.append("</code>\n");
    content.append("</pre>\n");
  }

  private String getCodeTable(String annotatedCode) {
    StringBuilder builder = new StringBuilder();
    builder.append("<table>\n");
    int lineNr = 1;
    String[] splitLines = annotatedCode.split("\n");
    for (String line : splitLines) {
      builder.append("<tr>");
      builder.append("<td class=\"hits\">").append(getHitsForLine(lineNr)).append("</td>");
      builder.append("<td class=\"lNr\">").append(lineNr++).append("</td>");
      builder.append("<td class=\"code\">").append(line).append("</td");
      builder.append("</tr>\n");
    }
    builder.append("</table>\n");
    return builder.toString();
  }

  private List<CodeInsert> getTagInserts(String sourceCode) {
    char lf = '\n';
    List<CodeInsert> inserts = new ArrayList<>();
    inserts.add(new CodeInsert(0, "<span>"));
    for (Block block : javaFile.foundBlocks) {
      if (sourceCode.charAt(block.begPos) != lf) { // optimization to not add 0-length block spans
        inserts.add(new CodeInsert(block.begPos, "</span>"));
        inserts.add(new CodeInsert(block.begPos, codeSpanAt(block.begPos)));
      }
      if (sourceCode.charAt(block.endPos) != lf) { // optimization to not add 0-length block spans
        inserts.add(new CodeInsert(block.endPos, "</span>"));
        inserts.add(new CodeInsert(block.endPos, codeSpanAt(block.endPos)));
      }
    }
    for (int index = sourceCode.indexOf(lf); index >= 0; index = sourceCode.indexOf(lf, index + 1)) {
      inserts.add(new CodeInsert(index, "</span>"));
      inserts.add(new CodeInsert(index + 1, codeSpanAt(index + 1)));
    }
    inserts.add(new CodeInsert(sourceCode.length(), "</span>"));
    inserts.sort(Comparator.comparing(CodeInsert::chPos));
    return inserts;
  }

  private String getHitsForLine(int lineNr) {
    List<Block> activeBlocks = new ArrayList<>();
    for (int i = 0; i < javaFile.foundBlocks.size(); i++) {
      Block b = javaFile.foundBlocks.get(i);
      if (b.beg <= lineNr && lineNr <= b.end) {
        activeBlocks.add(b);
      }
    }
    return String.join(" ", activeBlocks.stream().map(b -> Integer.toString(b.hits)).toArray(String[]::new));
  }

  private String codeSpan(List<Integer> activeBlocks, Block block) {
    String description = block.toString();
    int hits = block.hits;
    String title;
    String coverageClass;
    if (block.blockType.isNotYetSupported()) { // currently not supported
      coverageClass = "u";
      title = String.format("%s&#10;&#10;%s",
          description,
          "This type of block is currently not supported by the instrumenter."
      );
    } else {
      coverageClass = hits > 0 ? "c" : "nc";
      String coverageStatus = hits > 0 ? "covered" : "not covered";
      // &#10; == <br/> == newLine
      title = String.format("%s&#10;&#10;Hits: %d (%s)", description, hits, coverageStatus);
    }
    String classes = activeBlocks.stream().map(i -> "b" + i).collect(Collectors.joining(" "));
    return String.format("<span class=\"%s %s\" title=\"%s\">", coverageClass, classes, title);
  }

  private String codeSpanAt(int chPos) {
    List<Integer> activeBlocks = new ArrayList<>();
    for (int i = 0; i < javaFile.foundBlocks.size(); i++) {
      Block b = javaFile.foundBlocks.get(i);
      if (b.begPos <= chPos && chPos < b.endPos) {
        activeBlocks.add(i);
      }
    }
    if (activeBlocks.isEmpty()) {
      return "<span>";
    } else {
      Block lastBlock = javaFile.foundBlocks.get(activeBlocks.get(activeBlocks.size() - 1));
      return codeSpan(activeBlocks, lastBlock);
    }
  }

}