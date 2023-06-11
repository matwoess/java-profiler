package profile;

import misc.IO;
import model.Block;
import model.Class;
import model.JavaFile;
import model.Method;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class ReportMethodIndexWriter extends AbstractHtmlWriter {
  Class clazz;
  Path reportSourceFile;

  public ReportMethodIndexWriter(Class clazz, JavaFile javaFile) {
    this.clazz = clazz;
    this.reportSourceFile = IO.getReportSourceFilePath(javaFile);
    title = "Methods in " + clazz.getFullName();
    cssStyle = """
        body {
          font-family: Arial, sans-serif;
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
    Path sourceFileHref = IO.reportDir.relativize(reportSourceFile);
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
    return IO.getReportMethodIndexPath(clazz);
  }
}
