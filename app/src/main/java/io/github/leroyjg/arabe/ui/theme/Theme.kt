package io.github.leroyjg.arabe.ui.theme

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
import io.github.leroyjg.arabe.review.Mastery

/** Police arabe embarquée : rendu identique sur tous les téléphones. */
val NotoNaskh = FontFamily(
    Font(R.font.noto_naskh_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_naskh_arabic_bold, FontWeight.Bold),
)

/** Couleurs de la marque, identiques en thème clair et sombre (bandeaux en dégradé). */
object Brand {
    val Emerald = Color(0xFF0B7A4B)
    val EmeraldDeep = Color(0xFF064A2E)
    val Gold = Color(0xFFE5BE5C)
}

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
    val weak: Color,
    val medium: Color,
    val strong: Color,
) {
    fun forType(type: ActivityType): Color = when (type) {
        ActivityType.VOCABULARY -> vocabulary
        ActivityType.GRAMMAR -> grammar
        ActivityType.DIALOGUE -> dialogue
        ActivityType.REVIEW -> review
        ActivityType.QUIZ, ActivityType.TEST, ActivityType.EXAM -> quiz
    }

    fun forMastery(mastery: Mastery): Color = when (mastery) {
        Mastery.WEAK -> weak
        Mastery.MEDIUM -> medium
        Mastery.STRONG -> strong
    }
}

// Palette : vert émeraude (couleur associée à l'arabe) et or, sur un fond ivoire.

private val LightAppColors = AppColors(
    correct = Color(0xFF12864E), correctContainer = Color(0xFFD6F2E2),
    wrong = Color(0xFFC0392B), wrongContainer = Color(0xFFFBE3E0),
    remark = Color(0xFFB26A00), remarkContainer = Color(0xFFFCEFD6),
    vocabulary = Color(0xFF0E8C7A), grammar = Color(0xFF6A4C93), dialogue = Color(0xFFC25A2E),
    review = Color(0xFF2F6E9E), quiz = Color(0xFFB07D16),
    weak = Color(0xFFD0503F), medium = Color(0xFFE0A526), strong = Color(0xFF12864E),
)

private val DarkAppColors = AppColors(
    correct = Color(0xFF4FD08A), correctContainer = Color(0xFF173A27),
    wrong = Color(0xFFF08A80), wrongContainer = Color(0xFF3E1A17),
    remark = Color(0xFFF2B54A), remarkContainer = Color(0xFF3D2C0E),
    vocabulary = Color(0xFF3FC7B2), grammar = Color(0xFFB49BDB), dialogue = Color(0xFFEE9466),
    review = Color(0xFF7FB2DE), quiz = Color(0xFFE5BE5C),
    weak = Color(0xFFE8776A), medium = Color(0xFFE8BC50), strong = Color(0xFF4FD08A),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0B7A4B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEBDB),
    onPrimaryContainer = Color(0xFF00391F),
    secondary = Color(0xFFB07D16),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF6E7C1),
    onSecondaryContainer = Color(0xFF3D2A00),
    background = Color(0xFFF6F4EE),
    onBackground = Color(0xFF1B1F1C),
    surface = Color.White,
    onSurface = Color(0xFF1B1F1C),
    surfaceVariant = Color(0xFFECE9E0),
    onSurfaceVariant = Color(0xFF5A5E57),
    surfaceContainer = Color.White,
    surfaceContainerLow = Color(0xFFFBFAF6),
    surfaceContainerHigh = Color.White,
    outline = Color(0xFFCBC7BB),
    outlineVariant = Color(0xFFE3DFD4),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF5ED3A0),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF0F5236),
    onPrimaryContainer = Color(0xFFC9F2DD),
    secondary = Color(0xFFE5BE5C),
    onSecondary = Color(0xFF3D2A00),
    secondaryContainer = Color(0xFF5A4310),
    onSecondaryContainer = Color(0xFFF8E6B8),
    background = Color(0xFF0E1411),
    onBackground = Color(0xFFE3E8E4),
    surface = Color(0xFF161E1A),
    onSurface = Color(0xFFE3E8E4),
    surfaceVariant = Color(0xFF232D28),
    onSurfaceVariant = Color(0xFFA9B5AE),
    surfaceContainer = Color(0xFF161E1A),
    surfaceContainerLow = Color(0xFF121915),
    surfaceContainerHigh = Color(0xFF1C2520),
    outline = Color(0xFF3A4640),
    outlineVariant = Color(0xFF2C3631),
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
fun ArabeTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
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
