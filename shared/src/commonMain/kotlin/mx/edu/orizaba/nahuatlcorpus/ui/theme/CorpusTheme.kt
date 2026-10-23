package mx.edu.orizaba.nahuatlcorpus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CorpusColors = lightColorScheme(
    primary = Color(0xFF245C3D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF245C3D),
    onPrimaryContainer = Color(0xFF183D29),
    secondary = Color(0xFF52674D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF52674D),
    onSecondaryContainer = Color(0xFF263B24),
    background = Color(0xFFFAF9F3),
    onBackground = Color(0xFF20362A),
    surface = Color(0xFFFAF9F3),
    onSurface = Color(0xFF20362A),
    surfaceVariant = Color(0xFFE8EBDD),
    onSurfaceVariant = Color(0xFF526052),
    outline = Color(0xFF71836C),
    outlineVariant = Color(0xFFD2DCCB)
    )

@Composable
fun CorpusTheme(content: @Composable () -> Unit,){
    MaterialTheme(
        colorScheme = CorpusColors,
        typography = Typography(),
        content = content
    )
}