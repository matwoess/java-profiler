package fxui.model;

import common.IO;
import fxui.util.BindingUtils;
import javafx.beans.property.*;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static common.IO.getUIParametersPath;
import static fxui.model.RunMode.DEFAULT;
import static fxui.model.RunMode.REPORT_ONLY;

public class Parameters {
  public ObjectProperty<Path> projectRoot = new SimpleObjectProperty<>(null);

  public ObjectProperty<RunMode> runMode = new SimpleObjectProperty<>(DEFAULT);

  public ObjectProperty<Path> mainFile = new SimpleObjectProperty<>(null);
  public StringProperty programArgs = new SimpleStringProperty("");
  public ObjectProperty<Path> sourcesDir = new SimpleObjectProperty<>(null);
  public BooleanProperty syncCounters = new SimpleBooleanProperty(false);

  public BooleanProperty invalidMainFilePath = new SimpleBooleanProperty(false);
  public BooleanProperty invalidOutDirPath = new SimpleBooleanProperty(false);
  public BooleanProperty invalidSourcesDirPath = new SimpleBooleanProperty(false);

  public Parameters() {
    initializeExtraProperties();
  }

  public void initializeExtraProperties() {
    invalidMainFilePath.bind(mainFile.isNotNull().and(BindingUtils.creatRelativeIsJavaFileBinding(projectRoot, mainFile).not()));
    invalidSourcesDirPath.bind(sourcesDir.isNotNull().and(BindingUtils.createRelativeIsDirectoryBinding(projectRoot, sourcesDir).not()));
  }

  public String[] getRunParameters() {
    RunMode mode = runMode.get();
    List<String> arguments = new ArrayList<>();
    boolean sync = syncCounters.get();
    if (sync && mode != REPORT_ONLY) {
      arguments.add("--synchronized");
    }
    switch (mode) {
      case REPORT_ONLY -> arguments.add("--generate-report");
      case INSTRUMENT_ONLY -> {
        arguments.add("--instrument-only");
        arguments.add(mainFile.get().toString());
      }
      case DEFAULT -> {
        if (sourcesDir.isNotNull().get()) {
          String additionalSourcesDir = sourcesDir.get().toString();
          if (!additionalSourcesDir.isBlank()) {
            arguments.add("--sources-directory");
            arguments.add(additionalSourcesDir);
          }
        }
        arguments.add(mainFile.get().toString());
        String args = programArgs.get();
        if (!args.isBlank()) {
          arguments.addAll(Arrays.stream(args.split(" ")).toList());
        }
      }
    }
    return arguments.toArray(String[]::new);
  }

  public void exportParameters() {
    Path parametersPath = getUIParametersPath();
    IO.createDirectoriesIfNotExists(parametersPath);
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(parametersPath.toFile()))) {
      oos.writeInt(runMode.get().ordinal());
      oos.writeUTF(mainFile.get().toString());
      oos.writeUTF(programArgs.get());
      oos.writeUTF(sourcesDir.get().toString());
      oos.writeBoolean(syncCounters.get());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void importParameters() {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getUIParametersPath().toFile()))) {
       runMode.set(RunMode.values()[ois.readInt()]);
       mainFile.set(Path.of(ois.readUTF()));
       programArgs.set(ois.readUTF());
       sourcesDir.set(Path.of(ois.readUTF()));
       syncCounters.set(ois.readBoolean());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
