@file:OptIn(ExperimentalLayoutApi::class)

package io.github.leroyjg.arabe.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.arabic.AnswerMatcher
import io.github.leroyjg.arabe.audio.AudioPlayer
import io.github.leroyjg.arabe.audio.Sound
import io.github.leroyjg.arabe.audio.SoundEffects
import io.github.leroyjg.arabe.audio.rememberHaptics
import io.github.leroyjg.arabe.content.BiText
import io.github.leroyjg.arabe.content.BreakdownBlock
import io.github.leroyjg.arabe.content.CompareBlock
import io.github.leroyjg.arabe.content.Course
import io.github.leroyjg.arabe.content.DialogueScreen
import io.github.leroyjg.arabe.content.ExampleBlock
import io.github.leroyjg.arabe.content.Exercise
import io.github.leroyjg.arabe.content.GalleryBlock
import io.github.leroyjg.arabe.content.Item
import io.github.leroyjg.arabe.content.MatchMode
import io.github.leroyjg.arabe.content.McqMode
import io.github.leroyjg.arabe.content.NoteBlock
import io.github.leroyjg.arabe.content.NoteTone
import io.github.leroyjg.arabe.content.ParagraphBlock
import io.github.leroyjg.arabe.content.TableBlock
import io.github.leroyjg.arabe.content.TipScreen
import io.github.leroyjg.arabe.content.Txt
import io.github.leroyjg.arabe.content.TypingAsk
import io.github.leroyjg.arabe.engine.PreparedFillGap
import io.github.leroyjg.arabe.engine.PreparedMatch
import io.github.leroyjg.arabe.engine.PreparedMcq
import io.github.leroyjg.arabe.engine.PreparedMulti
import io.github.leroyjg.arabe.engine.PreparedOrder
import io.github.leroyjg.arabe.engine.PreparedQuestion
import io.github.leroyjg.arabe.engine.PreparedTrueFalse
import io.github.leroyjg.arabe.engine.PreparedTyping
import io.github.leroyjg.arabe.ui.GenderTag
import io.github.leroyjg.arabe.ui.LocalAppContainer
import io.github.leroyjg.arabe.ui.text.ArabicText
import io.github.leroyjg.arabe.ui.text.LocalReadingPrefs
import io.github.leroyjg.arabe.ui.text.RichText
import io.github.leroyjg.arabe.ui.text.arabicStyle
import io.github.leroyjg.arabe.ui.theme.AppTheme
import kotlinx.coroutines.delay

/** Ce dont les écrans ont besoin pour jouer les sons. */
class Playback(val course: Course, val audio: AudioPlayer) {
    fun play(sound: Sound, onDone: (() -> Unit)? = null) = audio.play(course, sound, onDone)
    fun stop() = audio.stop()
}

@Composable
private fun Instruction(text: String) {
    RichText(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Joue automatiquement un son à l'affichage de l'écran (réglage « lecture automatique »). */
@Composable
private fun AutoPlay(key: Any, playback: Playback, sound: Sound?) {
    val auto = LocalReadingPrefs.current.autoPlay
    LaunchedEffect(key) {
        if (auto && sound != null) {
            delay(250)
            playback.play(sound)
        }
    }
}

/** Arabe en grand avec son bouton d'écoute. */
@Composable
private fun SpokenArabic(text: String, onPlay: () -> Unit, size: Int = 34) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SpeakerButton(onClick = onPlay)
        ArabicText(text, size = size.sp, modifier = Modifier.weight(1f, fill = false))
    }
}

// ---------------------------------------------------------------- présentation

@Composable
fun PresentContent(item: Item, playback: Playback, turn: Int, saved: Boolean, onToggleSaved: () -> Unit) {
    val prefs = LocalReadingPrefs.current
    AutoPlay(turn, playback, Sound.OfItem(item))
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.fillMaxWidth()) {
            ContentIcon(item.icon, size = 132.dp, modifier = Modifier.align(Alignment.Center))
            IconButton(onClick = onToggleSaved, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(
                    if (saved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(if (saved) R.string.saved_remove else R.string.saved_add),
                    tint = if (saved) AppTheme.colors.weak else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        SpokenArabic(item.ar, onPlay = { playback.play(Sound.OfItem(item)) }, size = 42)
        if (prefs.showTranslit && item.translit != null) {
            Text(item.translit!!, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RichText(item.fr, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        item.gender?.let { GenderTag(it) }
        item.plural?.let { plural ->
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.plural_label), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    ArabicText(plural, size = 24.sp)
                    Spacer(Modifier.width(8.dp))
                    SpeakerButton(onClick = { playback.play(Sound.OfText(plural, item.voice)) }, size = 36.dp)
                }
            }
        }
        item.note?.let { note -> InfoCard { RichText(note, style = MaterialTheme.typography.bodyMedium) } }
        item.example?.let { ex ->
            InfoCard {
                Text(stringResource(R.string.example_label), style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SpeakerButton(onClick = { playback.play(Sound.OfText(ex.ar, item.voice)) }, size = 36.dp)
                    Spacer(Modifier.width(8.dp))
                    ArabicText(ex.ar, size = 24.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                }
                RichText(ex.fr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
    }
}

// ---------------------------------------------------------------- astuce

@Composable
fun TipContent(screen: TipScreen, playback: Playback) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        RichText(screen.title, style = MaterialTheme.typography.titleLarge, keepHarakat = true)
        for (block in screen.blocks) when (block) {
            is ParagraphBlock -> RichText(block.text, style = MaterialTheme.typography.bodyLarge, keepHarakat = true)
            is ExampleBlock -> ExampleCard(block, playback)
            is TableBlock -> TipTable(block)
            is NoteBlock -> NoteBox(block)
            is CompareBlock -> CompareView(block, playback)
            is BreakdownBlock -> BreakdownView(block, playback)
            is GalleryBlock -> GalleryView(block, playback)
        }
    }
}

/** Encadré : neutre, « Attention », « Pour retenir » ou « Le savais-tu ? ». */
@Composable
private fun NoteBox(block: NoteBlock) {
    val colors = AppTheme.colors
    val scheme = MaterialTheme.colorScheme
    val (label, accent, background) = when (block.tone) {
        NoteTone.INFO -> Triple(null, colors.remark, colors.remarkContainer)
        NoteTone.WARNING -> Triple(R.string.note_warning, colors.wrong, colors.wrongContainer)
        NoteTone.MEMO -> Triple(R.string.note_memo, scheme.secondary, scheme.secondaryContainer)
        NoteTone.CULTURE -> Triple(R.string.note_culture, scheme.tertiary, scheme.tertiaryContainer)
    }
    Row(
        Modifier.fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(background, RoundedCornerShape(14.dp)),
    ) {
        if (label != null) {
            Box(Modifier.width(5.dp).fillMaxHeight().background(accent, RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)))
        }
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            label?.let { Text(stringResource(it), style = MaterialTheme.typography.labelLarge, color = accent) }
            RichText(block.text, style = MaterialTheme.typography.bodyMedium, keepHarakat = true)
        }
    }
}

/** Formes côte à côte : avant → après, chacune écoutable. */
@Composable
private fun CompareView(block: CompareBlock, playback: Playback) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        block.title?.let { RichText(it, style = MaterialTheme.typography.titleSmall, keepHarakat = true) }
        block.rows.forEach { row ->
            Column(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CompareSide(row.from, playback, scheme.surface, Modifier.weight(1f))
                    Text("→", style = MaterialTheme.typography.titleLarge, color = scheme.primary, modifier = Modifier.padding(horizontal = 8.dp))
                    CompareSide(row.to, playback, scheme.primaryContainer, Modifier.weight(1f))
                }
                row.label?.let {
                    RichText(it, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, keepHarakat = true, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun CompareSide(text: BiText, playback: Playback, background: Color, modifier: Modifier) {
    Column(
        modifier
            .background(background, RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .clickable { playback.play(Sound.OfText(text.ar)) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ArabicText(text.ar, size = 26.sp, keepHarakat = true)
        RichText(text.fr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

/** Un mot ou une phrase et ses morceaux (de droite à gauche), chacun avec son sens. */
@Composable
private fun BreakdownView(block: BreakdownBlock, playback: Playback) {
    val scheme = MaterialTheme.colorScheme
    val tints = listOf(scheme.primaryContainer, scheme.secondaryContainer, scheme.tertiaryContainer)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SpokenArabic(block.ar, onPlay = { playback.play(Sound.OfText(block.ar)) }, size = 34)
            RichText(block.fr, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            RtlFlow {
                block.parts.forEachIndexed { i, part ->
                    if (i > 0) Text("+", style = MaterialTheme.typography.titleLarge, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 18.dp))
                    Column(
                        Modifier.background(tints[i % tints.size], RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ArabicText(part.ar, size = 28.sp, keepHarakat = true)
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            RichText(part.fr, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 140.dp))
                        }
                    }
                }
            }
        }
    }
}

/** Galerie de mots déjà vus : image, arabe et traduction ; toucher pour écouter. */
@Composable
private fun GalleryView(block: GalleryBlock, playback: Playback) {
    val items = block.items.mapNotNull { playback.course.item(it) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        block.title?.let { RichText(it, style = MaterialTheme.typography.titleSmall) }
        items.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { item ->
                    Column(
                        Modifier.weight(1f)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                            .clickable { playback.play(Sound.OfItem(item)) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ContentIcon(item.icon, size = 48.dp)
                        ArabicText(item.ar, size = 22.sp)
                        RichText(item.fr, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2)
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ExampleCard(block: ExampleBlock, playback: Playback) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SpeakerButton(onClick = { playback.play(Sound.OfText(block.ar)) }, size = 38.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                ArabicText(block.ar, size = 26.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth(), keepHarakat = true)
                RichText(block.fr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, keepHarakat = true)
            }
        }
    }
}

@Composable
private fun TipTable(block: TableBlock) {
    val line = MaterialTheme.colorScheme.outline
    Column(Modifier.fillMaxWidth().border(1.dp, line, RoundedCornerShape(12.dp))) {
        TableRow(block.header, header = true)
        block.rows.forEach { TableRow(it, header = false) }
    }
}

@Composable
private fun TableRow(cells: List<String>, header: Boolean) {
    Row(
        Modifier.fillMaxWidth()
            .height(IntrinsicSize.Min)
            .then(if (header) Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)) else Modifier),
    ) {
        cells.forEach { cell ->
            Box(Modifier.weight(1f).fillMaxHeight().padding(horizontal = 8.dp, vertical = 6.dp), contentAlignment = Alignment.CenterStart) {
                RichText(
                    cell,
                    style = if (header) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
                    keepHarakat = true,
                )
            }
        }
    }
}

// ---------------------------------------------------------------- contexte commun

@Composable
fun ContextView(screen: Exercise, playback: Playback) {
    val ctx = screen.context ?: return
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        ctx.icon?.let { ContentIcon(it, size = 120.dp) }
        ctx.item?.let { id ->
            playback.course.item(id)?.let { item -> SpokenArabic(item.ar, onPlay = { playback.play(Sound.OfItem(item)) }, size = 30) }
        }
        ctx.dialogue?.let { id ->
            val dialogue = playback.course.activities.flatMap { it.screens }.firstOrNull { it.id == id } as? DialogueScreen
            if (dialogue != null) DialogueReminder(dialogue, playback)
        }
    }
}

@Composable
private fun DialogueReminder(dialogue: DialogueScreen, playback: Playback) {
    var showText by remember(dialogue.id) { mutableStateOf(false) }
    var playing by remember(dialogue.id) { mutableStateOf<Int?>(null) }
    DisposableEffect(dialogue.id) { onDispose { playback.stop() } }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedButton(onClick = { playLines(dialogue, playback, 0) { playing = it } }) {
            RichText(stringResource(R.string.dialogue_replay, dialogue.title))
        }
        TextButton(onClick = { showText = !showText }) {
            Text(stringResource(if (showText) R.string.dialogue_hide_text else R.string.dialogue_show_text))
        }
        if (showText) DialogueLines(dialogue, playback, playing, showTranslation = false) { playing = it }
    }
}

/** Joue les lignes à partir de [from], l'une après l'autre ; [onLine] reçoit la ligne en cours (null à la fin). */
private fun playLines(dialogue: DialogueScreen, playback: Playback, from: Int, onLine: (Int?) -> Unit) {
    val line = dialogue.lines.getOrNull(from)
    if (line == null) {
        onLine(null)
        return
    }
    onLine(from)
    playback.play(Sound.OfText(line.ar, dialogue.speakers[line.speaker]?.voice)) {
        playLines(dialogue, playback, from + 1, onLine)
    }
}

// ---------------------------------------------------------------- dialogue

@Composable
fun DialogueContent(screen: DialogueScreen, playback: Playback) {
    var playing by remember(screen.id) { mutableStateOf<Int?>(null) }
    var translation by remember(screen.id) { mutableStateOf(true) }
    DisposableEffect(screen.id) { onDispose { playback.stop() } }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RichText(screen.title, style = MaterialTheme.typography.titleLarge)
        screen.intro?.let { RichText(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = {
                if (playing != null) {
                    playback.stop()
                    playing = null
                } else {
                    playLines(screen, playback, 0) { playing = it }
                }
            }) {
                Text(stringResource(if (playing != null) R.string.dialogue_stop else R.string.dialogue_play_all))
            }
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.dialogue_translation), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Switch(checked = translation, onCheckedChange = { translation = it })
        }
        DialogueLines(screen, playback, playing, translation) { playing = it }
    }
}

@Composable
private fun DialogueLines(
    screen: DialogueScreen,
    playback: Playback,
    playing: Int?,
    showTranslation: Boolean,
    onPlaying: (Int?) -> Unit,
) {
    val speakerKeys = screen.speakers.keys.toList()
    val palette = listOf(MaterialTheme.colorScheme.primary, AppTheme.colors.dialogue, AppTheme.colors.grammar, AppTheme.colors.review)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        screen.lines.forEachIndexed { i, line ->
            val color = palette[speakerKeys.indexOf(line.speaker).coerceAtLeast(0) % palette.size]
            val highlighted = playing == i
            Column(
                Modifier.fillMaxWidth()
                    .background(
                        if (highlighted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(14.dp),
                    )
                    .clickable {
                        onPlaying(i)
                        playback.play(Sound.OfText(line.ar, screen.speakers[line.speaker]?.voice)) { onPlaying(null) }
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(screen.speakers[line.speaker]?.name ?: line.speaker, color = color, style = MaterialTheme.typography.labelLarge)
                ArabicText(line.ar, size = 26.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                if (showTranslation) RichText(line.fr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ---------------------------------------------------------------- QCM

@Composable
fun McqContent(p: PreparedMcq, vm: PlayerViewModel, playback: Playback) {
    val mode = p.screen.mode
    val promptSound = if (mode == McqMode.AR_FR || mode == McqMode.AUDIO_AR) Sound.OfItem(p.target) else null
    AutoPlay(vm.turn, playback, promptSound)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Instruction(
            p.screen.instruction ?: stringResource(
                when (mode) {
                    McqMode.AR_FR -> R.string.instruction_mcq_ar_fr
                    McqMode.FR_AR -> R.string.instruction_mcq_fr_ar
                    McqMode.AUDIO_AR -> R.string.instruction_mcq_audio_ar
                    McqMode.ICON_AR -> R.string.instruction_mcq_icon_ar
                },
            ),
        )
        ContextView(p.screen, playback)
        when (mode) {
            McqMode.AR_FR -> SpokenArabic(p.target.ar, onPlay = { playback.play(Sound.OfItem(p.target)) }, size = 38)
            McqMode.FR_AR -> RichText(p.target.fr, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            McqMode.AUDIO_AR -> BigPlayButton(onClick = { playback.play(Sound.OfItem(p.target)) })
            McqMode.ICON_AR -> ContentIcon(p.target.icon, size = 150.dp)
        }
        Spacer(Modifier.height(4.dp))
        p.choices.forEachIndexed { i, item ->
            ChoiceCard(state = choiceState(i, vm.choice, p.correctIndex, vm.feedback != null), onClick = { vm.choice = i }) {
                if (mode == McqMode.AR_FR) RichText(item.fr, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                else ArabicText(item.ar, size = 28.sp)
            }
        }
    }
}

private fun choiceState(index: Int, selected: Int?, correct: Int, checked: Boolean): ChoiceState = when {
    !checked -> if (selected == index) ChoiceState.SELECTED else ChoiceState.IDLE
    index == correct -> ChoiceState.CORRECT
    index == selected -> ChoiceState.WRONG
    else -> ChoiceState.DISABLED
}

// ---------------------------------------------------------------- question

@Composable
fun QuestionContent(p: PreparedQuestion, vm: PlayerViewModel, playback: Playback) {
    AutoPlay(vm.turn, playback, p.screen.prompt.ar?.let { Sound.OfText(it) })
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Instruction(p.screen.instruction ?: stringResource(R.string.instruction_question))
        ContextView(p.screen, playback)
        TxtView(p.screen.prompt, playback, prompt = true)
        Spacer(Modifier.height(4.dp))
        p.order.forEachIndexed { position, choiceIndex ->
            ChoiceCard(
                state = choiceState(position, vm.choice, p.correctPosition, vm.feedback != null),
                onClick = { vm.choice = position },
            ) { TxtView(p.screen.choices[choiceIndex], playback, prompt = false) }
        }
    }
}

// ---------------------------------------------------------------- réponses multiples

/** « Coche toutes les bonnes réponses » : on touche chaque réponse à cocher, puis « Vérifier ». */
@Composable
fun MultiContent(p: PreparedMulti, vm: PlayerViewModel, playback: Playback) {
    AutoPlay(vm.turn, playback, p.screen.prompt.ar?.let { Sound.OfText(it) })
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Instruction(p.screen.instruction ?: stringResource(R.string.instruction_multi))
        ContextView(p.screen, playback)
        TxtView(p.screen.prompt, playback, prompt = true)
        Spacer(Modifier.height(4.dp))
        p.order.forEachIndexed { position, choiceIndex ->
            val checked = vm.feedback != null
            val selected = position in vm.multiSelected
            val state = when {
                !checked -> if (selected) ChoiceState.SELECTED else ChoiceState.IDLE
                position in p.correctPositions -> ChoiceState.CORRECT
                selected -> ChoiceState.WRONG
                else -> ChoiceState.DISABLED
            }
            ChoiceCard(state = state, onClick = { vm.toggleMulti(position) }) {
                TxtView(p.screen.choices[choiceIndex], playback, prompt = false)
            }
        }
    }
}

@Composable
private fun TxtView(t: Txt, playback: Playback, prompt: Boolean) {
    when {
        t.ar != null && prompt -> SpokenArabic(t.ar!!, onPlay = { playback.play(Sound.OfText(t.ar!!)) }, size = 32)
        t.ar != null -> ArabicText(t.ar!!, size = 26.sp)
        prompt -> RichText(t.fr.orEmpty(), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        else -> RichText(t.fr.orEmpty(), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

// ---------------------------------------------------------------- vrai / faux

@Composable
fun TrueFalseContent(p: PreparedTrueFalse, vm: PlayerViewModel, playback: Playback) {
    val st = p.screen.statement
    AutoPlay(vm.turn, playback, st.ar?.let { Sound.OfText(it) })
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Instruction(p.screen.instruction ?: stringResource(R.string.instruction_true_false))
        ContextView(p.screen, playback)
        if (st.ar != null) SpokenArabic(st.ar!!, onPlay = { playback.play(Sound.OfText(st.ar!!)) }, size = 32)
        else RichText(st.fr, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (value in listOf(true, false)) {
                val state = when {
                    vm.feedback == null -> if (vm.trueFalseChoice == value) ChoiceState.SELECTED else ChoiceState.IDLE
                    value == p.screen.answer -> ChoiceState.CORRECT
                    value == vm.trueFalseChoice -> ChoiceState.WRONG
                    else -> ChoiceState.DISABLED
                }
                ChoiceCard(state, onClick = { vm.trueFalseChoice = value }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(if (value) R.string.true_ else R.string.false_), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- saisie

@Composable
fun TypingContent(p: PreparedTyping, vm: PlayerViewModel, playback: Playback) {
    val screen = p.screen
    val item = screen.item?.let { playback.course.item(it) }
    val promptSound = when (screen.ask) {
        TypingAsk.AUDIO -> item?.let { Sound.OfItem(it) } ?: Sound.OfText(p.answers.first())
        TypingAsk.QUESTION -> screen.question?.let { Sound.OfText(it) }
        TypingAsk.FR -> null
    }
    AutoPlay(vm.turn, playback, promptSound)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Instruction(
            screen.instruction ?: stringResource(
                when (screen.ask) {
                    TypingAsk.FR -> R.string.instruction_typing_fr
                    TypingAsk.AUDIO -> R.string.instruction_typing_audio
                    TypingAsk.QUESTION -> R.string.instruction_typing_question
                },
            ),
        )
        ContextView(screen, playback)
        when (screen.ask) {
            TypingAsk.FR -> RichText(p.translation, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            TypingAsk.AUDIO -> BigPlayButton(onClick = { promptSound?.let { playback.play(it) } })
            TypingAsk.QUESTION -> screen.question?.let { q -> SpokenArabic(q, onPlay = { playback.play(Sound.OfText(q)) }, size = 32) }
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            OutlinedTextField(
                value = vm.typed,
                onValueChange = { vm.typed = it },
                enabled = vm.feedback == null,
                modifier = Modifier.fillMaxWidth(),
                textStyle = arabicStyle(28.sp).copy(color = MaterialTheme.colorScheme.onSurface),
                placeholder = {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(stringResource(R.string.typing_placeholder))
                    }
                },
                minLines = 1,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { vm.check() }),
            )
        }
        Text(
            stringResource(R.string.typing_keyboard_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---------------------------------------------------------------- texte à trous

@Composable
fun FillGapContent(p: PreparedFillGap, vm: PlayerViewModel, playback: Playback) {
    val checked = vm.feedback != null
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Instruction(p.screen.instruction ?: stringResource(R.string.instruction_fill_gap))
        ContextView(p.screen, playback)
        RtlFlow {
            for (part in p.parts) when (part) {
                is PreparedFillGap.SentencePart.Text ->
                    part.text.split(' ').filter { it.isNotEmpty() }.forEach { word -> ArabicText(word, size = 28.sp) }
                is PreparedFillGap.SentencePart.Gap -> {
                    val value = vm.gapFill.getOrNull(part.index)
                    val state = when {
                        !checked -> ChoiceState.IDLE
                        value != null && value in p.screen.gaps[part.index].answers -> ChoiceState.CORRECT
                        else -> ChoiceState.WRONG
                    }
                    TokenChip(
                        onClick = if (!checked && value != null) ({ vm.gapFill[part.index] = null }) else null,
                        state = state,
                        modifier = Modifier.defaultMinSize(minWidth = 72.dp),
                    ) { ArabicText(value ?: "", size = 26.sp) }
                }
            }
        }
        RichText(p.screen.fr, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        RtlFlow {
            for (option in p.options) {
                val used = option in vm.gapFill
                TokenChip(
                    onClick = {
                        val empty = vm.gapFill.indexOfFirst { it == null }
                        if (!checked && !used && empty >= 0) vm.gapFill[empty] = option
                    },
                    state = if (used) ChoiceState.DISABLED else ChoiceState.IDLE,
                    invisible = used,
                ) { ArabicText(option, size = 26.sp) }
            }
        }
    }
}

@Composable
private fun RtlFlow(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { content() }
    }
}

// ---------------------------------------------------------------- remise en ordre

@Composable
fun OrderContent(p: PreparedOrder, vm: PlayerViewModel, playback: Playback) {
    val checked = vm.feedback != null
    val correct = vm.feedback?.verdict == AnswerMatcher.Verdict.CORRECT
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Instruction(p.screen.instruction ?: stringResource(R.string.instruction_order))
        ContextView(p.screen, playback)
        RichText(p.screen.fr, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Box(
            Modifier.fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            RtlFlow {
                vm.orderPicked.forEachIndexed { position, tokenIndex ->
                    TokenChip(
                        onClick = if (!checked) ({ vm.orderPicked.removeAt(position); Unit }) else null,
                        state = if (!checked) ChoiceState.SELECTED else if (correct) ChoiceState.CORRECT else ChoiceState.WRONG,
                    ) { ArabicText(p.tokens[tokenIndex], size = 26.sp) }
                }
                if (vm.orderPicked.isNotEmpty() && p.screen.punctuation != null) ArabicText(p.screen.punctuation!!, size = 26.sp)
            }
        }
        RtlFlow {
            p.tokens.forEachIndexed { i, token ->
                val used = i in vm.orderPicked
                TokenChip(
                    onClick = { if (!checked && !used) vm.orderPicked.add(i) },
                    invisible = used,
                ) { ArabicText(token, size = 26.sp) }
            }
        }
    }
}

// ---------------------------------------------------------------- association

@Composable
fun MatchContent(p: PreparedMatch, vm: PlayerViewModel, playback: Playback) {
    val mode = p.screen.mode
    val sounds = LocalAppContainer.current.sounds
    val haptics = rememberHaptics()
    LaunchedEffect(vm.matchWrong) {
        if (vm.matchWrong != null) {
            haptics.error()
            delay(700)
            vm.matchWrong = null
        }
    }
    // Petit son à chaque paire trouvée (la dernière est saluée par le panneau de correction).
    LaunchedEffect(vm.matchDone.size) {
        if (vm.matchDone.isNotEmpty() && vm.matchDone.size < p.left.size) {
            sounds.play(SoundEffects.Effect.PAIR)
            haptics.tick()
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Instruction(p.screen.instruction ?: stringResource(R.string.instruction_match))
        ContextView(p.screen, playback)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                p.left.forEachIndexed { i, item ->
                    MatchTile(
                        state = matchState(item, vm.matchDone, vm.matchLeft == i, vm.matchWrong?.first == i),
                        onClick = {
                            if (mode != MatchMode.SING_PLUR) playback.play(Sound.OfItem(item))
                            vm.tapMatch(left = i, right = null)
                        },
                    ) {
                        when (mode) {
                            MatchMode.AUDIO_AR -> Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(SpeakerIcon, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("${i + 1}", style = MaterialTheme.typography.titleMedium)
                            }
                            else -> ArabicText(item.ar, size = 24.sp)
                        }
                    }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                p.right.forEachIndexed { i, item ->
                    MatchTile(
                        state = matchState(item, vm.matchDone, vm.matchRight == i, vm.matchWrong?.second == i),
                        onClick = {
                            if (mode == MatchMode.SING_PLUR) playback.play(Sound.OfText(item.plural!!, item.voice))
                            vm.tapMatch(left = null, right = i)
                        },
                    ) {
                        when (mode) {
                            MatchMode.AR_FR -> RichText(item.fr, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                            MatchMode.AUDIO_AR -> ArabicText(item.ar, size = 24.sp)
                            MatchMode.SING_PLUR -> ArabicText(item.plural.orEmpty(), size = 24.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun matchState(item: Item, done: List<String>, selected: Boolean, wrong: Boolean): ChoiceState = when {
    item.id in done -> ChoiceState.CORRECT
    wrong -> ChoiceState.WRONG
    selected -> ChoiceState.SELECTED
    else -> ChoiceState.IDLE
}

@Composable
private fun MatchTile(state: ChoiceState, onClick: () -> Unit, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    val scheme = MaterialTheme.colorScheme
    val (border, background) = when (state) {
        ChoiceState.CORRECT -> colors.correct to colors.correctContainer
        ChoiceState.WRONG -> colors.wrong to colors.wrongContainer
        ChoiceState.SELECTED -> scheme.secondary to scheme.secondaryContainer
        else -> scheme.outline to scheme.surface
    }
    Box(
        Modifier.answerAnimation(state)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .background(background, RoundedCornerShape(14.dp))
            .border(if (state == ChoiceState.IDLE) 1.5.dp else 2.5.dp, border, RoundedCornerShape(14.dp))
            .then(if (state != ChoiceState.CORRECT) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** Libellé d'une remarque de saisie. */
fun AnswerMatcher.Remark.label(): Int = when (this) {
    AnswerMatcher.Remark.HAMZA_ALIF -> R.string.remark_hamza_alif
    AnswerMatcher.Remark.TA_MARBUTA -> R.string.remark_ta_marbuta
    AnswerMatcher.Remark.ALIF_MAQSURA -> R.string.remark_alif_maqsura
    AnswerMatcher.Remark.HARAKAT -> R.string.remark_harakat
}
