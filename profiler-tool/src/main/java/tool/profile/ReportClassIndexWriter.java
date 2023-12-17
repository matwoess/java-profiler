package tool.profile;

import common.IO;
import tool.model.JClass;
import tool.model.JavaFile;

import java.nio.file.Path;
import java.util.*;

/**
 * The writer class for the class index overview page.
 * This is the main page of the report.
 */
public class ReportClassIndexWriter extends AbstractHtmlWriter {
  private final JavaFile[] allJavaFiles;

  /**
   * Creates a new {@link ReportClassIndexWriter} object.
   *
   * @param allJavaFiles the list of all java files contained in the project
   */
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

  /**
   * Generates the main content of the HTML document by calling {@link #sortedClassTable}.
   */
  @Override
  public void body() {
    sortedClassTable();
  }

  /**
   * Appends a table of all classes sorted by the number of method invocations to the internal <code>content</code>.
   */
  public void sortedClassTable() {
    Map<JClass, JavaFile> fileByClass = new HashMap<>();
    for (JavaFile jFile : allJavaFiles) {
      for (JClass clazz : jFile.topLevelClasses) {
        fileByClass.put(clazz, jFile);
      }
    }
    List<JClass> sortedClasses = Arrays.stream(allJavaFiles)
        .flatMap(f -> f.topLevelClasses.stream())
        .filter(c -> c.getMethodsRecursive().stream().anyMatch(m -> !m.isAbstract()))
        .sorted(Comparator.comparingInt(JClass::getAggregatedMethodBlockCounts).reversed())
        .toList();
    content.append("<table>\n")
        .append("<tr>\n")
        .append("<th>Method invocations</th>\n")
        .append("<th>Class</th>\n")
        .append("<th>Source file</th>\n")
        .append("</tr>\n");
    for (JClass clazz : sortedClasses) {
      JavaFile javaFile = fileByClass.get(clazz);
      Path methIdxHref = IO.getReportMethodIndexPath(clazz.name).getFileName();
      Path sourceFileHref = IO.getReportDir().relativize(IO.getReportSourceFilePath(javaFile.relativePath));
      content.append("<tr>\n")
          .append("<td class=\"hits\">").append(clazz.getAggregatedMethodBlockCounts()).append("</td>\n")
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n", methIdxHref, clazz.getName()))
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n", sourceFileHref, javaFile.sourceFile.toFile().getName()))
          .append("</tr>\n");
    }
    content.append("</table>\n");
  }

  /**
   * Defines the output path for the generated HTML document as {@link IO#getReportIndexPath}.
   * @return
   */
  @Override
  public Path getFileOutputPath() {
    return IO.getReportIndexPath();
  }
}
