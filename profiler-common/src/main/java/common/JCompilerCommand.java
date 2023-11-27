package common;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JCompilerCommand {
  private final List<String> sourceFiles = new ArrayList<>();
  private Path classPath;
  private Path directory;
  private final List<String> compileArgs = new ArrayList<>();

  public JCompilerCommand addSourceFile(Path sourceFile) {
    this.sourceFiles.add(sourceFile.toString());
    return this;
  }

  public JCompilerCommand setClassPath(Path classPath) {
    this.classPath = classPath;
    return this;
  }

  public JCompilerCommand setDirectory(Path directory) {
    this.directory = directory;
    return this;
  }

  public JCompilerCommand addCompileArg(String arg, String val) {
    compileArgs.add(arg);
    compileArgs.add(val);
    return this;
  }

  public String[] build() {
    List<String> command = new ArrayList<>();
    command.add("javac");
    if (classPath != null) {
      command.add("-cp");
      command.add(classPath.toString());
    }
    if (directory != null) {
      command.add("-d");
      command.add(directory.toString());
    }
    command.addAll(compileArgs);
    command.addAll(sourceFiles);
    return command.toArray(String[]::new);
  }
}
