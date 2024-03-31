package tool.profile;

import common.IO;
import tool.model.JClass;
import tool.model.JavaFile;
import tool.model.Method;

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
    title = "Class Overview";
    cssFile = "css/index.css";
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
        .sorted(Comparator.comparingLong(JClass::getAggregatedMethodBlockCounts).reversed())
        .toList();
    content.append("<table>\n")
        .append("<tr>\n")
        .append("<th>Class</th>\n")
        .append("<th>Agg. Method Calls</th>\n")
        .append("<th>Block Hit Max</th>\n")
        .append("<th>Method Coverage</th>\n")
        .append("<th>Source File</th>\n")
        .append("</tr>\n");
    for (JClass clazz : sortedClasses) {
      JavaFile javaFile = fileByClass.get(clazz);
      Path methIdxHref = IO.getReportMethodIndexPath(clazz.name).getFileName();
      Path sourceFileHref = IO.getReportDir().relativize(IO.getReportSourceFilePath(javaFile.relativePath));
      content.append("<tr>\n")
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n", methIdxHref, clazz.getName()))
          .append("<td class=\"hits\">").append(clazz.getAggregatedMethodBlockCounts()).append("</td>\n")
          .append("<td class=\"hit-max\">").append(getBlockHitMax(clazz)).append("</td>\n")
          .append("<td class=\"coverage\">").append(getMethodCoverage(clazz)).append("</td>\n")
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n", sourceFileHref, javaFile.sourceFile.toFile().getName()))
          .append("</tr>\n");
    }
    content.append("</table>\n");
  }

  /**
   * Returns the method coverage of a class as a percentage string.
   * The coverage is calculated as the number of covered methods divided by the total number of methods.
   *
   * @param clazz the class to calculate the coverage for
   * @return the method coverage as a percentage string in the format "#.#% (covered/total)"
   */
  private String getMethodCoverage(JClass clazz) {
    List<Method> methods = clazz.getMethodsRecursive().stream().filter(m -> !m.isAbstract()).toList();
    int coveredMethods = (int) methods.stream().filter(m -> m.getMethodBlock().hits > 0).count();
    float coverage = (float) coveredMethods / methods.size() * 100;
    return String.format("%s%% (%d/%d)", DECIMAL_FORMAT.format(coverage), coveredMethods, methods.size());
  }

  /**
   * Returns the hit-count of the most frequent executed inner block inside a class and its methods.
   *
   * @param clazz the class to calculate the maximum for
   * @return the hit-count of the most often executed inner block inside a class and its methods
   */
  private int getBlockHitMax(JClass clazz) {
    return clazz.getBlocksRecursive().stream()
        .map(b -> b.hits)
        .max(Long::compareTo)
        .orElse(0L)
        .intValue();
  }

  /**
   * Defines the output path for the generated HTML document as {@link IO#getReportIndexPath}.
   */
  @Override
  public Path getFileOutputPath() {
    return IO.getReportIndexPath();
  }
}
