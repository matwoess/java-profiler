import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = if (darkTheme) darkColors(
            primary = Color(0xFFA4C9FF),
            onPrimary = Color(0xFF00315D),
            secondary = Color(0xFF50DBD0),
            onSecondary = Color(0xFF003734),
            background = Color(0xFF1A1C1E),
            onBackground = Color(0xFFE3E2E6),
            surface = Color(0xFF1A1C1E),
            onSurface = Color(0xFFE3E2E6),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
        ) else lightColors(
            primary = Color(0xFF1960A5),
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF006A64),
            onSecondary = Color(0xFFFFFFFF),
            background = Color(0xFFFDFCFF),
            onBackground = Color(0xFF1A1C1E),
            surface = Color(0xFFFDFCFF),
            onSurface = Color(0xFF1A1C1E),
            error = Color(0xFFBA1A1A),
            onError = Color(0xFFFFFFFF),
        ),
        content = content
    )
}