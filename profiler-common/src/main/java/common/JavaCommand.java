package common;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JavaCommand {
  private String mainClass;
  private Path classPath;
  private final List<String> args = new ArrayList<>();

  public JavaCommand setMainClass(String mainClass) {
    this.mainClass = mainClass;
    return this;
  }

  public JavaCommand setClassPath(Path classPath) {
    this.classPath = classPath;
    return this;
  }

  public JavaCommand addArgs(String... args) {
    this.args.addAll(List.of(args));
    return this;
  }

  public String[] build() {
    List<String> command = new ArrayList<>();
    command.add("java");
    if (classPath != null) {
      command.add("-cp");
      command.add(classPath.toString());
    }
    command.add(mainClass);
    command.addAll(args);
    return command.toArray(String[]::new);
  }
}
