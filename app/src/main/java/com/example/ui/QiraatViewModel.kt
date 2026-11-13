package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AyahSearchResult
import com.example.data.local.BookmarkEntity
import com.example.data.local.CachedAyahEntity
import com.example.data.local.CachedSurahEntity
import com.example.data.model.ArabicTextUtils
import com.example.data.model.QiraatDiffEntry
import com.example.data.model.QiraatDiffItem
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserPrefs
import com.example.data.remote.QuranApiService
import com.example.data.repository.QuranRepository
import com.example.ui.theme.DarkThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReaderUiState(
    val surahId: Int = 1,
    val surahName: String = "الفاتحة",
    val revelationType: String = "meccan",
    val totalVerses: Int = 7,
    val selectedRiwayah: String = "uthmani",
    val compareMode: Boolean = false,
    val fontSizeSp: Float = 28f,
    val ayahs: List<CachedAyahEntity> = emptyList(),
    val hafsAyahsByNumber: Map<Int, CachedAyahEntity> = emptyMap(),
    val diffs: List<QiraatDiffEntry> = emptyList(),
    val bookmarkedAyahs: Set<Int> = emptySet(),
    val highlightAyahNumber: Int? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class SurahsUiState(
    val searchQuery: String = "",
    val surahs: List<CachedSurahEntity> = emptyList(),
    val cachedSurahIds: Set<Int> = emptySet(),
    val ayahSearchResults: List<AyahSearchResult> = emptyList(),
    val isRefreshingCatalog: Boolean = false,
    val lastSurahId: Int = 1,
    val lastSurahName: String = "الفاتحة"
)

data class EditorUiState(
    val surahId: Int = 1,
    val ayahNumber: Int = 1,
    val ayahsForSurah: List<CachedAyahEntity> = emptyList(),
    val selectedWord: String = "",
    val wordTitle: String = "",
    val matchTokensText: String = "",
    val readingInput: String = "",
    val ruleType: String = "فرش الحروف",
    val selectedReaders: Set<String> = emptySet(),
    val customReadersText: String = "",
    val noteInput: String = "",
    val pendingItems: List<QiraatDiffItem> = emptyList(),
    val isLoadingAyahs: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class QiraatViewModel(application: Application) : AndroidViewModel(application) {
    private val moshi = QuranApiService.createMoshi()
    private val api = QuranApiService.create(moshi)
    private val db = AppDatabase.getInstance(application)
    private val repository = QuranRepository(db, api, moshi)
    private val prefsRepo = UserPreferencesRepository(application)

    val userPrefs: StateFlow<UserPrefs> = prefsRepo.userPrefsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPrefs()
    )

    private val _searchQuery = MutableStateFlow("")
    private val _ayahSearchResults = MutableStateFlow<List<AyahSearchResult>>(emptyList())
    private val _isRefreshingCatalog = MutableStateFlow(false)

    private val _catalogMetaFlow = combine(
        repository.surahsFlow,
        repository.cachedHafsSurahIdsFlow,
        _isRefreshingCatalog
    ) { allSurahs, cachedIds, refreshing ->
        Triple(allSurahs, cachedIds, refreshing)
    }

    val surahsUiState: StateFlow<SurahsUiState> = combine(
        _searchQuery,
        _catalogMetaFlow,
        _ayahSearchResults,
        userPrefs
    ) { query, (allSurahs, cachedIds, refreshing), ayahMatches, prefs ->
        val trimmed = query.trim()
        val filteredSurahs = if (trimmed.isEmpty()) {
            allSurahs
        } else {
            allSurahs.filter { surah ->
                surah.nameArabic.contains(trimmed) ||
                    surah.transliteration.contains(trimmed, ignoreCase = true) ||
                    surah.id.toString() == trimmed
            }
        }

        SurahsUiState(
            searchQuery = query,
            surahs = filteredSurahs,
            cachedSurahIds = cachedIds,
            ayahSearchResults = ayahMatches,
            isRefreshingCatalog = refreshing,
            lastSurahId = prefs.lastSurahId,
            lastSurahName = prefs.lastSurahName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SurahsUiState()
    )

    private val _currentReaderSurahId = MutableStateFlow(1)
    private val _readerDataState = MutableStateFlow(ReaderUiState())

    private val _currentSurahDiffs = _currentReaderSurahId.flatMapLatest { id ->
        repository.observeDiffsForSurah(id)
    }
    private val _currentSurahBookmarks = _currentReaderSurahId.flatMapLatest { id ->
        repository.observeBookmarkedAyahs(id)
    }

    val readerUiState: StateFlow<ReaderUiState> = combine(
        _readerDataState,
        userPrefs,
        _currentSurahDiffs,
        _currentSurahBookmarks
    ) { data, prefs, diffs, bookmarked ->
        data.copy(
            selectedRiwayah = prefs.selectedRiwayah,
            compareMode = prefs.compareMode,
            fontSizeSp = prefs.fontSizeSp,
            diffs = diffs,
            bookmarkedAyahs = bookmarked
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReaderUiState()
    )

    val bookmarksFlow: StateFlow<List<BookmarkEntity>> = repository.bookmarksFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allDiffsFlow: StateFlow<List<QiraatDiffEntry>> = repository.allDiffsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _editorState = MutableStateFlow(EditorUiState())
    val editorState: StateFlow<EditorUiState> = _editorState.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>()
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            _isRefreshingCatalog.value = true
            repository.refreshSurahsCatalog()
            _isRefreshingCatalog.value = false
            loadSurahInReader(userPrefs.value.lastSurahId)
            loadEditorSurah(1)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _ayahSearchResults.value = repository.searchCachedAyahs(query)
        }
    }

    fun refreshCatalog() {
        viewModelScope.launch {
            _isRefreshingCatalog.value = true
            val result = repository.refreshSurahsCatalog()
            _isRefreshingCatalog.value = false
            if (result.isFailure) {
                _snackbarEvents.emit("فہرستِ سورتیں اپڈیٹ نہ ہو سکیں — انٹرنیٹ چیک کریں")
            }
        }
    }

    fun loadSurahInReader(
        surahId: Int,
        highlightAyah: Int? = null,
        overrideRiwayah: String? = null
    ) {
        val clampedId = surahId.coerceIn(1, 114)
        _currentReaderSurahId.value = clampedId
        viewModelScope.launch {
            if (overrideRiwayah != null) {
                prefsRepo.setSelectedRiwayah(overrideRiwayah)
            }
            val riwayah = overrideRiwayah ?: userPrefs.value.selectedRiwayah
            val compare = userPrefs.value.compareMode

            val meta = repository.getSurahMetadata(clampedId)
            val surahName = meta?.nameArabic ?: "سورة $clampedId"
            prefsRepo.setLastSurah(clampedId, surahName)

            _readerDataState.update {
                it.copy(
                    surahId = clampedId,
                    surahName = surahName,
                    revelationType = meta?.revelationType ?: "meccan",
                    totalVerses = meta?.totalVerses ?: 0,
                    highlightAyahNumber = highlightAyah,
                    isLoading = true,
                    errorMessage = null
                )
            }

            val primaryResult = repository.getSurahAyahs(clampedId, riwayah)
            var hafsMap = emptyMap<Int, CachedAyahEntity>()
            if (compare && riwayah != "uthmani") {
                val hafsResult = repository.getSurahAyahs(clampedId, "uthmani")
                hafsMap = hafsResult.getOrNull().orEmpty().associateBy { it.ayahNumber }
            }

            primaryResult.fold(
                onSuccess = { ayahs ->
                    _readerDataState.update {
                        it.copy(
                            ayahs = ayahs,
                            hafsAyahsByNumber = hafsMap,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = {
                    _readerDataState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "اس سورہ کا متن لوڈ نہ ہو سکا۔ انٹرنیٹ کنکشن چیک کر کے دوبارہ کوشش کریں۔"
                        )
                    }
                }
            )
        }
    }

    fun selectRiwayah(riwayahCode: String) {
        viewModelScope.launch {
            prefsRepo.setSelectedRiwayah(riwayahCode)
            loadSurahInReader(_currentReaderSurahId.value, overrideRiwayah = riwayahCode)
        }
    }

    fun toggleCompareMode() {
        viewModelScope.launch {
            val next = !userPrefs.value.compareMode
            prefsRepo.setCompareMode(next)
            val surahId = _currentReaderSurahId.value
            val riwayah = userPrefs.value.selectedRiwayah
            if (next && riwayah != "uthmani") {
                val hafsMap = repository.getSurahAyahs(surahId, "uthmani")
                    .getOrNull().orEmpty().associateBy { it.ayahNumber }
                _readerDataState.update { it.copy(hafsAyahsByNumber = hafsMap) }
            }
        }
    }

    fun adjustFontSize(delta: Float) {
        viewModelScope.launch {
            val next = (userPrefs.value.fontSizeSp + delta).coerceIn(20f, 44f)
            prefsRepo.setFontSize(next)
        }
    }

    fun cycleThemeMode() {
        viewModelScope.launch {
            val next = when (userPrefs.value.darkThemeMode) {
                DarkThemeMode.SYSTEM -> DarkThemeMode.DARK
                DarkThemeMode.DARK -> DarkThemeMode.LIGHT
                DarkThemeMode.LIGHT -> DarkThemeMode.SYSTEM
            }
            prefsRepo.setDarkThemeMode(next)
        }
    }

    fun toggleBookmark(ayah: CachedAyahEntity) {
        viewModelScope.launch {
            val state = readerUiState.value
            val added = repository.toggleBookmark(
                surahId = state.surahId,
                ayahNumber = ayah.ayahNumber,
                riwayahCode = state.selectedRiwayah,
                surahName = state.surahName,
                ayahText = ayah.text
            )
            _snackbarEvents.emit(
                if (added) "آیت ${ayah.ayahNumber} بُک مارک میں محفوظ ہو گئی"
                else "آیت ${ayah.ayahNumber} بُک مارک سے ہٹا دی گئی"
            )
        }
    }

    fun deleteBookmark(surahId: Int, ayahNumber: Int) {
        viewModelScope.launch {
            repository.deleteBookmark(surahId, ayahNumber)
            _snackbarEvents.emit("بُک مارک حذف کر دیا گیا")
        }
    }

    // --- Editor Actions ---

    fun loadEditorSurah(surahId: Int, initialAyah: Int = 1, prefillWord: String? = null) {
        viewModelScope.launch {
            _editorState.update { it.copy(surahId = surahId, isLoadingAyahs = true) }
            val result = repository.getSurahAyahs(surahId, "uthmani")
            val ayahs = result.getOrNull().orEmpty()
            val targetAyah = if (ayahs.any { it.ayahNumber == initialAyah }) initialAyah
            else ayahs.firstOrNull()?.ayahNumber ?: 1

            _editorState.update {
                it.copy(
                    surahId = surahId,
                    ayahNumber = targetAyah,
                    ayahsForSurah = ayahs,
                    isLoadingAyahs = false
                )
            }
            if (!prefillWord.isNullOrBlank()) {
                selectEditorWord(prefillWord)
            }
        }
    }

    fun selectEditorAyah(ayahNumber: Int) {
        _editorState.update {
            it.copy(
                ayahNumber = ayahNumber,
                selectedWord = "",
                wordTitle = "",
                matchTokensText = "",
                readingInput = ""
            )
        }
    }

    fun selectEditorWord(word: String) {
        val clean = ArabicTextUtils.normalizeArabic(word)
        _editorState.update {
            it.copy(
                selectedWord = word,
                wordTitle = word,
                matchTokensText = clean,
                readingInput = word
            )
        }
    }

    fun updateEditorFields(
        wordTitle: String? = null,
        matchTokensText: String? = null,
        readingInput: String? = null,
        ruleType: String? = null,
        customReadersText: String? = null,
        noteInput: String? = null
    ) {
        _editorState.update {
            it.copy(
                wordTitle = wordTitle ?: it.wordTitle,
                matchTokensText = matchTokensText ?: it.matchTokensText,
                readingInput = readingInput ?: it.readingInput,
                ruleType = ruleType ?: it.ruleType,
                customReadersText = customReadersText ?: it.customReadersText,
                noteInput = noteInput ?: it.noteInput
            )
        }
    }

    fun toggleEditorReaderChip(name: String) {
        _editorState.update { state ->
            val updated = state.selectedReaders.toMutableSet()
            if (updated.contains(name)) updated.remove(name) else updated.add(name)
            state.copy(
                selectedReaders = updated,
                customReadersText = updated.joinToString("، ")
            )
        }
    }

    fun addPendingVariant() {
        val s = _editorState.value
        val reading = s.readingInput.trim()
        val readers = s.customReadersText.trim()
        if (reading.isEmpty() || readers.isEmpty()) {
            viewModelScope.launch {
                _snackbarEvents.emit("قراءت کی شکل اور قراء کے نام درج کریں")
            }
            return
        }
        val newItem = QiraatDiffItem(
            reading = reading,
            rule = s.ruleType,
            readers = readers,
            note = s.noteInput.trim()
        )
        _editorState.update {
            it.copy(
                pendingItems = it.pendingItems + newItem,
                selectedReaders = emptySet(),
                customReadersText = "",
                noteInput = ""
            )
        }
    }

    fun removePendingVariant(index: Int) {
        _editorState.update { state ->
            val list = state.pendingItems.toMutableList()
            if (index in list.indices) list.removeAt(index)
            state.copy(pendingItems = list)
        }
    }

    fun saveEditorEntry() {
        val s = _editorState.value
        val title = s.wordTitle.trim()
        val tokens = s.matchTokensText.split(",", "،")
            .map { ArabicTextUtils.normalizeArabic(it) }
            .filter { it.isNotEmpty() }

        if (title.isEmpty() || tokens.isEmpty() || s.pendingItems.isEmpty()) {
            viewModelScope.launch {
                _snackbarEvents.emit("پہلے لفظ منتخب کریں اور کم از کم ایک قراءت لسٹ میں شامل کریں")
            }
            return
        }

        viewModelScope.launch {
            repository.saveDiffEntry(
                QiraatDiffEntry(
                    surahId = s.surahId,
                    ayahHafs = s.ayahNumber,
                    wordTitle = title,
                    matchTokens = tokens,
                    items = s.pendingItems,
                    isUserEdited = true
                )
            )
            _editorState.update {
                it.copy(
                    pendingItems = emptyList(),
                    selectedWord = "",
                    wordTitle = "",
                    matchTokensText = "",
                    readingInput = "",
                    noteInput = ""
                )
            }
            _snackbarEvents.emit("اختلافِ قراءت محفوظ ہو گیا (${s.surahId}:${s.ayahNumber})")
        }
    }

    fun deleteSavedDiff(id: Long) {
        viewModelScope.launch {
            repository.deleteDiffEntry(id)
            _snackbarEvents.emit("محفوظ شدہ اندراج حذف کر دیا گیا")
        }
    }

    fun buildExportJson(): String = repository.exportDifferencesJson(allDiffsFlow.value)

    fun findWordDiffs(hafsRefs: List<Int>, word: String, diffs: List<QiraatDiffEntry>): QiraatDiffEntry? {
        val cleanWord = ArabicTextUtils.normalizeArabic(word)
        if (cleanWord.isEmpty()) return null
        for (ref in hafsRefs) {
            val entriesForAyah = diffs.filter { it.ayahHafs == ref }
            for (entry in entriesForAyah) {
                val matched = entry.matchTokens.any { token ->
                    val normToken = ArabicTextUtils.normalizeArabic(token)
                    normToken.isNotEmpty() && (cleanWord == normToken || cleanWord.contains(normToken))
                }
                if (matched) return entry
            }
        }
        return null
    }
}
