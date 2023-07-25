package profile;

import misc.IO;
import model.Class;
import model.JavaFile;

import java.nio.file.Path;
import java.util.*;

public class ReportClassIndexWriter extends AbstractHtmlWriter {
  JavaFile[] allJavaFiles;

  public ReportClassIndexWriter(JavaFile[] allJavaFiles) {
    this.allJavaFiles = allJavaFiles;
    title = "Classes";
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
    sortedClassTable();
  }

  public void sortedClassTable() {
    Map<Class, JavaFile> fileByClass = new HashMap<>();
    for (JavaFile jFile : allJavaFiles) {
      for (Class clazz : jFile.topLevelClasses) {
        fileByClass.put(clazz, jFile);
      }
    }
    List<Class> sortedClasses = Arrays.stream(allJavaFiles)
        .flatMap(f -> f.topLevelClasses.stream())
        .filter(c -> c.getMethodsRecursive().stream().anyMatch(m -> !m.isAbstract()))
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
      Path methIdxHref = IO.getReportMethodIndexPath(clazz).getFileName();
      Path sourceFileHref = IO.getReportDir().relativize(IO.getReportSourceFilePath(javaFile));
      content.append("<tr>\n")
          .append("<td class=\"hits\">").append(clazz.getAggregatedMethodBlockCounts()).append("</td>\n")
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n", methIdxHref, clazz.getName()))
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n", sourceFileHref, javaFile.sourceFile.toFile().getName()))
          .append("</tr>\n");
    }
    content.append("</table>\n");
  }

  @Override
  public Path getFileOutputPath() {
    return IO.getReportIndexPath();
  }
}