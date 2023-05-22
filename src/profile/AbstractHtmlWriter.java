package profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractHtmlWriter {
  StringBuilder report = new StringBuilder();
  public String title;
  public String[] includeScripts;
  public String[] bodyScripts;

  public void header() {
    report.append("<!DOCTYPE html>\n").append("<html>\n").append("<head>\n");
    if (title != null) {
      report.append("<title>").append(title).append("</title>\n");
    }
    if (includeScripts != null) {
      for (String scriptSrc : includeScripts) {
        report.append(String.format("<script src=\"%s\"></script>\n", scriptSrc));
      }
    }
    report.append("</head>\n");
  }

  public void bodyStart() {
    report.append("<body>\n");
  }

  public void heading(String heading) {
    report.append("<h1>").append(heading).append("</h1>\n");
  }

  public void bodyEnd() {
    if (bodyScripts != null) {
      for (String bodyScript : bodyScripts) {
        report.append(String.format("<script type=\"text/javascript\" src=\"%s\"></script>\n", bodyScript));
      }
    }
    report.append("</body>\n");
    report.append("</html>\n");
  }

  public void write(Path destPath) {
    destPath.getParent().toFile().mkdirs(); // make sure parent directory exists
    try {
      Files.writeString(destPath, report.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
