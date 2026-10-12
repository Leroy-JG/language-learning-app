package io.github.leroyjg.arabe.ui.activity

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

private class Piece(
    val x: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val ratio: Float,
    val spin: Float,
    val angle: Float,
    val color: Color,
    val round: Boolean,
)

/** Pluie de confettis (environ trois secondes) pour fêter une leçon réussie. */
@Composable
fun Confetti(colors: List<Color>, modifier: Modifier = Modifier, count: Int = 150) {
    val pieces = remember {
        val random = Random(System.nanoTime())
        List(count) {
            Piece(
                x = 0.2f + random.nextFloat() * 0.6f,
                vx = (random.nextFloat() - 0.5f) * 0.9f,
                vy = -0.4f - random.nextFloat() * 0.5f,
                size = 7f + random.nextFloat() * 7f,
                ratio = 0.45f + random.nextFloat() * 0.9f,
                spin = (random.nextFloat() - 0.5f) * 900f,
                angle = random.nextFloat() * 360f,
                color = colors[random.nextInt(colors.size)],
                round = random.nextInt(5) == 0,
            )
        }
    }
    val time = remember { Animatable(0f) }
    LaunchedEffect(Unit) { time.animateTo(DURATION, tween((DURATION * 1000).toInt(), easing = LinearEasing)) }
    Canvas(modifier) {
        val t = time.value
        if (t >= DURATION) return@Canvas
        val fade = if (t > DURATION - 0.8f) (DURATION - t) / 0.8f else 1f
        for (p in pieces) {
            // Lancés vers le haut depuis le tiers supérieur, puis retombent (gravité, freinage de l'air).
            val drag = 1f / (1f + 0.6f * t)
            val sway = sin(t * 6f + p.angle) * 6f * density
            val x = (p.x + p.vx * t * drag) * size.width + sway
            val y = (0.32f + p.vy * t + 0.5f * t * t) * size.height
            if (y > size.height + 40f) continue
            val w = p.size * density
            val h = w * p.ratio
            val c = p.color.copy(alpha = p.color.alpha * fade)
            rotate(p.angle + p.spin * t, pivot = Offset(x, y)) {
                if (p.round) drawCircle(c, radius = w / 2.4f, center = Offset(x, y))
                else drawRect(c, topLeft = Offset(x - w / 2, y - h / 2), size = Size(w, h))
            }
        }
    }
}

private const val DURATION = 3f
