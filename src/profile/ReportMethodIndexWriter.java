package profile;

import misc.Constants;
import model.Class;
import model.Method;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class ReportMethodIndexWriter extends AbstractHtmlWriter {

  public ReportMethodIndexWriter(String className) {
    title = "Methods in " + className;
    cssStyle = """
        table {
          border-collapse: collapse;
        }
        table, th, td {
          border: 1px solid #ddd;
        }
        th, td {
          padding: 8px;
        }
        """;
  }

  public void sortedMethodTable(Class clazz, Path reportSourceFile) {
    List<Method> sortedMethods = clazz.methods.stream()
        .filter(method -> !method.isAbstract())
        .sorted(Comparator.comparingInt((Method m) -> m.getMethodBlock().hits).reversed())
        .toList();
    content.append("<table>\n")
        .append("<tr>\n")
        .append("<th>Invocations</th>\n")
        .append("<th>Method</th>\n")
        .append("</tr>\n");
    for (Method meth : sortedMethods) {
      Path href = Constants.reportDir.relativize(reportSourceFile);
      String lineNrRef = href + "#" + meth.getMethodBlock().beg;
      content.append("<tr>\n")
          .append("<td>").append(meth.getMethodBlock().hits).append("</td>\n")
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n", lineNrRef, meth.name))
          .append("</tr>\n");
    }
    content.append("</table>\n");
  }

}
