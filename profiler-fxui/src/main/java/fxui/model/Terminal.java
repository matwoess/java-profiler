package fxui.model;

import common.OS;

import java.nio.file.Path;

public enum Terminal {
  WINDOWS_CMD {
    @Override
    String getExecutable() {
      return "cmd.exe";
    }
  },
  WINDOWS_POWERSHELL {
    @Override
    String getExecutable() {
      return "powershell.exe";
    }
  },
  GNOME_TERMINAL {
    @Override
    String getExecutable() {
      return "gnome-terminal";
    }
  },
  GNOME_CONSOLE {
    @Override
    String getExecutable() {
      return "kgx";
    }
  },
  KDE_KONSOLE {
    @Override
    String getExecutable() {
      return "konsole";
    }
  },
  MACOS_TERMINAL {
    @Override
    String getExecutable() {
      return "Terminal";
    }
  };

  abstract String getExecutable();

  public String[] wrapWithTerminalCommand(String cmdString, Path pwd) {
    return switch (this) {
      case WINDOWS_CMD -> new String[]{
          "cmd.exe",
          "/c",
          "start %s /c \"%s && pause || pause\"".formatted(getExecutable(), cmdString)
      };
      case WINDOWS_POWERSHELL -> new String[]{
          "cmd.exe",
          "/c",
          "start %s -Command \" %s ; pause \"".formatted(getExecutable(), cmdString.replace("\"", "'"))
      };
      case GNOME_TERMINAL, GNOME_CONSOLE -> new String[]{
          "/bin/sh",
          "-c",
          "%s -- bash -c \"%s; echo Done - Press enter to exit; read\"".formatted(getExecutable(), cmdString)
      };
      case KDE_KONSOLE -> new String[]{
          "/bin/sh",
          "-c",
          "%s -e bash -c \"%s; echo Done - Press enter to exit; read\"".formatted(getExecutable(), cmdString)
      };
      case MACOS_TERMINAL -> new String[]{
          "/bin/sh",
          "-c",
          "/usr/bin/osascript -e " +
              String.format(
                  "'tell app \"%s\" to activate & do script \"cd %s; %s; echo Done - Press enter to exit; read; exit\"'",
                  getExecutable(),
                  pwd,
                  cmdString.replace("\"", "")
              )
      };
    };
  }

  public static Terminal[] getSystemTerminalOptions() {
    return switch (OS.getOS()) {
      case WINDOWS -> new Terminal[]{WINDOWS_CMD, WINDOWS_POWERSHELL};
      case LINUX -> new Terminal[]{GNOME_TERMINAL, KDE_KONSOLE, GNOME_CONSOLE};
      case MAC -> new Terminal[]{MACOS_TERMINAL};
      case SOLARIS -> throw new RuntimeException("unsupported operating system");
    };
  }

  public static Terminal getDefaultSystemTerminal() {
    return getSystemTerminalOptions()[0];
  }

  @Override
  public String toString() {
    return switch (this) {
      case WINDOWS_CMD -> "Windows Command Prompt";
      case WINDOWS_POWERSHELL -> "Windows PowerShell";
      case GNOME_TERMINAL -> "GNOME Terminal";
      case GNOME_CONSOLE -> "GNOME Console";
      case KDE_KONSOLE -> "KDE Konsole";
      case MACOS_TERMINAL -> "MacOS Terminal";
    };
  }
}
