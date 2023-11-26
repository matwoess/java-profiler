# A Profiler for Java Programs

## Introduction
This project was created as part of my master's thesis at the Institute for System Software ([SSW](https://ssw.jku.at/)) of the Johannes Kepler University ([JKU](https://www.jku.at/)), located in Linz, Austria.

The profiler instruments source code directly (as opposed to commonly used bytecode instrumentation) by inserting counter statements at the beginning of every executable code block.
Blocks are found by parsing files with a Scanner+Parser generated by the [Coco/R](https://ssw.jku.at/Research/Projects/Coco/) tool.

The intention for the attributed Grammar (ATG), used in this project, was to be kept of minimal size and complexity by applying "fuzzy" parsing. This should improve maintainability with the constantly updating and growing [Java Language Specification](https://docs.oracle.com/javase/specs/jls/se17/html/index.html).

In the default mode, this tool instruments source files automatically, compiles them, runs the program (with arguments), and generates an HTML report. 
The report provides a structured and sorted overview over the number of method invocations by class. It further allows exploration of source files and shows coverage (with counts) of (grouped) statements regions.

The profiler was designed to be a command line tool that can easily be used in scripts, CI/CD pipelines and automations.

For Java/programming beginners, or users who are less familiar with the command line, a JavaFX tool-runner GUI was created (see [FxUI](#fxui) section).


## Download
The [Releases](https://github.com/matwoess/java-profiler/releases/) section contains downloadable `.jar`-archives for the tool and the FxUI tool-runner.

The UI has separate releases for Windows, Linux and MacOS.
The "distribution" zip archives provide executable scripts to directly start the GUI without using the command line.

## Building from source
To build the project from source, Gradle and a Java JDK of version 17 or newer are required.

After cloning or downloading the repository, the `Scanner.java` and `Parser.java` files must first be generated using the [Coco/R library](https://ssw.jku.at/Research/Projects/Coco/Java/Coco.jar).

This can be done automatically by running the provided `generate-parser.sh`  bash script or the `generate-parser.ps1` PowerShell script in the [scripts/](scripts/) folder. 

Both should automatically download the library and execute it on the project's [ATG file](profiler-tool/src/main/java/tool/instrument/JavaFile.atg).

This can also be done manually by downloading the library and executing the following command in the project root, to re-generate the parser files at any time:
```shell
java -jar lib/Coco.jar -package tool.instrument profiler-tool/src/main/java/tool/instrument/JavaFile.atg 
```
## Tool usage
```
Usage: profiler [options] <main file> [program args]  
Or   : profiler [options] <run mode>
```
A Java 17+ SDK is required to run the tool. Also the Java binaries should be included in the system environment path variable.

### Sample usage
(In the below section the `java -jar profiler.jar` command is substituted by `profiler`)

In the simplest case, the tool can be used as following:
```shell
profiler Main.java arg1 arg2 ...
```
This will parse the given file and create an instrumented copy in the `.profiler/instrumented/` folder. The first argument for the tool specifies the class containing the main entry point.
Additionally the `.profiler/metadata.dat` file will be created, containing information about the begin/end of every found block and its parent method and class.

The tool will automatically compile the instrumented version using `javac`.
```shell
javac Main.java
```
The Java compiler finds referenced Java files used in Main itself and will compile them also into (instrumented) `.class` files.

Next, the java binary will be used to execute the specified class by name (without the `.java` extension) with the specified arguments:
```
cd .profiler/instrumented/
java Main arg1 arg2 ...
```
Executing the instrumented files, automatically stores the hit-counter values in `.profiler/counts.dat`, as soon as the program ends (if at least 1 counter was inserted).

Finally, the metadata and counts will be used to create the report inside `.profiler/report/`.

All tool output is stored in the output-directory (default=`.profiler/`).

### Command line options
There are a few optional arguments available. For a full list, run `profiler -h` or `profiler --help`.

#### sources-directory
As soon as the project-to-profile consists of two or more (linked) Java files, the sources directory has to be specified.
This is done with the `-d` or `--sources-directory` option:
```shell
profiler -d src/main/java/ src/main/java/subfolder/Main.java
```

Using this option, all `.java` files inside `src/main/java/` will be parsed, instrumented and copied (relative) to the "instrumented" directory.

#### synchronized
When adding `-s` or `--synchronized` as a option, all inserted counters will be incremented atomically. This might be useful for multi-threaded programs, where a few methods or blocks are constantly executed in parallel.
It will ensure that hit counts are correct, but runtime performance will be impacted.

#### verbose
This option is mainly for debugging purposes. It can be activated with `-v` or `--verbose` and will output detailed information about the parsing process for each file.

### Run modes

The tool is primarily designed for easy usage with small projects that have a Main file. In case the project cannot be compiled with `javac Main.java`, or usage of build tools (like Maven, Gradle, or Ant), we cannot use the default compilation logic.

For this case, two additional run modes are available:

#### instrument-only

By specifying the `-i <file|dir>` or `--instrument-only <file|dir>` mode, the target file or directory, with all its Java files, will be instrumented and added to the `.profiler/instrumented/` directory. Also, the `metadata.dat` file is generated.

It can then be compiled by custom commands and run manually.
(automatic copying of `pom.xml` or gradle files is currently not done)

#### generate-report-only

If a project was already instrumented and run, the HTML report can be quickly (re-)generated with the `-r` or `--generate-report` run mode.
In this mode, no parsing or instrumentation will be done.
For it to succeed the `metadata.dat` and `counts.dat` files must already exist in the output directory.

## FxUI

A graphical application (using [JavaFX](https://openjfx.io/)) was created, to easily configure parameters and arguments to execute the tool with.

![FxUI preview](https://i.ibb.co/Tkcz5wX/Screenshot-from-2023-11-20-22-36-27.png)

The golden `(?)` labels can be hovered over for more information.

Using the file tree on the left, the sources directory and main file can be selected. Depending on the run mode this may be required.

Assigning a file or directory to a parameter can be done with the <kbd>Return</kbd> key, or using the context menu on a tree item.

The `.profiler/` output directory is highlighted in a brown color, the selected sources directory as blue, and the main file as green.

The menu bar allows saving and restoring currently set parameters (will be saved in the output directory as `parameters.dat`) and rebuilding the tree.

When clicking "Run tool", a  system-native terminal (can be chosen) will be opened to show program output and to allow user input (for interactive programs).

The executed command can be previewed with the "Preview command" button.

"Open report" will only show up once the `.profiler/report/index.html` file exists.

The UI uses the `PrimerDark` theme from [AtlantaFX](https://github.com/mkpaz/atlantafx) as a `userAgentStylesheet`.

## Report

// TODO

## Implementation details

// TODO

## Dependencies

// TODO

## Future work and ideas

// TODO