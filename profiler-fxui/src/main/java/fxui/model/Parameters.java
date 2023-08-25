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
  public ObjectProperty<RunMode> runMode = new SimpleObjectProperty<>(DEFAULT);

  public StringProperty mainFile = new SimpleStringProperty("");
  public StringProperty programArgs = new SimpleStringProperty("");
  public StringProperty sourcesDir = new SimpleStringProperty("");
  public StringProperty outputDir = new SimpleStringProperty("");
  public BooleanProperty syncCounters = new SimpleBooleanProperty(false);

  public BooleanProperty invalidMainFilePath = new SimpleBooleanProperty(false);
  public BooleanProperty invalidOutDirPath = new SimpleBooleanProperty(false);
  public BooleanProperty invalidSourcesDirPath = new SimpleBooleanProperty(false);

  public Parameters() {
    initializeExtraProperties();
  }

  public void initializeExtraProperties() {
    invalidMainFilePath.bind(mainFile.isNotEmpty().and(BindingUtils.createIsJavaFileBinding(mainFile).not()));
    invalidSourcesDirPath.bind(sourcesDir.isNotEmpty().and(BindingUtils.createIsDirectoryBinding(sourcesDir).not()));
    invalidOutDirPath.bind(outputDir.isNotEmpty().and(BindingUtils.createIsDirectoryBinding(outputDir).not()));
  }

  public String[] getRunParameters() {
    RunMode mode = runMode.get();
    List<String> arguments = new ArrayList<>();
    String outDir = outputDir.get();
    if (!outDir.isBlank()) {
      arguments.add("--out-directory");
      arguments.add(outDir);
    }
    boolean sync = syncCounters.get();
    if (sync && mode != REPORT_ONLY) {
      arguments.add("--synchronized");
    }
    switch (mode) {
      case REPORT_ONLY -> arguments.add("--generate-report");
      case INSTRUMENT_ONLY -> {
        arguments.add("--instrument-only");
        arguments.add(mainFile.get());
      }
      case DEFAULT -> {
        String additionalSourcesDir = sourcesDir.get();
        if (!additionalSourcesDir.isBlank()) {
          arguments.add("--sources-directory");
          arguments.add(additionalSourcesDir);
        }
        arguments.add(mainFile.get());
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
      oos.writeUTF(mainFile.get());
      oos.writeUTF(programArgs.get());
      oos.writeUTF(sourcesDir.get());
      oos.writeUTF(outputDir.get());
      oos.writeBoolean(syncCounters.get());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void importParameters() {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getUIParametersPath().toFile()))) {
       runMode.set(RunMode.values()[ois.readInt()]);
       mainFile.set(ois.readUTF());
       programArgs.set(ois.readUTF());
       sourcesDir.set(ois.readUTF());
       outputDir.set(ois.readUTF());
       syncCounters.set(ois.readBoolean());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
