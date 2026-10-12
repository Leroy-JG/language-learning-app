package io.github.leroyjg.arabe.ui.text

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.leroyjg.arabe.arabic.ArabicScript
import io.github.leroyjg.arabe.arabic.Markup
import io.github.leroyjg.arabe.ui.theme.NotoNaskh

/** Préférences de lecture de l'arabe (réglages de l'app). */
data class ReadingPrefs(
    val hideHarakat: Boolean = false,
    val arabicScale: Float = 1f,
    val showTranslit: Boolean = true,
    val autoPlay: Boolean = true,
)

val LocalReadingPrefs = staticCompositionLocalOf { ReadingPrefs() }

/** Masque les harakat si le réglage est actif, sauf pour un signe montré seul sur ◌. */
fun displayArabic(text: String, hideHarakat: Boolean): String =
    if (hideHarakat && ArabicScript.CIRCLE !in text) ArabicScript.withoutHarakat(text) else ArabicScript.nfc(text)

/**
 * Texte arabe seul : police Naskh, sens de droite à gauche, interligne large pour que les harakat
 * ne soient jamais coupées.
 */
@Composable
fun ArabicText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 26.sp,
    color: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Center,
    fontWeight: FontWeight = FontWeight.Normal,
    keepHarakat: Boolean = false,
) {
    val prefs = LocalReadingPrefs.current
    val scaled = size * prefs.arabicScale
    Text(
        text = displayArabic(text, prefs.hideHarakat && !keepHarakat),
        modifier = modifier,
        color = if (color == Color.Unspecified) LocalContentColor.current else color,
        style = arabicStyle(scaled).copy(textAlign = textAlign, fontWeight = fontWeight),
    )
}

fun arabicStyle(size: TextUnit): TextStyle = TextStyle(
    fontFamily = NotoNaskh,
    fontSize = size,
    lineHeight = size * 1.8f,
    textDirection = TextDirection.Rtl,
    platformStyle = PlatformTextStyle(includeFontPadding = true),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
)

private const val FSI = '\u2068' // isole un passage arabe dans une phrase française
private const val PDI = '\u2069'

private const val NBSP = '\u00A0'

/**
 * Typographie française : l'espace avant « ? ! : ; » et à l'intérieur des guillemets devient
 * insécable, pour qu'un retour à la ligne ne sépare jamais la ponctuation de son mot.
 */
fun frenchSpacing(text: String): String = text
    .replace(Regex(" (?=[?!:;»])"), NBSP.toString())
    .replace("« ", "«$NBSP")

/** Construit le texte affiché d'un texte français balisé (`{ar|…}`, `**gras**`). */
fun richText(text: String, hideHarakat: Boolean, arabicScale: Float = 1f): AnnotatedString {
    val parsed = Markup.parse(text)
    return buildAnnotatedString {
        for (segment in parsed.segments) {
            val weight = if (segment.bold) FontWeight.Bold else null
            if (segment.arabic) {
                append(FSI)
                withStyle(SpanStyle(fontFamily = NotoNaskh, fontSize = (1.2f * arabicScale).em, fontWeight = weight)) {
                    append(displayArabic(segment.text, hideHarakat))
                }
                append(PDI)
            } else if (weight != null) {
                withStyle(SpanStyle(fontWeight = weight)) { append(frenchSpacing(segment.text)) }
            } else {
                append(frenchSpacing(segment.text))
            }
        }
    }
}

/** Texte français pouvant contenir de l'arabe balisé : chaque passage arabe est isolé et lisible. */
@Composable
fun RichText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
    keepHarakat: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
) {
    val prefs = LocalReadingPrefs.current
    val hide = prefs.hideHarakat && !keepHarakat
    val annotated = remember(text, hide, prefs.arabicScale) { richText(text, hide, prefs.arabicScale) }
    val hasArabic = annotated.text.contains(FSI)
    Text(
        text = annotated,
        modifier = modifier,
        color = if (color == Color.Unspecified) LocalContentColor.current else color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = style.copy(
            textAlign = textAlign,
            textDirection = TextDirection.Ltr,
            lineHeight = if (hasArabic && style.fontSize != TextUnit.Unspecified) style.fontSize * 1.75f else style.lineHeight,
            platformStyle = PlatformTextStyle(includeFontPadding = true),
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
        ),
    )
}
