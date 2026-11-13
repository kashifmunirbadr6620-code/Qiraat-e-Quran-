package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CachedAyahEntity
import com.example.data.model.ArabicTextUtils
import com.example.data.model.QiraatCatalog
import com.example.data.model.QiraatDiffEntry
import com.example.ui.ReaderUiState
import com.example.ui.components.QiraatDiffSheet
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.LocalQiraatColors

@Composable
fun ReaderScreen(
    uiState: ReaderUiState,
    onSelectRiwayah: (String) -> Unit,
    onToggleCompare: () -> Unit,
    onBookmarkAyah: (CachedAyahEntity) -> Unit,
    onNavigateSurah: (Int) -> Unit,
    onRetry: () -> Unit,
    onEditDiffInEditor: (QiraatDiffEntry) -> Unit,
    findWordDiff: (List<Int>, String, List<QiraatDiffEntry>) -> QiraatDiffEntry?,
    modifier: Modifier = Modifier
) {
    val qiraatColors = LocalQiraatColors.current
    val listState = rememberLazyListState()
    var activeDiffModal by remember { mutableStateOf<Pair<String, QiraatDiffEntry>?>(null) }

    val activeRiwayahInfo = remember(uiState.selectedRiwayah) {
        QiraatCatalog.liveRiwayat.find { it.code == uiState.selectedRiwayah }
            ?: QiraatCatalog.liveRiwayat.first()
    }

    LaunchedEffect(uiState.highlightAyahNumber, uiState.ayahs) {
        val target = uiState.highlightAyahNumber
        if (target != null && uiState.ayahs.isNotEmpty()) {
            val idx = uiState.ayahs.indexOfFirst { it.ayahNumber == target }
            if (idx >= 0) {
                listState.animateScrollToItem(idx + 1)
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            // Riwayah selector chips
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(QiraatCatalog.liveRiwayat, key = { it.code }) { riwayah ->
                            val selected = riwayah.code == uiState.selectedRiwayah
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectRiwayah(riwayah.code) },
                                label = { Text(riwayah.label) },
                                modifier = Modifier.testTag("riwayah_chip_${riwayah.code}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${activeRiwayahInfo.label} • ${activeRiwayahInfo.description}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.selectedRiwayah != "uthmani") {
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = uiState.compareMode,
                                onClick = onToggleCompare,
                                label = { Text("موازنہ (حفص)") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CompareArrows,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("compare_mode_chip")
                            )
                        }
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("آیات لوڈ ہو رہی ہیں…")
                        }
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.errorMessage,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(onClick = onRetry) {
                                Text("دوبارہ کوشش کریں")
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            SurahHeaderCard(
                                surahId = uiState.surahId,
                                surahName = uiState.surahName,
                                revelationType = uiState.revelationType,
                                totalVerses = uiState.totalVerses,
                                showBismillah = uiState.surahId != 1 && uiState.surahId != 9,
                                borderColor = qiraatColors.cardBorder
                            )
                        }

                        items(
                            items = uiState.ayahs,
                            key = { "${it.riwayahCode}_${it.surahId}_${it.ayahNumber}" }
                        ) { ayah ->
                            val isHighlighted = uiState.highlightAyahNumber == ayah.ayahNumber
                            val isBookmarked = uiState.bookmarkedAyahs.contains(ayah.ayahNumber)
                            val hafsRefs = ayah.hafsRefs()
                            val matchingHafsText = if (uiState.compareMode && uiState.selectedRiwayah != "uthmani") {
                                hafsRefs.mapNotNull { ref -> uiState.hafsAyahsByNumber[ref] }
                                    .joinToString(" ") { hv ->
                                        "${hv.text} ﴿${ArabicTextUtils.toArabicDigits(hv.ayahNumber)}﴾"
                                    }
                            } else {
                                ""
                            }

                            AyahCard(
                                ayah = ayah,
                                diffs = uiState.diffs,
                                fontSizeSp = uiState.fontSizeSp,
                                isHighlighted = isHighlighted,
                                isBookmarked = isBookmarked,
                                matchingHafsText = matchingHafsText,
                                findWordDiff = findWordDiff,
                                onWordDiffClick = { word, entry ->
                                    activeDiffModal = word to entry
                                },
                                onBookmarkClick = { onBookmarkAyah(ayah) }
                            )
                        }

                        item {
                            SurahNavigationFooter(
                                surahId = uiState.surahId,
                                onNavigateSurah = onNavigateSurah
                            )
                        }
                    }
                }
            }
        }
    }

    activeDiffModal?.let { (word, entry) ->
        QiraatDiffSheet(
            tappedWord = word,
            entry = entry,
            onEditClick = { target ->
                activeDiffModal = null
                onEditDiffInEditor(target)
            },
            onDismiss = { activeDiffModal = null }
        )
    }
}

@Composable
private fun SurahHeaderCard(
    surahId: Int,
    surahName: String,
    revelationType: String,
    totalVerses: Int,
    showBismillah: Boolean,
    borderColor: androidx.compose.ui.graphics.Color
) {
    val revLabel = if (ArabicTextUtils.isMeccan(revelationType)) "مکیہ" else "مدنیہ"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "سورة $surahName",
                fontFamily = AmiriFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "سورہ $surahId • $revLabel • $totalVerses آیات • سرخ الفاظ پر ٹیپ کریں",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (showBismillah) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    fontFamily = AmiriFontFamily,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AyahCard(
    ayah: CachedAyahEntity,
    diffs: List<QiraatDiffEntry>,
    fontSizeSp: Float,
    isHighlighted: Boolean,
    isBookmarked: Boolean,
    matchingHafsText: String,
    findWordDiff: (List<Int>, String, List<QiraatDiffEntry>) -> QiraatDiffEntry?,
    onWordDiffClick: (String, QiraatDiffEntry) -> Unit,
    onBookmarkClick: () -> Unit
) {
    val qiraatColors = LocalQiraatColors.current
    val hafsRefs = remember(ayah.numberInHafsCsv, ayah.ayahNumber) { ayah.hafsRefs() }
    val words = remember(ayah.text) { ayah.text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() } }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ayah_card_${ayah.ayahNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isHighlighted) 1.5.dp else 1.dp,
            color = if (isHighlighted) qiraatColors.goldAccent else qiraatColors.cardBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                words.forEachIndexed { index, word ->
                    val diffEntry = findWordDiff(hafsRefs, word, diffs)
                    if (diffEntry != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(qiraatColors.diffHighlightBg)
                                .clickable { onWordDiffClick(word, diffEntry) }
                                .testTag("diff_word_${ayah.ayahNumber}_$index")
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = word,
                                fontFamily = AmiriFontFamily,
                                fontSize = fontSizeSp.sp,
                                lineHeight = (fontSizeSp * 1.65f).sp,
                                fontWeight = FontWeight.Bold,
                                color = qiraatColors.diffHighlightText
                            )
                        }
                    } else {
                        Text(
                            text = word,
                            fontFamily = AmiriFontFamily,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.65f).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "﴿${ArabicTextUtils.toArabicDigits(ayah.ayahNumber)}﴾",
                    fontFamily = AmiriFontFamily,
                    fontSize = (fontSizeSp * 0.85f).sp,
                    lineHeight = (fontSizeSp * 1.65f).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)
                )
            }

            if (matchingHafsText.isNotBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = qiraatColors.cardBorder
                )
                Text(
                    text = "حفص: $matchingHafsText",
                    fontFamily = AmiriFontFamily,
                    fontSize = (fontSizeSp * 0.82f).sp,
                    lineHeight = (fontSizeSp * 1.45f).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val refLabel = if (hafsRefs != listOf(ayah.ayahNumber)) {
                    "آیت ${ayah.ayahNumber} (حفص: ${hafsRefs.joinToString("، ")})"
                } else {
                    "آیت ${ayah.ayahNumber}"
                }
                Text(
                    text = refLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier.testTag("bookmark_ayah_${ayah.ayahNumber}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isBookmarked) "بُک مارک ہٹائیں" else "بُک مارک محفوظ کریں",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SurahNavigationFooter(
    surahId: Int,
    onNavigateSurah: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { onNavigateSurah(surahId - 1) },
            enabled = surahId > 1,
            modifier = Modifier.testTag("prev_surah_button")
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("پچھلی سورہ")
        }

        OutlinedButton(
            onClick = { onNavigateSurah(surahId + 1) },
            enabled = surahId < 114,
            modifier = Modifier.testTag("next_surah_button")
        ) {
            Text("اگلی سورہ")
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}
