package tool.profile;

import common.IO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;

/**
 * The abstract superclass for all HTML writers.
 * <p>
 * Contains common methods to generate parts of an HTML document and write them to a file.
 * <p>
 * Subclasses should implement the {@link #body()} method to generate the main content of the HTML document.
 * <p>
 * The fields {@link #title}, {@link #includeScripts}, {@link #bodyScripts} and {@link #cssFile} can be used to
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
  public String cssFile;

  /**
   * Formatter for index percentage values with zero or one decimal places.
   */
  protected final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

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
    if (cssFile != null) {
      Path cssResource = IO.getReportResourcePath(cssFile);
      Path relativeCssPath = getFileOutputPath().getParent().relativize(cssResource);
      content.append("<style>@import \"").append(relativeCssPath).append("\"</style>\n");
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
   * Adds a breadcrumb bar at the top of the page to navigate back to the main index file.
   * <p/>
   * The main index itself does not have a breadcrumb bar.
   */
  private void breadcrumbBar() {
    if (getFileOutputPath().equals(IO.getReportIndexPath())) {
      return; // no breadcrumbs for the main index file itself
    }
    content.append("<div class=\"breadcrumbs\">");
    String backlinkButton = String.format("<input type=\"button\" onclick=\"location.href='%s';\" value=\"%s\"/>",
        getFileOutputPath().getParent().relativize(IO.getReportIndexPath()),
        "Back to Class Overview");
    content.append(backlinkButton);
    content.append("</div>");
  }

  /**
   * Writes the custom heading to the internal <code>content</code> StringBuilder.
   *
   * @param heading the page title string
   */
  public void heading(String heading) {
    content.append("<h2>").append(heading).append("</h2>\n");
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
        Path scriptResource = IO.getReportResourcePath(bodyScript);
        Path relativeScriptResource = getFileOutputPath().getParent().relativize(scriptResource);
        content.append(String.format("<script type=\"text/javascript\" src=\"%s\"></script>\n", relativeScriptResource));
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
    breadcrumbBar();
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
   *
   * @return the destination path of the output file
   */
  public abstract Path getFileOutputPath();

}
