package tool.profile;

import tool.model.CodeInsert;
import tool.model.Block;
import tool.model.CodeRegion;
import tool.model.JavaFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import common.IO;

public class ReportSourceWriter extends AbstractHtmlWriter {
  JavaFile javaFile;

  public ReportSourceWriter(JavaFile javaFile) {
    this.javaFile = javaFile;
    this.title = javaFile.sourceFile.getFileName().toString();
    includeScripts = new String[]{
        "https://ajax.googleapis.com/ajax/libs/jquery/3.6.3/jquery.min.js"
    };
    cssStyle = """
        body {
          font-family: Helvetica Neue, Verdana, sans-serif;
        }
        table {
          border-collapse: collapse;
        }
        td {
          padding: 0;
        }
        td.lNr {
          text-align: right;
          background-color: #dedede;
        }
        :target td.lNr {
          background-color: Gold;
        }
        td.hits {
          background-color: #eee;
        }
        """;
    bodyScripts = new String[]{
        IO.getReportSourceFilePath(javaFile.relativePath).getParent().relativize(IO.getReportHighlighterPath()).toString()
    };
  }

  @Override
  public void body() {
    codeDiv();
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
        builder.append(escapeHtmlTagCharacters(sourceCode.substring(prevIdx, tagInsert.chPos())));
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

  public static String escapeHtmlTagCharacters(String code) {
    return code.replace("<", "&lt;").replace(">", "&gt;");
  }

  private String getCodeTable(String annotatedCode) {
    StringBuilder builder = new StringBuilder();
    builder.append("<table>\n");
    int lineNr = 1;
    String[] splitLines = annotatedCode.split("\n");
    for (String line : splitLines) {
      builder.append(String.format("<tr id=\"%s\">", lineNr));
      builder.append("<td class=\"hits\">").append(getHitsForLine(lineNr)).append("</td>");
      builder.append("<td class=\"lNr\">").append(lineNr).append("</td>");
      builder.append("<td class=\"code\">").append(line).append("</td>");
      builder.append("</tr>\n");
      lineNr++;
    }
    builder.append("</table>\n");
    return builder.toString();
  }

  private List<CodeInsert> getTagInserts(String sourceCode) {
    char lf = '\n';
    List<CodeInsert> inserts = new ArrayList<>();
    inserts.add(new CodeInsert(0, "<span>"));
    for (Block block : javaFile.foundBlocks) {
      if (sourceCode.charAt(block.beg.pos()) != lf) { // optimization to not add 0-length region spans
        inserts.add(new CodeInsert(block.beg.pos(), "</span>"));
        inserts.add(new CodeInsert(block.beg.pos(), codeSpanAt(block.beg.pos())));
      }
      for (CodeRegion region : block.codeRegions) {
        if (sourceCode.charAt(region.beg.pos()) != lf) { // optimization to not add 0-length region spans
          inserts.add(new CodeInsert(region.beg.pos(), "</span>"));
          inserts.add(new CodeInsert(region.beg.pos(), codeSpanAt(region.beg.pos())));
        }
        if (sourceCode.charAt(region.end.pos()) != lf) { // optimization to not add 0-length region spans
          inserts.add(new CodeInsert(region.end.pos(), "</span>"));
          inserts.add(new CodeInsert(region.end.pos(), codeSpanAt(region.end.pos())));
        }
      }
      if (sourceCode.charAt(block.end.pos()) != lf) { // optimization to not add 0-length region spans
        inserts.add(new CodeInsert(block.end.pos(), "</span>"));
        inserts.add(new CodeInsert(block.end.pos(), codeSpanAt(block.end.pos())));
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
    StringBuilder builder = new StringBuilder();
    List<CodeRegion> activeRegions = getActiveCodeRegionsForLine(lineNr);
    for (CodeRegion region : activeRegions) {
      int hitCount = region.getHitCount();
      String coverageStatus = hitCount > 0 ? "c" : "nc";
      String regionClass = "r" + region.block.id + "_" + region.id;
      builder.append(String.format("<span class=\"%s %s\">%s</span>", coverageStatus, regionClass, hitCount));
      builder.append(" ");
    }
    return builder.toString();
  }

  private String codeSpanAt(int chPos) {
    List<Block> activeBlocks = getActiveBlocksAtCharPosition(chPos);
    if (activeBlocks.isEmpty()) {
      return "<span>";
    }
    Block lastBlock = activeBlocks.get(activeBlocks.size() - 1);
    CodeRegion region = null;
    for (CodeRegion r : lastBlock.codeRegions) {
      if (r.beg.pos() <= chPos && chPos < r.end.pos()) {
        region = r;
      }
    }
    return codeSpan(activeBlocks, lastBlock, region);

  }

  private String codeSpan(List<Block> activeBlocks, Block block, CodeRegion region) {
    String description = block.toString();
    String regionDescr = "";
    String minusBlocks = null;
    if (region != null) {
      regionDescr = region.toString();
      minusBlocks = getMinusBlockClasses(region);
    }
    int hits = (region != null) ? region.getHitCount() : block.hits;
    String coverageClass = hits > 0 ? "c" : "nc";
    // &#10; == <br/> == newLine
    String title = String.format("%s&#10;%s&#10;Hits: %d", description, regionDescr, hits);
    //title = String.valueOf(hits);
    String classes = activeBlocks.stream().map(b -> "b" + b.id).collect(Collectors.joining(" "));
    if (region != null) {
      classes += " r" + activeBlocks.get(activeBlocks.size() - 1).id + "_" + region.id;
      if (!minusBlocks.isBlank()) {
        classes = minusBlocks + " " + classes;
      }
    }
    return String.format("<span class=\"%s %s\" title=\"%s\">", coverageClass, classes, title);
  }

  private static String getMinusBlockClasses(CodeRegion region) {
    Stream<Block> minusBlocks = Stream.concat(region.minusBlocks.stream(), region.block.inheritedJumpBlocks.stream());
    return minusBlocks.distinct().map(b -> "m" + b.id).collect(Collectors.joining(" "));
  }

  private List<Block> getActiveBlocksAtCharPosition(int chPos) {
    return javaFile.foundBlocks.stream()
        .filter(b -> b.beg.pos() <= chPos && chPos < b.end.pos() && !b.blockType.hasNoCounter())
        .collect(Collectors.toList());
  }

  private List<CodeRegion> getActiveCodeRegionsForLine(int lineNr) {
    return javaFile.foundBlocks.stream()
        .filter(b -> b.isActiveInLine(lineNr))
        .flatMap(b -> b.codeRegions.stream())
        .filter(cr -> cr.isActiveInLine(lineNr))
        .sorted(Comparator.comparing(cr -> cr.beg))
        .collect(Collectors.toList());
  }

  @Override
  public Path getFileOutputPath() {
    return IO.getReportSourceFilePath(javaFile.relativePath);
  }
}