package profile;

import misc.Constants;
import model.Class;
import model.JavaFile;

import java.nio.file.Path;
import java.util.*;

public class ReportClassIndexWriter extends AbstractHtmlWriter {

  public ReportClassIndexWriter() {
    title = "Classes";
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

  public void sortedClassTable(JavaFile[] allJavaFiles) {
    Map<Class, JavaFile> fileByClass = new HashMap<>();
    for (JavaFile jFile : allJavaFiles) {
      for (Class clazz : jFile.foundClasses) {
        fileByClass.put(clazz, jFile);
      }
    }
    List<Class> sortedClasses = Arrays.stream(allJavaFiles)
        .flatMap(f -> f.foundClasses.stream())
        .sorted(Comparator.comparingInt(Class::getAggregatedMethodBlockCounts).reversed())
        .toList();
    content.append("<table>\n")
        .append("<tr>\n")
        .append("<th>Method invocations</th>\n")
        .append("<th>Class</th>\n")
        .append("<th>Source file</th>\n")
        .append("</tr>\n");
    for (Class clazz : sortedClasses) {
      JavaFile javaFile = fileByClass.get(clazz);
      Path methIdxHref = clazz.getReportMethodIndexPath().getFileName();
      Path sourceFileHref = Constants.reportDir.relativize(javaFile.getReportHtmlFile());
      content.append("<tr>\n")
          .append("<td>").append(clazz.getAggregatedMethodBlockCounts()).append("</td>\n")
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n", methIdxHref, clazz.name))
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n", sourceFileHref, javaFile.sourceFile.toFile().getName()))
          .append("</tr>\n");
    }
    content.append("</table>\n");
  }
}
