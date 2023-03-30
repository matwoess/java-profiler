package profile;

import common.Block;
import common.JavaFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReportGenerator {
  StringBuilder report = new StringBuilder();

  public void header(String title) {
    report.append("<!DOCTYPE html>\n")
        .append("<html>\n")
        .append("<head>\n")
        .append("<title>").append(title).append("</title>\n")
        .append("</head>\n");
  }

  public void bodyStart() {
    report.append("<body>\n");
  }

  public void heading(String main) {
    report.append("<h1>").append(main).append("</h1>\n");
  }

  public void codeDiv(JavaFile javaFile, int[] fileBlockCounts) {
    report.append("<div>\n");
    report.append("<pre><code>\n");
    try {
      String sourceCode = Files.readString(javaFile.sourceFile);
      List<Block> blocks = javaFile.foundBlocks;
      for (int i = 0; i < blocks.size(); i++) {
        blocks.get(i).hits = fileBlockCounts[i];
      }
      StringBuilder builder = new StringBuilder();
      int prevIdx = 0;
      List<TagInsert> tagInserts = getTagInserts(blocks);
      for (TagInsert tagInsert : tagInserts) {
        builder.append(sourceCode, prevIdx, tagInsert.chPos());
        prevIdx = tagInsert.chPos();
        builder.append(tagInsert.tag());
      }
      builder.append(sourceCode.substring(prevIdx));
      String annotatedCode = builder.toString();
      String tabledCode = getCodeTable(annotatedCode);
      report.append(tabledCode);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    report.append("</code></pre>\n");
    report.append("</div>\n");
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

  private List<TagInsert> getTagInserts(List<Block> blocks) {
    List<TagInsert> inserts = new ArrayList<>();
    for (Block block : blocks) {
      String beginTag = String.format("<blockBegin tag=\"%d\"/>", block.hits);
      String endTag = "<blockEnd/>";
      inserts.add(new TagInsert(block.begPos, beginTag));
      inserts.add(new TagInsert(block.endPos, endTag));
    }
    inserts.sort(Comparator.comparing(TagInsert::chPos));
    return inserts;
  }

  public void bodyEnd() {
    report.append("</body>\n");
    report.append("</html>\n");
  }

  public void write(Path destPath) {
    try {
      Files.writeString(destPath, report.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

record TagInsert(int chPos, String tag) {
}