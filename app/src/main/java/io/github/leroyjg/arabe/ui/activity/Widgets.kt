package io.github.leroyjg.arabe.ui.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.ui.IconImages
import io.github.leroyjg.arabe.ui.theme.AppTheme

/** Icône « haut-parleur » (tracé Material Icons, licence Apache 2.0). */
val SpeakerIcon: ImageVector by lazy {
    ImageVector.Builder("speaker", 24.dp, 24.dp, 24f, 24f)
        .addPath(
            pathData = addPathNodes(
                "M3,9v6h4l5,5V4L7,9H3zM16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v8.05c1.48,-0.73 2.5,-2.25 2.5,-4.02z" +
                    "M14,3.23v2.06c2.89,0.86 5,3.54 5,6.71s-2.11,5.85 -5,6.71v2.06c4.01,-0.91 7,-4.49 7,-8.77s-2.99,-7.86 -7,-8.77z",
            ),
            fill = SolidColor(Color.Black),
        )
        .build()
}

@Composable
fun SpeakerButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(size),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Icon(SpeakerIcon, contentDescription = stringResource(R.string.listen), modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun ContentIcon(name: String?, modifier: Modifier = Modifier, size: Dp = 120.dp) {
    val res = IconImages.of(name) ?: return
    Image(painterResource(res), contentDescription = null, modifier = modifier.size(size))
}

/** État visuel d'un choix. */
enum class ChoiceState { IDLE, SELECTED, CORRECT, WRONG, DISABLED }

@Composable
fun ChoiceCard(
    state: ChoiceState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    val scheme = MaterialTheme.colorScheme
    val (border, background) = when (state) {
        ChoiceState.IDLE, ChoiceState.DISABLED -> scheme.outline to scheme.surface
        ChoiceState.SELECTED -> scheme.secondary to scheme.secondaryContainer
        ChoiceState.CORRECT -> colors.correct to colors.correctContainer
        ChoiceState.WRONG -> colors.wrong to colors.wrongContainer
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .alpha(if (state == ChoiceState.DISABLED) 0.45f else 1f),
        shape = RoundedCornerShape(16.dp),
        color = background,
        border = BorderStroke(if (state == ChoiceState.IDLE) 1.5.dp else 2.5.dp, border),
        onClick = onClick,
        enabled = state == ChoiceState.IDLE || state == ChoiceState.SELECTED,
    ) {
        Box(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), contentAlignment = Alignment.Center) { content() }
    }
}

/** Étiquette déplaçable (remise en ordre, texte à trous). */
@Composable
fun TokenChip(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    state: ChoiceState = ChoiceState.IDLE,
    invisible: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    val scheme = MaterialTheme.colorScheme
    val (border, background) = when (state) {
        ChoiceState.CORRECT -> colors.correct to colors.correctContainer
        ChoiceState.WRONG -> colors.wrong to colors.wrongContainer
        ChoiceState.SELECTED -> scheme.secondary to scheme.secondaryContainer
        else -> scheme.outline to scheme.surface
    }
    Box(
        modifier
            .alpha(if (invisible) 0f else 1f)
            .background(background, RoundedCornerShape(12.dp))
            .border(1.5.dp, border, RoundedCornerShape(12.dp))
            .then(if (onClick != null && !invisible) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(PaddingValues(horizontal = 14.dp, vertical = 4.dp)),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
fun BigPlayButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(96.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Icon(SpeakerIcon, contentDescription = stringResource(R.string.listen), modifier = Modifier.size(48.dp))
    }
}
