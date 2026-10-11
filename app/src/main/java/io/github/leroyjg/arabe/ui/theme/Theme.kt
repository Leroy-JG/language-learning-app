package io.github.leroyjg.arabe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.content.ActivityType

/** Police arabe embarquée : rendu identique sur tous les téléphones. */
val NotoNaskh = FontFamily(
    Font(R.font.noto_naskh_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_naskh_arabic_bold, FontWeight.Bold),
)

/** Couleurs propres à l'app, en plus de celles de Material. */
@Immutable
data class AppColors(
    val correct: Color,
    val correctContainer: Color,
    val wrong: Color,
    val wrongContainer: Color,
    val remark: Color,
    val remarkContainer: Color,
    val vocabulary: Color,
    val grammar: Color,
    val dialogue: Color,
    val review: Color,
    val quiz: Color,
) {
    fun forType(type: ActivityType): Color = when (type) {
        ActivityType.VOCABULARY -> vocabulary
        ActivityType.GRAMMAR -> grammar
        ActivityType.DIALOGUE -> dialogue
        ActivityType.REVIEW -> review
        ActivityType.QUIZ -> quiz
    }
}

private val LightAppColors = AppColors(
    correct = Color(0xFF15803D), correctContainer = Color(0xFFDCFCE7),
    wrong = Color(0xFFC62828), wrongContainer = Color(0xFFFDE2E2),
    remark = Color(0xFFB45309), remarkContainer = Color(0xFFFEF3C7),
    vocabulary = Color(0xFF0E9F6E), grammar = Color(0xFF7C3AED), dialogue = Color(0xFFEA580C),
    review = Color(0xFF2457D6), quiz = Color(0xFFDB2777),
)

private val DarkAppColors = AppColors(
    correct = Color(0xFF4ADE80), correctContainer = Color(0xFF14321F),
    wrong = Color(0xFFF87171), wrongContainer = Color(0xFF3B1517),
    remark = Color(0xFFFBBF24), remarkContainer = Color(0xFF3A2A0C),
    vocabulary = Color(0xFF34D399), grammar = Color(0xFFA78BFA), dialogue = Color(0xFFFB923C),
    review = Color(0xFF7AA2FF), quiz = Color(0xFFF472B6),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF2457D6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6FF),
    onPrimaryContainer = Color(0xFF0B2A73),
    secondary = Color(0xFFF2A516),
    onSecondary = Color(0xFF231600),
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF151821),
    surface = Color.White,
    onSurface = Color(0xFF151821),
    surfaceVariant = Color(0xFFEDEFF5),
    onSurfaceVariant = Color(0xFF545B6B),
    outline = Color(0xFFC9CEDA),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF7AA2FF),
    onPrimary = Color(0xFF0B1C4A),
    primaryContainer = Color(0xFF1C3678),
    onPrimaryContainer = Color(0xFFDCE6FF),
    secondary = Color(0xFFF7C35A),
    onSecondary = Color(0xFF2A1C00),
    background = Color(0xFF0F1218),
    onBackground = Color(0xFFE6E8EF),
    surface = Color(0xFF181C24),
    onSurface = Color(0xFFE6E8EF),
    surfaceVariant = Color(0xFF232836),
    onSurfaceVariant = Color(0xFFAAB1C3),
    outline = Color(0xFF3B4254),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

private val AppTypography = Typography().let { t ->
    t.copy(
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.Bold),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = t.bodyLarge.copy(fontSize = 17.sp, lineHeight = 26.sp),
        labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp),
    )
}

@Composable
fun ArabeTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppColors provides if (darkTheme) DarkAppColors else LightAppColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = AppTypography,
            content = content,
        )
    }
}

object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}
