package io.github.leroyjg.arabe.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.content.Gender
import io.github.leroyjg.arabe.ui.text.ArabicText

/** Symboles ♂ et ♀ (traits arrondis). */
private object GenderIcons {
    val Masculine: ImageVector by lazy {
        stroke("masculin", "M4.5,14a5.5,5.5 0 1,0 11,0a5.5,5.5 0 1,0 -11,0Z M13.9,10.1L20,4 M14.5,4H20V9.5")
    }
    val Feminine: ImageVector by lazy {
        stroke("feminin", "M6.5,9a5.5,5.5 0 1,0 11,0a5.5,5.5 0 1,0 -11,0Z M12,14.5V22 M8.5,18.5H15.5")
    }

    private fun stroke(name: String, path: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
            .addPath(
                pathData = addPathNodes(path),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            .build()
}

/**
 * Genre grammatical d'un nom : petite étiquette discrète (symbole, mot français, mot arabe),
 * information secondaire sous la traduction.
 */
@Composable
fun GenderTag(gender: Gender, modifier: Modifier = Modifier) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val label = stringResource(if (gender == Gender.MASCULINE) R.string.gender_masculine else R.string.gender_feminine)
    val arabic = if (gender == Gender.MASCULINE) "مُذَكَّرٌ" else "مُؤَنَّثٌ"
    Row(
        modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
            .padding(start = 8.dp, end = 10.dp)
            .clearAndSetSemantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            if (gender == Gender.MASCULINE) GenderIcons.Masculine else GenderIcons.Feminine,
            contentDescription = null,
            tint = muted,
            modifier = Modifier.size(14.dp),
        )
        Text(label, style = MaterialTheme.typography.labelMedium, color = muted)
        Text("·", style = MaterialTheme.typography.labelMedium, color = muted.copy(alpha = 0.6f))
        ArabicText(arabic, size = 15.sp, color = muted)
    }
}
