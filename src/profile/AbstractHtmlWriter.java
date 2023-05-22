package profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractHtmlWriter {
  StringBuilder content = new StringBuilder();
  public String title;
  public String[] includeScripts;
  public String[] bodyScripts;
  public String cssStyle;

  public void header() {
    content.append("<!DOCTYPE html>\n").append("<html>\n").append("<head>\n");
    if (title != null) {
      content.append("<title>").append(title).append("</title>\n");
    }
    if (includeScripts != null) {
      for (String scriptSrc : includeScripts) {
        content.append(String.format("<script src=\"%s\"></script>\n", scriptSrc));
      }
    }
    if (cssStyle != null) {
      content.append("<style>\n").append(cssStyle).append("</style>\n");
    }
    content.append("</head>\n");
  }

  public void bodyStart() {
    content.append("<body>\n");
  }

  public void heading(String heading) {
    content.append("<h1>").append(heading).append("</h1>\n");
  }

  public void bodyEnd() {
    if (bodyScripts != null) {
      for (String bodyScript : bodyScripts) {
        content.append(String.format("<script type=\"text/javascript\" src=\"%s\"></script>\n", bodyScript));
      }
    }
    content.append("</body>\n");
    content.append("</html>\n");
  }

  public void write(Path destPath) {
    destPath.getParent().toFile().mkdirs(); // make sure parent directory exists
    try {
      Files.writeString(destPath, content.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
