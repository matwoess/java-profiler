package common;

public enum OS {
  WINDOWS, LINUX, MAC, SOLARIS;

  private static OS os = null;

  public String lineSeparator() {
    if (this == WINDOWS) {
      return "\r\n";
    } else {
      return "\n";
    }
  }

  public String pathSeparator() {
    if (this == WINDOWS) {
      return ";";
    } else {
      return ":";
    }
  }

  public static OS getOS() {
    if (os == null) {
      String osName = System.getProperty("os.name").toLowerCase();
      if (osName.contains("win")) {
        os = OS.WINDOWS;
      } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
        os = OS.LINUX;
      } else if (osName.contains("mac")) {
        os = OS.MAC;
      } else if (osName.contains("sunos")) {
        os = OS.SOLARIS;
      }
    }
    return os;
  }
}
