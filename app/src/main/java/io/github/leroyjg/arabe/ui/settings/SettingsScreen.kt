@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.leroyjg.arabe.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.leroyjg.arabe.BuildConfig
import io.github.leroyjg.arabe.R
import io.github.leroyjg.arabe.data.BackupFile
import io.github.leroyjg.arabe.data.Settings
import io.github.leroyjg.arabe.data.ThemeMode
import io.github.leroyjg.arabe.ui.LocalAppContainer
import io.github.leroyjg.arabe.ui.text.ArabicText
import io.github.leroyjg.arabe.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Onglet « Paramètres ». Chaque réglage s'applique tout de suite. */
@Composable
fun SettingsScreen() {
    val container = LocalAppContainer.current
    val repo = container.settings
    val settings by repo.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var pendingImport by remember { mutableStateOf<BackupFile?>(null) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    val msgExported = stringResource(R.string.settings_exported)
    val msgExportFailed = stringResource(R.string.settings_export_failed)
    val msgImported = stringResource(R.string.settings_imported)
    val msgImportInvalid = stringResource(R.string.settings_import_invalid)
    val msgReset = stringResource(R.string.settings_reset_done)

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { container.backup.export(it) } ?: error("flux indisponible")
                    }.isSuccess
                }
                snackbar.showSnackbar(if (ok) msgExported else msgExportFailed)
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val file = withContext(Dispatchers.IO) {
                    runCatching { context.contentResolver.openInputStream(uri)?.use { container.backup.read(it) } }.getOrNull()
                }
                if (file == null) snackbar.showSnackbar(msgImportInvalid) else pendingImport = file
            }
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    stringResource(R.string.tab_settings),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                )

                Section(stringResource(R.string.settings_section_arabic)) {
                    Box(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ArabicText("هٰذَا كِتَابٌ.", size = 28.sp)
                            Text(
                                stringResource(R.string.settings_preview_translation),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }
                    }
                    SwitchRow(
                        stringResource(R.string.settings_harakat),
                        stringResource(R.string.settings_harakat_hint),
                        settings.showHarakat,
                    ) { v -> repo.update { it.copy(showHarakat = v) } }
                    SwitchRow(
                        stringResource(R.string.settings_translit),
                        stringResource(R.string.settings_translit_hint),
                        settings.showTranslit,
                    ) { v -> repo.update { it.copy(showTranslit = v) } }
                    ChoiceRow(
                        title = stringResource(R.string.settings_arabic_size),
                        options = Settings.ARABIC_SCALES,
                        labels = listOf(
                            stringResource(R.string.settings_size_small),
                            stringResource(R.string.settings_size_normal),
                            stringResource(R.string.settings_size_large),
                            stringResource(R.string.settings_size_xlarge),
                        ),
                        selected = settings.arabicScale,
                    ) { v -> repo.update { it.copy(arabicScale = v) } }
                }

                Section(stringResource(R.string.settings_section_audio)) {
                    SwitchRow(
                        stringResource(R.string.settings_autoplay),
                        stringResource(R.string.settings_autoplay_hint),
                        settings.autoPlay,
                    ) { v -> repo.update { it.copy(autoPlay = v) } }
                    ChoiceRow(
                        title = stringResource(R.string.settings_speech_rate),
                        options = Settings.SPEECH_RATES,
                        labels = listOf(
                            stringResource(R.string.settings_rate_slow),
                            stringResource(R.string.settings_rate_normal),
                            stringResource(R.string.settings_rate_fast),
                        ),
                        selected = settings.speechRate,
                    ) { v -> repo.update { it.copy(speechRate = v) } }
                    SwitchRow(
                        stringResource(R.string.settings_sounds),
                        stringResource(R.string.settings_sounds_hint),
                        settings.soundEffects,
                    ) { v -> repo.update { it.copy(soundEffects = v) } }
                    SwitchRow(
                        stringResource(R.string.settings_haptics),
                        stringResource(R.string.settings_haptics_hint),
                        settings.haptics,
                    ) { v -> repo.update { it.copy(haptics = v) } }
                }

                Section(stringResource(R.string.settings_section_review)) {
                    SwitchRow(
                        stringResource(R.string.settings_typing),
                        stringResource(R.string.settings_typing_hint),
                        settings.typingInReview,
                    ) { v -> repo.update { it.copy(typingInReview = v) } }
                    ChoiceRow(
                        title = stringResource(R.string.settings_review_size),
                        options = Settings.REVIEW_SIZES,
                        labels = Settings.REVIEW_SIZES.map { stringResource(R.string.settings_review_size_value, it) },
                        selected = settings.reviewSize,
                    ) { v -> repo.update { it.copy(reviewSize = v) } }
                }

                Section(stringResource(R.string.settings_section_appearance)) {
                    ChoiceRow(
                        title = stringResource(R.string.settings_theme),
                        options = ThemeMode.entries.toList(),
                        labels = listOf(
                            stringResource(R.string.settings_theme_system),
                            stringResource(R.string.settings_theme_light),
                            stringResource(R.string.settings_theme_dark),
                        ),
                        selected = settings.theme,
                    ) { v -> repo.update { it.copy(theme = v) } }
                }

                Section(stringResource(R.string.settings_section_data)) {
                    ActionRow(stringResource(R.string.settings_export), stringResource(R.string.settings_export_hint)) {
                        exportLauncher.launch("loura-progression-${LocalDate.now()}.json")
                    }
                    ActionRow(stringResource(R.string.settings_import), stringResource(R.string.settings_import_hint)) {
                        importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                    }
                    ActionRow(
                        stringResource(R.string.settings_reset),
                        stringResource(R.string.settings_reset_hint),
                        color = AppTheme.colors.wrong,
                    ) { confirmReset = true }
                }

                Section(stringResource(R.string.settings_section_about)) {
                    InfoRow(stringResource(R.string.app_name), stringResource(R.string.settings_version, BuildConfig.VERSION_NAME))
                    InfoRow(stringResource(R.string.settings_about_content), stringResource(R.string.settings_about_content_text))
                    InfoRow(stringResource(R.string.settings_about_offline), stringResource(R.string.settings_about_offline_text))
                    InfoRow(stringResource(R.string.settings_about_credits), stringResource(R.string.settings_about_credits_text))
                }
                Spacer(Modifier.height(8.dp))
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
        }
    }

    pendingImport?.let { file ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text(stringResource(R.string.settings_import_confirm_title)) },
            text = { Text(stringResource(R.string.settings_import_confirm_text, file.exportedAt.take(10), file.results.size, file.cards.size)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingImport = null
                    scope.launch {
                        withContext(Dispatchers.IO) { container.backup.restore(file) }
                        snackbar.showSnackbar(msgImported)
                    }
                }) { Text(stringResource(R.string.settings_import_confirm)) }
            },
            dismissButton = { TextButton(onClick = { pendingImport = null }) { Text(stringResource(R.string.quit_cancel_generic)) } },
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.settings_reset_confirm_title)) },
            text = { Text(stringResource(R.string.settings_reset_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    scope.launch {
                        withContext(Dispatchers.IO) { container.backup.reset() }
                        snackbar.showSnackbar(msgReset)
                    }
                }) { Text(stringResource(R.string.settings_reset_confirm), color = AppTheme.colors.wrong) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.quit_cancel_generic)) } },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp),
        )
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(vertical = 6.dp)) { content() }
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun <T> ChoiceRow(title: String, options: List<T>, labels: List<String>, selected: T, onSelect: (T) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            options.forEachIndexed { i, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    shape = SegmentedButtonDefaults.itemShape(index = i, count = options.size),
                    icon = {},
                    label = { Text(labels[i], maxLines = 1, textAlign = TextAlign.Center) },
                )
            }
        }
    }
}

@Composable
private fun ActionRow(title: String, subtitle: String, color: Color = Color.Unspecified, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun InfoRow(title: String, text: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
