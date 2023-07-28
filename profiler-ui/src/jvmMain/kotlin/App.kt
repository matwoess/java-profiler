import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import javax.swing.JFileChooser
import javax.swing.JFrame

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Java Profiler",
        //icon = painterResource("profiler.svg"),
        state = WindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = DpSize(width = 700.dp, height = 1000.dp)
        ),
    ) {
        App()
    }
}

@Composable
@Preview
fun App() {
    val state = remember { AppState }
    AppTheme(state.getDarkMode()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                Header(toggleFn = { state.setDarkMode(!state.getDarkMode()) })
                Body(state)
            }
        }
    }
}

@Composable
fun Header(toggleFn: () -> Unit) {
    TopAppBar(elevation = 8.dp) {
        Row(
            modifier = Modifier.padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Java Profiler", style = TextStyle(
                    fontSize = MaterialTheme.typography.h6.fontSize,
                    fontWeight = FontWeight.Bold
                )
            )
            Box(modifier = Modifier.weight(1f))
            IconButton(onClick = toggleFn) {
                Icon(Icons.Default.Settings, contentDescription = "Toggle dark theme")
            }
        }
    }
}


@Composable
fun Body(state: AppState) {
    Column(modifier = Modifier.padding(all = 24.dp)) {
        RunMode(state)
        PathSelector("Main file:", state.getMainFile(), state::setMainFile, listOf("java"))
        if (state.getRunMode() == RunMode.Default) {
            ProgramArgs(state)
            PathSelector("Sources dir:", state.getSourcesDir(), state::setSourcesDir, listOf())
        }
        PathSelector("Output dir:", state.getOutputDir(), state::setOutputDir, listOf())
        Box(modifier = Modifier.height(24.dp))
        Row {
            if (state.getRunMode() != RunMode.ReportOnly) {
                Option("Synchronized counters", state.getSyncCounters(), state::setSyncCounters)
            }
            Option("Verbose output", state.getVerboseOutput(), state::setVerboseOutput)
            Box(modifier = Modifier.weight(1f))
            RunButton(state)
        }
        Box(modifier = Modifier.height(24.dp))
        ProgramOutput(state)
    }
}

@Composable
fun RunMode(state: AppState) {
    Text(
        text = "Run mode:",
        style = MaterialTheme.typography.h5
    )
    Column(modifier = Modifier.selectableGroup()) {
        RunMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (mode == state.getRunMode()),
                        onClick = { state.setRunMode(mode) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    modifier = Modifier.padding(end = 16.dp),
                    selected = (mode == state.getRunMode()),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Text(text = mode.toString())
            }
        }
    }
}

@Composable
fun PathSelector(title: String, value: String, onChange: (String) -> Unit, fileExtensions: List<String>) {
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)) {
        Text(text = title)
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = value,
                onValueChange = onChange,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Button(
                onClick = { openFileDialog(fileExtensions) },
                Modifier.align(Alignment.CenterVertically)
            ) {
                Text("Select")
            }
        }
    }
}

fun openFileDialog(fileExtensions: List<String>) {
    val frame = JFrame()
    var fc = JFileChooser()
    fc.showOpenDialog(frame)
}

@Composable
fun ProgramArgs(state: AppState) {
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)) {
        Text(text = "Program arguments:")
        Row {
            TextField(
                value = state.getProgramArgs(),
                onValueChange = { state.setProgramArgs(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun Option(text: String, checked: Boolean, setFn: (Boolean) -> Unit) {
    Row {
        Checkbox(
            checked = checked,
            onCheckedChange = setFn,
        )
        Text(
            text = text,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun RunButton(state: AppState) {
    Button(
        onClick = { runTool(state) },
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
    ) {
        Text("Run tool")
    }
}

@Composable
fun ProgramOutput(state: AppState) {
    Column {
        Text(text = "Program output:")
        TextField(
            value = state.getProgramOutput(),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}


fun runTool(state: AppState) {
    state.setProgramOutput("Running tool now...\nHere is the output.")
}