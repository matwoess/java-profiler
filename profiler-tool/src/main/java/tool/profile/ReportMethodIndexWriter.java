package tool.profile;

import common.IO;
import tool.model.Block;
import tool.model.JClass;
import tool.model.JavaFile;
import tool.model.Method;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class ReportMethodIndexWriter extends AbstractHtmlWriter {
  JClass clazz;
  Path reportSourceFile;

  public ReportMethodIndexWriter(JClass clazz, JavaFile javaFile) {
    this.clazz = clazz;
    this.reportSourceFile = IO.getReportSourceFilePath(javaFile.relativePath);
    title = "Methods in " + clazz.getFullName();
    cssStyle = """
        body {
          font-family: Helvetica Neue, Verdana, sans-serif;
        }
        table {
          border-collapse: collapse;
        }
        table, th, td {
          border: 1px solid #ddd;
        }
        th, td {
          padding: 8px;
        }
        td.hits {
          text-align: right;
        }
        a {
          color: MediumBlue;
          text-decoration: none;
        }
        """;
  }

  @Override
  public void body() {
    sortedMethodTable();
  }

  public void sortedMethodTable() {
    List<Method> sortedMethods = clazz.getMethodsRecursive().stream()
        .filter(method -> !method.isAbstract())
        .sorted(Comparator.comparingInt((Method m) -> m.getMethodBlock().hits).reversed())
        .toList();
    content.append("<table>\n")
        .append("<tr>\n")
        .append("<th>Invocations</th>\n")
        .append("<th>Method</th>\n")
        .append("</tr>\n");
    Path sourceFileHref = IO.getReportDir().relativize(reportSourceFile);
    for (Method meth : sortedMethods) {
      Block methBlock = meth.getMethodBlock();
      String lineNrRef = sourceFileHref + "#" + methBlock.beg;
      String methName = (methBlock.clazz != clazz)
          ? methBlock.clazz.getName() + "::" + meth.name
          : meth.name;
      content.append("<tr>\n")
          .append("<td class=\"hits\">").append(meth.getMethodBlock().hits).append("</td>\n")
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n", lineNrRef, methName))
          .append("</tr>\n");
    }
    content.append("</table>\n");
  }

  @Override
  public Path getFileOutputPath() {
    return IO.getReportMethodIndexPath(clazz.name);
  }
}
