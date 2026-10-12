package io.github.leroyjg.arabe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import io.github.leroyjg.arabe.review.Mastery
import io.github.leroyjg.arabe.ui.theme.AppTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Icônes de la barre de navigation (tracés dessinés pour l'app). */
object AppIcons {
    /** Livre ouvert : « Apprendre ». */
    val Book: ImageVector by lazy {
        icon(
            "book",
            "M11,6.2C9.4,5 7.3,4.5 4.8,4.5C3.9,4.5 3,4.6 2.2,4.8C1.8,4.9 1.5,5.2 1.5,5.6V18.6C1.5,19.1 2,19.5 2.5,19.4" +
                "C3.2,19.3 4,19.2 4.8,19.2C7.1,19.2 9.2,19.8 11,20.9Z" +
                "M13,6.2C14.6,5 16.7,4.5 19.2,4.5C20.1,4.5 21,4.6 21.8,4.8C22.2,4.9 22.5,5.2 22.5,5.6V18.6C22.5,19.1 22,19.5 21.5,19.4" +
                "C20.8,19.3 20,19.2 19.2,19.2C16.9,19.2 14.8,19.8 13,20.9Z",
        )
    }

    /** Barres qui montent : « Réviser ». */
    val Chart: ImageVector by lazy {
        icon("chart", "M3,13h4.5v8H3Z M9.75,8.5h4.5V21h-4.5Z M16.5,3h4.5v18h-4.5Z")
    }

    private fun icon(name: String, path: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
            .addPath(pathData = addPathNodes(path), fill = SolidColor(Color.Black))
            .build()
}

/**
 * Étoile à huit branches (khatam), motif de l'art islamique : décor discret des cartes.
 * [center] et [radius] en pixels.
 */
fun DrawScope.eightPointStar(center: Offset, radius: Float, color: Color, strokeWidth: Float) {
    fun star(r: Float): Path {
        val inner = r * (cos(PI / 4) / cos(PI / 8)).toFloat()
        val path = Path()
        for (i in 0 until 16) {
            val angle = (i * PI / 8 - PI / 2).toFloat()
            val rr = if (i % 2 == 0) r else inner
            val x = center.x + rr * cos(angle)
            val y = center.y + rr * sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }
    drawPath(star(radius), color, style = Stroke(width = strokeWidth))
    drawPath(star(radius * 0.62f), color, style = Stroke(width = strokeWidth))
    drawCircle(color, radius = radius * 0.3f, center = center, style = Stroke(width = strokeWidth))
}

/** Trois barres de maîtrise, à la Busuu : rouge (à travailler), jaune (à revoir), vert (acquis). */
@Composable
fun MasteryBars(mastery: Mastery?, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val filled = when (mastery) {
        null -> 0
        Mastery.WEAK -> 1
        Mastery.MEDIUM -> 2
        Mastery.STRONG -> 3
    }
    val color = mastery?.let { colors.forMastery(it) } ?: Color.Transparent
    val empty = MaterialTheme.colorScheme.outline
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
        listOf(9, 14, 19).forEachIndexed { i, h ->
            Box(
                Modifier.width(5.dp).height(h.dp).background(if (i < filled) color else empty, RoundedCornerShape(2.dp)),
            )
        }
    }
}
