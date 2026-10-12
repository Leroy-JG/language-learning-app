package io.github.leroyjg.arabe.ui.activity

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.arabic.AnswerMatcher
import io.github.leroyjg.arabe.audio.Sound
import io.github.leroyjg.arabe.audio.SoundEffects
import io.github.leroyjg.arabe.audio.rememberHaptics
import io.github.leroyjg.arabe.content.Item
import io.github.leroyjg.arabe.content.SortCard
import io.github.leroyjg.arabe.content.Txt
import io.github.leroyjg.arabe.engine.MemoryFace
import io.github.leroyjg.arabe.engine.PreparedBuild
import io.github.leroyjg.arabe.engine.PreparedMemory
import io.github.leroyjg.arabe.engine.PreparedSort
import io.github.leroyjg.arabe.engine.PreparedSpot
import io.github.leroyjg.arabe.ui.LocalAppContainer
import io.github.leroyjg.arabe.ui.eightPointStar
import io.github.leroyjg.arabe.ui.text.ArabicText
import io.github.leroyjg.arabe.ui.text.RichText
import io.github.leroyjg.arabe.ui.theme.AppTheme
import kotlinx.coroutines.delay

// ---------------------------------------------------------------- ranger

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SortContent(p: PreparedSort, vm: PlayerViewModel, playback: Playback) {
    val sounds = LocalAppContainer.current.sounds
    val haptics = rememberHaptics()
    val current = vm.sortDone.size
    LaunchedEffect(vm.sortWrong) {
        if (vm.sortWrong != null) {
            haptics.error()
            delay(650)
            vm.sortWrong = null
        }
    }
    // Petit son à chaque carte bien rangée (la dernière est saluée par le panneau de correction).
    LaunchedEffect(current) {
        if (current in 1 until p.cards.size) {
            sounds.play(SoundEffects.Effect.PAIR)
            haptics.tick()
        }
    }
    // Une carte en arabe se fait entendre dès qu'elle apparaît.
    LaunchedEffect(current) {
        val sound = p.cards.getOrNull(current)?.let { sortSound(it, p.items[current]) }
        if (sound != null && vm.feedback == null) {
            delay(250)
            playback.play(sound)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        GameInstruction(p.screen.instruction ?: stringResource(R.string.instruction_sort))
        ContextView(p.screen, playback)
        Text(
            if (current < p.cards.size) stringResource(R.string.sort_progress, current + 1, p.cards.size)
            else stringResource(R.string.sort_done),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AnimatedContent(
            targetState = current,
            transitionSpec = {
                (slideInVertically(spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)) { -it / 2 } + fadeIn())
                    .togetherWith(fadeOut(tween(120)))
            },
            label = "carte",
        ) { index ->
            val card = p.cards.getOrNull(index)
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                if (card != null) {
                    val item = p.items[index]
                    Surface(
                        onClick = { sortSound(card, item)?.let { playback.play(it) } },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth(0.8f).height(108.dp),
                    ) {
                        Box(Modifier.padding(12.dp), contentAlignment = Alignment.Center) { SortCardText(card, item, size = 34) }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            p.screen.categories.forEachIndexed { k, category ->
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceCard(
                        state = if (vm.sortWrong == k) ChoiceState.WRONG else ChoiceState.IDLE,
                        onClick = { vm.tapSort(k) },
                        modifier = Modifier.defaultMinSize(minHeight = 72.dp),
                    ) { CategoryLabel(category) }
                    // Les cartes déjà rangées dans cette catégorie.
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        FlowRow(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            vm.sortDone.forEachIndexed { i, chosen ->
                                if (chosen == k) {
                                    TokenChip(onClick = null, state = ChoiceState.CORRECT) {
                                        SortCardText(p.cards[i], p.items[i], size = 20)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sortSound(card: SortCard, item: Item?): Sound? = when {
    item != null -> Sound.OfItem(item)
    card.ar != null -> Sound.OfText(card.ar!!)
    else -> null
}

@Composable
private fun SortCardText(card: SortCard, item: Item?, size: Int) {
    when {
        item != null -> ArabicText(item.ar, size = size.sp)
        card.ar != null -> ArabicText(card.ar!!, size = size.sp)
        else -> RichText(card.fr.orEmpty(), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CategoryLabel(t: Txt) {
    if (t.ar != null) ArabicText(t.ar!!, size = 28.sp)
    else RichText(t.fr.orEmpty(), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
}

@Composable
private fun GameInstruction(text: String) {
    RichText(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

// ---------------------------------------------------------------- repérer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpotContent(p: PreparedSpot, vm: PlayerViewModel, playback: Playback) {
    val checked = vm.feedback != null
    val answers = p.screen.answers.toSet()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        GameInstruction(p.screen.instruction)
        ContextView(p.screen, playback)
        SpeakerButton(onClick = { playback.play(Sound.OfText(p.sentence)) })
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                p.screen.words.forEachIndexed { i, word ->
                    val selected = i in vm.spotSelected
                    val state = when {
                        !checked -> if (selected) ChoiceState.SELECTED else ChoiceState.IDLE
                        i in answers -> ChoiceState.CORRECT
                        selected -> ChoiceState.WRONG
                        else -> ChoiceState.IDLE
                    }
                    TokenChip(
                        onClick = if (!checked) ({ vm.toggleSpot(i) }) else null,
                        state = state,
                        modifier = Modifier.answerAnimation(state),
                    ) { ArabicText(word, size = 30.sp) }
                }
            }
        }
        p.screen.fr?.let {
            RichText(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

// ---------------------------------------------------------------- écrire un mot

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuildContent(p: PreparedBuild, vm: PlayerViewModel, playback: Playback) {
    val checked = vm.feedback != null
    val correct = vm.feedback?.verdict == AnswerMatcher.Verdict.CORRECT
    val haptics = rememberHaptics()
    val eraseLabel = stringResource(R.string.build_erase)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        GameInstruction(p.screen.instruction ?: stringResource(R.string.instruction_build))
        ContextView(p.screen, playback)
        if (p.screen.context == null) ContentIcon(p.target.icon, size = 96.dp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SpeakerButton(onClick = { playback.play(Sound.OfItem(p.target)) })
            RichText(p.target.fr, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        }
        // Le mot en train de s'écrire : les lettres se lient d'elles-mêmes.
        val written = vm.buildPicked.joinToString("") { p.tiles[it] }
        val (border, background) = when {
            !checked -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.surface
            correct -> AppTheme.colors.correct to AppTheme.colors.correctContainer
            else -> AppTheme.colors.wrong to AppTheme.colors.wrongContainer
        }
        Row(
            Modifier.fillMaxWidth()
                .defaultMinSize(minHeight = 84.dp)
                .background(background, RoundedCornerShape(16.dp))
                .border(1.5.dp, border, RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    if (!checked && vm.buildPicked.isNotEmpty()) {
                        vm.buildPicked.removeAt(vm.buildPicked.lastIndex)
                        haptics.tick()
                    }
                },
                enabled = !checked && vm.buildPicked.isNotEmpty(),
                modifier = Modifier.semantics { contentDescription = eraseLabel },
            ) {
                Text("⌫", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ArabicText(written, size = 40.sp, modifier = Modifier.weight(1f).semantics { contentDescription = written })
            Spacer(Modifier.width(48.dp))
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                p.tiles.forEachIndexed { i, letter ->
                    val used = i in vm.buildPicked
                    TokenChip(
                        onClick = {
                            if (!checked && !used) {
                                vm.buildPicked.add(i)
                                haptics.tick()
                            }
                        },
                        invisible = used,
                        modifier = Modifier.defaultMinSize(minWidth = 56.dp, minHeight = 56.dp),
                    ) { ArabicText(letter, size = 30.sp) }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- mémoire

@Composable
fun MemoryContent(p: PreparedMemory, vm: PlayerViewModel, playback: Playback) {
    val sounds = LocalAppContainer.current.sounds
    val haptics = rememberHaptics()
    // Deux cartes qui ne vont pas ensemble se referment après un instant.
    LaunchedEffect(vm.memoryOpen.size) {
        if (vm.memoryOpen.size == 2) {
            delay(900)
            vm.closeMemory()
        }
    }
    LaunchedEffect(vm.memoryFound.size) {
        if (vm.memoryFound.isNotEmpty() && vm.memoryFound.size < p.pairCount) {
            sounds.play(SoundEffects.Effect.PAIR)
            haptics.tick()
        }
    }
    val columns = if (p.cards.size % 3 == 0) 3 else 4
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        GameInstruction(p.screen.instruction ?: stringResource(R.string.instruction_memory))
        ContextView(p.screen, playback)
        Text(
            stringResource(R.string.memory_progress, vm.memoryFound.size, p.pairCount),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            p.cards.indices.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { i ->
                        val card = p.cards[i]
                        val found = card.item.id in vm.memoryFound
                        MemoryTile(
                            faceUp = found || i in vm.memoryOpen,
                            found = found,
                            onClick = {
                                if (card.face == MemoryFace.ARABIC && !found && i !in vm.memoryOpen) {
                                    playback.play(Sound.OfItem(card.item))
                                }
                                haptics.tick()
                                vm.tapMemory(i)
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            when (card.face) {
                                MemoryFace.ARABIC -> ArabicText(card.item.ar, size = 22.sp)
                                MemoryFace.FRENCH -> RichText(card.item.fr, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                                MemoryFace.ICON -> ContentIcon(card.item.icon, size = 52.dp)
                            }
                        }
                    }
                    // Dernière ligne incomplète : garde la largeur des cartes.
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun MemoryTile(
    faceUp: Boolean,
    found: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val rotation by animateFloatAsState(
        if (faceUp) 180f else 0f,
        spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "retournement",
    )
    val scheme = MaterialTheme.colorScheme
    val colors = AppTheme.colors
    val starColor = scheme.onPrimary.copy(alpha = 0.35f)
    val hidden = stringResource(R.string.memory_card_hidden)
    Box(
        modifier
            .aspectRatio(0.82f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !faceUp, onClick = onClick),
    ) {
        if (rotation <= 90f) {
            // Dos de la carte : couleur de l'app et étoile à huit branches.
            Box(
                Modifier.fillMaxSize()
                    .background(scheme.primary, RoundedCornerShape(14.dp))
                    .drawBehind {
                        eightPointStar(Offset(size.width / 2, size.height / 2), size.minDimension * 0.28f, starColor, 2.dp.toPx())
                    }
                    .semantics { contentDescription = hidden },
            )
        } else {
            // Face : retournée à nouveau pour ne pas être lue à l'envers.
            Box(
                Modifier.fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .background(if (found) colors.correctContainer else scheme.surface, RoundedCornerShape(14.dp))
                    .border(if (found) 2.5.dp else 1.5.dp, if (found) colors.correct else scheme.secondary, RoundedCornerShape(14.dp))
                    .padding(6.dp),
                contentAlignment = Alignment.Center,
            ) { content() }
        }
    }
}
