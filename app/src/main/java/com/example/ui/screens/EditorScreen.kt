package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CachedSurahEntity
import com.example.data.model.QiraatCatalog
import com.example.data.model.QiraatDiffEntry
import com.example.ui.EditorUiState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.LocalQiraatColors

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    uiState: EditorUiState,
    surahs: List<CachedSurahEntity>,
    savedDiffs: List<QiraatDiffEntry>,
    onSelectSurah: (Int) -> Unit,
    onSelectAyah: (Int) -> Unit,
    onSelectWord: (String) -> Unit,
    onUpdateFields: (
        wordTitle: String?,
        matchTokens: String?,
        reading: String?,
        ruleType: String?,
        readers: String?,
        note: String?
    ) -> Unit,
    onToggleReaderChip: (String) -> Unit,
    onAddPendingVariant: () -> Unit,
    onRemovePendingVariant: (Int) -> Unit,
    onSaveEntry: () -> Unit,
    onDeleteSavedEntry: (Long) -> Unit,
    onBuildExportJson: () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val qiraatColors = LocalQiraatColors.current
    var showSurahPicker by remember { mutableStateOf(false) }
    var showAyahPicker by remember { mutableStateOf(false) }

    val selectedSurahName = remember(surahs, uiState.surahId) {
        surahs.find { it.id == uiState.surahId }?.nameArabic ?: "سورہ ${uiState.surahId}"
    }
    val selectedAyahObj = remember(uiState.ayahsForSurah, uiState.ayahNumber) {
        uiState.ayahsForSurah.find { it.ayahNumber == uiState.ayahNumber }
    }
    val ayahWords = remember(selectedAyahObj) {
        selectedAyahObj?.text?.trim()?.split(Regex("\\s+"))?.filter { it.isNotEmpty() }.orEmpty()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, qiraatColors.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. سورہ اور آیت منتخب کریں",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            OutlinedButton(
                                onClick = {
                                    val json = onBuildExportJson()
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_SUBJECT, "differences.json — QiraatAsharah")
                                        putExtra(Intent.EXTRA_TEXT, json)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "differences.json ایکسپورٹ کریں")
                                    )
                                },
                                modifier = Modifier.testTag("export_json_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("JSON ایکسپورٹ")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("editor_surah_picker")
                                    .clickable { showSurahPicker = true },
                                border = BorderStroke(1.dp, qiraatColors.cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "سورہ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${uiState.surahId}. $selectedSurahName",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "سورہ منتخب کریں")
                                }
                            }

                            OutlinedCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("editor_ayah_picker")
                                    .clickable { showAyahPicker = true },
                                border = BorderStroke(1.dp, qiraatColors.cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "آیت (حفص)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "آیت ${uiState.ayahNumber}",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "آیت منتخب کریں")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "آیت کے کسی لفظ پر ٹیپ کریں:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (uiState.isLoadingAyahs) {
                            CircularProgressIndicator(modifier = Modifier.padding(12.dp))
                        } else if (ayahWords.isEmpty()) {
                            Text(
                                text = "اس سورہ کے الفاظ لوڈ کرنے کے لیے انٹرنیٹ درکار ہے",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ayahWords.forEachIndexed { idx, word ->
                                    val selected = uiState.selectedWord == word
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onSelectWord(word) },
                                        label = {
                                            Text(
                                                text = word,
                                                fontFamily = AmiriFontFamily,
                                                fontSize = 20.sp
                                            )
                                        },
                                        modifier = Modifier.testTag("editor_word_chip_$idx")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, qiraatColors.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. قراءاتی اختلاف شامل کریں",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uiState.wordTitle,
                            onValueChange = { onUpdateFields(it, null, null, null, null, null) },
                            label = { Text("لفظ کی سرخی (Title)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("editor_word_title_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.matchTokensText,
                            onValueChange = { onUpdateFields(null, it, null, null, null, null) },
                            label = { Text("میچنگ الفاظ (بغیر اعراب، کوما سے الگ)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("editor_match_tokens_input"),
                            singleLine = true
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        OutlinedTextField(
                            value = uiState.readingInput,
                            onValueChange = { onUpdateFields(null, null, it, null, null, null) },
                            label = { Text("قراءت کی شکل (Reading)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("editor_reading_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "قاعدہ کی قسم:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            QiraatCatalog.ruleTypes.forEach { rule ->
                                FilterChip(
                                    selected = uiState.ruleType == rule,
                                    onClick = { onUpdateFields(null, null, null, rule, null, null) },
                                    label = { Text(rule) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "قراء / رواة منتخب کریں:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            QiraatCatalog.allQurraAndRuwatChips.forEach { name ->
                                FilterChip(
                                    selected = uiState.selectedReaders.contains(name),
                                    onClick = { onToggleReaderChip(name) },
                                    label = { Text(name) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.customReadersText,
                            onValueChange = { onUpdateFields(null, null, null, null, it, null) },
                            label = { Text("منتخب قراء") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("editor_readers_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.noteInput,
                            onValueChange = { onUpdateFields(null, null, null, null, null, it) },
                            label = { Text("علمی وضاحت / نوٹ") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("editor_note_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onAddPendingVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_variant_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("لسٹ میں شامل")
                            }

                            Button(
                                onClick = onSaveEntry,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_diff_entry_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("محفوظ کریں")
                            }
                        }

                        if (uiState.pendingItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "شامل کردہ قراءات (${uiState.pendingItems.size}):",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            uiState.pendingItems.forEachIndexed { idx, item ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.background,
                                    border = BorderStroke(1.dp, qiraatColors.cardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${item.reading} (${item.rule})",
                                                fontFamily = AmiriFontFamily,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = item.readers,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        IconButton(onClick = { onRemovePendingVariant(idx) }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "ہٹائیں")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "3. محفوظ شدہ اختلافات (${savedDiffs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(savedDiffs, key = { it.id }) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, qiraatColors.cardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "سورہ ${entry.surahId} : آیت ${entry.ayahHafs} — ${entry.wordTitle}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "میچ: [${entry.matchTokens.joinToString(", ")}] • ${entry.items.size} قراءات",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onDeleteSavedEntry(entry.id) },
                            modifier = Modifier.testTag("delete_saved_diff_${entry.id}")
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف کریں")
                        }
                    }
                }
            }
        }
    }

    if (showSurahPicker) {
        ModalBottomSheet(onDismissRequest = { showSurahPicker = false }) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(surahs, key = { it.id }) { surah ->
                    ListItem(
                        headlineContent = { Text("${surah.id}. ${surah.nameArabic}") },
                        supportingContent = { Text("${surah.totalVerses} آیات") },
                        modifier = Modifier.clickable {
                            showSurahPicker = false
                            onSelectSurah(surah.id)
                        }
                    )
                }
            }
        }
    }

    if (showAyahPicker) {
        ModalBottomSheet(onDismissRequest = { showAyahPicker = false }) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(uiState.ayahsForSurah, key = { it.ayahNumber }) { ayah ->
                    ListItem(
                        headlineContent = { Text("آیت ${ayah.ayahNumber}") },
                        supportingContent = {
                            Text(
                                text = ayah.text,
                                fontFamily = AmiriFontFamily,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.clickable {
                            showAyahPicker = false
                            onSelectAyah(ayah.ayahNumber)
                        }
                    )
                }
            }
        }
    }
}
