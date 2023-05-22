package profile;

import model.Block;
import model.JavaFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static misc.Constants.*;

public class ReportGenerator {
  StringBuilder report = new StringBuilder();

  List<Block> blocks;

  public ReportGenerator(List<Block> fileBlocks) {
    blocks = fileBlocks;
  }

  public void header(String title) {
    report.append("<!DOCTYPE html>\n")
        .append("<html>\n")
        .append("<head>\n")
        .append("<title>").append(title).append("</title>\n")
        .append("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.6.3/jquery.min.js\"></script>")
        .append("</head>\n");
  }

  public void bodyStart() {
    report.append("<body>\n");
  }

  public void heading(String main) {
    report.append("<h1>").append(main).append("</h1>\n");
  }

  public void codeDiv(JavaFile javaFile) {
    report.append("<pre>\n");
    report.append("<code>\n");
    try {
      String sourceCode = Files.readString(javaFile.sourceFile, StandardCharsets.ISO_8859_1);
      StringBuilder builder = new StringBuilder();
      int prevIdx = 0;
      List<TagInsert> tagInserts = getTagInserts(sourceCode.length());
      for (TagInsert tagInsert : tagInserts) {
        builder.append(sourceCode, prevIdx, tagInsert.chPos());
        prevIdx = tagInsert.chPos();
        builder.append(tagInsert.tag());
      }
      builder.append(sourceCode.substring(prevIdx));
      String annotatedCode = builder.toString();
      //String tabledCode = getCodeTable(annotatedCode);
      report.append(annotatedCode);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    report.append("</code>\n");
    report.append("</pre>\n");
  }

  private String getCodeTable(String annotatedCode) {
    StringBuilder builder = new StringBuilder();
    builder.append("<table>\n");
    builder.append("<tbody>\n");
    int lineNr = 1;
    String[] splitLines = annotatedCode.split("\n");
    for (String line : splitLines) {
      builder.append("<tr>");
      builder.append("<td class=\"lineNr\">");
      builder.append(lineNr);
      lineNr++;
      builder.append("</td><td class=\"code\">");
      builder.append(line);
      builder.append("</td>");
      builder.append("</tr>\n");
    }
    builder.append("</tbody>\n");
    builder.append("</table>\n");
    return builder.toString();
  }

  private List<TagInsert> getTagInserts(int textLength) {
    List<TagInsert> inserts = new ArrayList<>();
    inserts.add(new TagInsert(0, "<span>"));
    for (Block block : blocks) {
      inserts.add(new TagInsert(block.begPos, "</span>"));
      inserts.add(new TagInsert(block.begPos, codeSpanAt(block.begPos)));
      inserts.add(new TagInsert(block.endPos, "</span>"));
      inserts.add(new TagInsert(block.endPos, codeSpanAt(block.endPos)));
    }
    inserts.add(new TagInsert(textLength, "</span>"));
    inserts.sort(Comparator.comparing(TagInsert::chPos));
    return inserts;
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
    for (int i = 0; i < blocks.size(); i++) {
      Block b = blocks.get(i);
      if (b.begPos <= chPos && chPos < b.endPos) {
        activeBlocks.add(i);
      }
    }
    if (activeBlocks.isEmpty()) {
      return "<span>";
    } else {
      Block lastBlock = blocks.get(activeBlocks.get(activeBlocks.size() - 1));
      return codeSpan(activeBlocks, lastBlock);
    }
  }

  public void bodyEnd(Path reportFilePath) {
    report.append(String.format("<script type=\"text/javascript\" src=\"%s\"></script>\n", reportFilePath.getParent().relativize(reportHighlighter)));
    report.append("</body>\n");
    report.append("</html>\n");
  }

  public void write(Path destPath) {
    destPath.getParent().toFile().mkdirs(); // make sure parent directory exists
    try {
      Files.writeString(destPath, report.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

record TagInsert(int chPos, String tag) {
}