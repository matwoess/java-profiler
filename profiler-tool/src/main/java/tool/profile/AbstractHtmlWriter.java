package tool.profile;

import common.IO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The abstract superclass for all HTML writers.
 * <p>
 * Contains common methods to generate parts of an HTML document and write them to a file.
 * <p>
 * Subclasses should implement the {@link #body()} method to generate the main content of the HTML document.
 * <p>
 * The fields {@link #title}, {@link #includeScripts}, {@link #bodyScripts} and {@link #cssStyle} can be used to
 * customize the HTML document.
 * <p>
 * The {@link #getFileOutputPath()} method should be overridden
 * to return the path to where the output file should be written to.
 */
public abstract class AbstractHtmlWriter {
  final StringBuilder content = new StringBuilder();
  public String title;
  public String[] includeScripts;
  public String[] bodyScripts;
  public String cssStyle;

  /**
   * Appends the HTML header to the internal <code>content</code> StringBuilder.
   * <p>
   * Includes the doctype, the html and head tags, the title, the included scripts and the css style.
   */
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

  /**
   * Appends the HTML body start tag to the internal <code>content</code> StringBuilder.
   */
  public void bodyStart() {
    content.append("<body>\n");
  }

  /**
   * Writes the custom heading to the internal <code>content</code> StringBuilder.
   * @param heading
   */
  public void heading(String heading) {
    content.append("<h1>").append(heading).append("</h1>\n");
  }

  /**
   * Should be overridden to generate the main content of the HTML document.
   */
  public abstract void body();

  /**
   * Appends the HTML body end tag to the internal <code>content</code> StringBuilder and includes all the body scripts.
   */
  public void bodyEnd() {
    if (bodyScripts != null) {
      for (String bodyScript : bodyScripts) {
        content.append(String.format("<script type=\"text/javascript\" src=\"%s\"></script>\n", bodyScript));
      }
    }
    content.append("</body>\n");
    content.append("</html>\n");
  }

  /**
   * Generates the full HTML document out of all helper methods.
   */
  public void generate() {
    header();
    bodyStart();
    heading(title);
    body();
    bodyEnd();
  }

  /**
   * Writes the generated HTML document to the file specified by {@link #getFileOutputPath()}.
   * <p>
   * Parent directories are created if they do not exist.
   */
  public void write() {
    this.generate();
    Path destPath = getFileOutputPath();
    IO.createDirectoriesIfNotExists(destPath);
    try {
      Files.writeString(destPath, content.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the path to where the output file should be written to. Should be overridden by subclasses.
   * @return the destination path of the output file
   */
  public abstract Path getFileOutputPath();

}
