package fxui.model;

import common.IO;
import common.Util;
import javafx.beans.property.*;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fxui.model.RunMode.*;

public class Parameters implements Serializable {
  public ObjectProperty<RunMode> runMode = new SimpleObjectProperty<>(DEFAULT);

  public StringProperty mainFile = new SimpleStringProperty("");
  public StringProperty programArgs = new SimpleStringProperty("");
  public StringProperty sourcesDir = new SimpleStringProperty("");
  public StringProperty outputDir = new SimpleStringProperty("");
  public BooleanProperty syncCounters = new SimpleBooleanProperty(false);
  public BooleanProperty verboseOutput = new SimpleBooleanProperty(false);

  public BooleanProperty invalidMainFilePath = new SimpleBooleanProperty(true);
  public BooleanProperty invalidOutDirPath = new SimpleBooleanProperty(false);
  public BooleanProperty invalidSourcesDirPath = new SimpleBooleanProperty(false);

  public Parameters() {
    initializeExtraProperties();
  }

  public void initializeExtraProperties() {
    mainFile.addListener((observable, oldValue, newValue) -> {
      if (!newValue.isBlank()) {
        Path newPath = Path.of(newValue);
        invalidMainFilePath.set(!Util.isJavaFile(newPath));
      } else {
        invalidMainFilePath.set(runMode.get() != REPORT_ONLY);
      }
    });
    sourcesDir.addListener((observable, oldValue, newValue) -> {
      if (newValue.isBlank()) {
        invalidSourcesDirPath.set(false);
      } else {
        Path newPath = Path.of(newValue);
        invalidSourcesDirPath.set(!newPath.toFile().isDirectory());
      }
    });
    outputDir.addListener((observable, oldValue, newValue) -> {
      if (newValue.isBlank()) {
        invalidOutDirPath.set(false);
      } else {
        Path newPath = Path.of(newValue);
        IO.outputDir = newPath;
        invalidOutDirPath.set(!newPath.toFile().isDirectory());
      }
    });
  }

  public String[] getRunCommand() {
    RunMode mode = runMode.get();
    List<String> arguments = new ArrayList<>();
    String outDir = outputDir.get();
    if (!outDir.isBlank()) {
      arguments.add("--out-directory");
      arguments.add(outDir);
    }
    boolean verbose = verboseOutput.get();
    if (verbose) {
      arguments.add("--verbose");
    }
    boolean sync = syncCounters.get();
    if (sync && mode != REPORT_ONLY) {
      arguments.add("--sync-counters");
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
}
