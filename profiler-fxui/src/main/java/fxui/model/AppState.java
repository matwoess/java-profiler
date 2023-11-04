package fxui.model;

import common.IO;
import fxui.util.BindingUtils;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static common.IO.getUIParametersPath;
import static fxui.model.RunMode.DEFAULT;
import static fxui.model.RunMode.REPORT_ONLY;

public class AppState {
  public ObjectProperty<Path> projectRoot = new SimpleObjectProperty<>(null);

  public ObjectProperty<RunMode> runMode = new SimpleObjectProperty<>(DEFAULT);

  public ObjectProperty<Path> mainFile = new SimpleObjectProperty<>(null);
  public StringProperty programArgs = new SimpleStringProperty("");
  public ObjectProperty<Path> sourcesDir = new SimpleObjectProperty<>(null);
  public BooleanProperty syncCounters = new SimpleBooleanProperty(false);
  public ObjectProperty<Terminal> terminal = new SimpleObjectProperty<>(Terminal.getDefaultSystemTerminal());

  public BooleanBinding invalidMainFilePath;
  public BooleanBinding invalidSourcesDirPath;
  public BooleanBinding reportIndexFileExists;
  public BooleanBinding parametersFileExists;
  public BooleanBinding metadataFileExists;
  public BooleanBinding countsFileExists;

  public AppState() {
    initializeAdditionalProperties();
  }

  public void initializeAdditionalProperties() {
    invalidMainFilePath = mainFile.isNotNull().and(BindingUtils.creatRelativeIsJavaFileBinding(projectRoot, mainFile).not());
    invalidSourcesDirPath = sourcesDir.isNotNull().and(BindingUtils.createRelativeIsDirectoryBinding(projectRoot, sourcesDir).not());
    reportIndexFileExists = BindingUtils.creatRelativeFileExistsBinding(projectRoot, IO.getReportIndexPath());
    parametersFileExists = BindingUtils.creatRelativeFileExistsBinding(projectRoot, IO.getUIParametersPath());
    metadataFileExists = BindingUtils.creatRelativeFileExistsBinding(projectRoot, IO.getMetadataPath());
    countsFileExists = BindingUtils.creatRelativeFileExistsBinding(projectRoot, IO.getCountsPath());
  }

  public void invalidateFileBindings() {
    reportIndexFileExists.invalidate();
    metadataFileExists.invalidate();
    countsFileExists.invalidate();
    parametersFileExists.invalidate();
  }

  public String[] getProgramArguments() {
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
      oos.writeUTF(sourcesDir.get() != null ? sourcesDir.get().toString() : "");
      oos.writeUTF(mainFile.get() != null ? mainFile.get().toString() : "");
      oos.writeUTF(programArgs.get());
      oos.writeBoolean(syncCounters.get());
      oos.writeInt(terminal.get().ordinal());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void importParameters() {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getUIParametersPath().toFile()))) {
      runMode.set(RunMode.values()[ois.readInt()]);
      String sourcesDirVal = ois.readUTF();
      sourcesDir.set(sourcesDirVal.isBlank() ? null : Path.of(sourcesDirVal));
      String mainFileVal = ois.readUTF();
      mainFile.set(mainFileVal.isBlank() ? null : Path.of(mainFileVal));
      programArgs.set(ois.readUTF());
      syncCounters.set(ois.readBoolean());
      terminal.set(Terminal.values()[ois.readInt()]);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
