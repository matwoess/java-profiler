package profile;

import misc.Constants;
import model.JavaFile;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

public class ReportIndexWriter extends AbstractHtmlWriter {
  StringBuilder fileContent = new StringBuilder();

  public void sortedFileTable(JavaFile[] allJavaFiles) {
    JavaFile[] sortedFiles = Arrays.stream(allJavaFiles)
        .sorted(Comparator.comparingInt(JavaFile::getAggregatedMethodBlockCounts).reversed())
        .toArray(JavaFile[]::new);
    fileContent.append("<table>\n")
        .append("<tr>\n")
        .append("<th>Method invocations</th>\n")
        .append("<th>Detailed report<th>\n")
        .append("</tr>\n");
    for (JavaFile jFile : sortedFiles) {
      Path href = Constants.reportDir.relativize(jFile.getReportHtmlFile());
      Path filePath = Constants.reportDir.relativize(jFile.getReportFile());
      fileContent.append("<tr>\n")
          .append("<td>").append(jFile.getAggregatedMethodBlockCounts()).append("</td>\n")
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n", href, filePath))
          .append("</tr>\n");
    }
    fileContent.append("</table>\n");
  }

}
