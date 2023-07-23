import androidx.compose.runtime.mutableStateOf

object AppState {
    private var darkMode = mutableStateOf(false)
    private var runMode = mutableStateOf(RunMode.Default)
    private var mainFile = mutableStateOf("")
    private var programArgs = mutableStateOf("")
    private var sourcesDir = mutableStateOf("")
    private var outputDir = mutableStateOf("")
    private var syncCounters = mutableStateOf(false)
    private var verboseOutput = mutableStateOf(false)
    private var programOutput = mutableStateOf("")

    fun getDarkMode() = darkMode.value
    fun setDarkMode(on: Boolean) {
        darkMode.value = on
    }

    fun getRunMode() = runMode.value
    fun setRunMode(mode: RunMode) {
        runMode.value = mode
    }

    fun getMainFile() = mainFile.value
    fun setMainFile(pathString: String) {
        mainFile.value = pathString
    }

    fun getProgramArgs() = mainFile.value
    fun setProgramArgs(args: String) {
        programArgs.value = args
    }

    fun getSourcesDir() = sourcesDir.value
    fun setSourcesDir(pathString: String) {
        sourcesDir.value = pathString
    }

    fun getOutputDir() = outputDir.value
    fun setOutputDir(pathString: String) {
        outputDir.value = pathString
    }

    fun getSyncCounters() = syncCounters.value
    fun setSyncCounters(enabled: Boolean) {
        syncCounters.value = enabled
    }

    fun getVerboseOutput() = verboseOutput.value
    fun setVerboseOutput(enabled: Boolean) {
        verboseOutput.value = enabled
    }

    fun getProgramOutput() = programOutput.value
    fun setProgramOutput(output: String) {
        programOutput.value = output
    }
}

enum class RunMode {
    Default, InstrumentOnly, ReportOnly;

    override fun toString(): String {
        return when (this) {
            Default -> "Instrument, compile, run, report"
            InstrumentOnly -> "Instrument only"
            ReportOnly -> "Generate report only"
        }
    }
}