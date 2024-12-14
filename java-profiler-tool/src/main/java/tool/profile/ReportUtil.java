package tool.profile;

/**
 * A utility class for common report generation helper methods.
 */
public class ReportUtil {

  /**
   * Format a number as a string with commas as thousands separator.
   *
   * @param number the number to be formatted
   * @return the formatted number string
   */
  public static String formatHitCount(long number) {
    return String.format("%,d", number);
  }
}
