package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.AyahSearchResult
import com.example.data.local.BookmarkEntity
import com.example.data.local.CachedAyahEntity
import com.example.data.local.CachedSurahEntity
import com.example.data.local.QiraatDiffEntity
import com.example.data.local.SeedData
import com.example.data.model.ArabicTextUtils
import com.example.data.model.QiraatDiffEntry
import com.example.data.model.QiraatDiffItem
import com.example.data.remote.QuranApiService
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class ExportDiffGroup(
    val match: List<String>,
    val title: String,
    val items: List<QiraatDiffItem>
)

class QuranRepository(
    private val db: AppDatabase,
    private val api: QuranApiService,
    private val moshi: Moshi
) {
    private val surahDao = db.surahDao()
    private val ayahDao = db.ayahDao()
    private val bookmarkDao = db.bookmarkDao()
    private val diffDao = db.qiraatDiffDao()

    private val itemsListType = Types.newParameterizedType(List::class.java, QiraatDiffItem::class.java)
    private val itemsAdapter = moshi.adapter<List<QiraatDiffItem>>(itemsListType)

    val surahsFlow: Flow<List<CachedSurahEntity>> = surahDao.observeAllSurahs()
    val cachedHafsSurahIdsFlow: Flow<Set<Int>> = ayahDao.observeCachedHafsSurahIds().map { it.toSet() }
    val bookmarksFlow: Flow<List<BookmarkEntity>> = bookmarkDao.observeBookmarks()
    val allDiffsFlow: Flow<List<QiraatDiffEntry>> = diffDao.observeAllDiffs().map { list ->
        list.map { it.toDomain() }
    }

    fun observeDiffsForSurah(surahId: Int): Flow<List<QiraatDiffEntry>> =
        diffDao.observeDiffsForSurah(surahId).map { list -> list.map { it.toDomain() } }

    fun observeBookmarkedAyahs(surahId: Int): Flow<Set<Int>> =
        bookmarkDao.observeBookmarkedAyahNumbers(surahId).map { it.toSet() }

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (surahDao.countSurahs() == 0) {
            surahDao.insertAll(SeedData.initialSurahs)
            ayahDao.insertAll(SeedData.initialAyahs)
        }
        if (diffDao.countDiffs() == 0) {
            diffDao.insertAll(SeedData.initialDiffs(moshi))
        }
    }

    suspend fun refreshSurahsCatalog(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val remote = api.getChapters()
            val entities = remote.map { dto ->
                CachedSurahEntity(
                    id = dto.id,
                    nameArabic = dto.name,
                    transliteration = dto.transliteration,
                    revelationType = dto.type,
                    totalVerses = dto.totalVerses
                )
            }
            if (entities.isNotEmpty()) {
                surahDao.insertAll(entities)
            }
        }
    }

    suspend fun getSurahMetadata(surahId: Int): CachedSurahEntity? = withContext(Dispatchers.IO) {
        surahDao.getSurahById(surahId)
    }

    suspend fun getSurahAyahs(
        surahId: Int,
        riwayahCode: String
    ): Result<List<CachedAyahEntity>> = withContext(Dispatchers.IO) {
        val cached = ayahDao.getSurahAyahs(riwayahCode, surahId)
        if (cached.isNotEmpty()) {
            return@withContext Result.success(cached)
        }

        runCatching {
            val remote = api.getChapter(riwayahCode, surahId)
            val entities = remote.verses.map { verse ->
                val refsCsv = verse.numberInHafs?.refs?.joinToString(",") ?: verse.id.toString()
                CachedAyahEntity(
                    riwayahCode = riwayahCode,
                    surahId = surahId,
                    ayahNumber = verse.id,
                    numberInHafsCsv = refsCsv,
                    text = verse.text,
                    textClean = ArabicTextUtils.normalizeArabic(verse.text)
                )
            }
            ayahDao.insertAll(entities)
            entities
        }
    }

    suspend fun searchCachedAyahs(query: String): List<AyahSearchResult> = withContext(Dispatchers.IO) {
        val clean = ArabicTextUtils.normalizeArabic(query)
        if (clean.length < 2) return@withContext emptyList()
        ayahDao.searchHafsAyahs(clean)
    }

    suspend fun toggleBookmark(
        surahId: Int,
        ayahNumber: Int,
        riwayahCode: String,
        surahName: String,
        ayahText: String
    ): Boolean = withContext(Dispatchers.IO) {
        val existing = bookmarkDao.findBookmark(surahId, ayahNumber)
        if (existing != null) {
            bookmarkDao.delete(surahId, ayahNumber)
            false
        } else {
            val preview = ayahText.take(90)
            bookmarkDao.insert(
                BookmarkEntity(
                    surahId = surahId,
                    ayahNumber = ayahNumber,
                    riwayahCode = riwayahCode,
                    surahName = surahName,
                    ayahPreview = preview
                )
            )
            true
        }
    }

    suspend fun deleteBookmark(surahId: Int, ayahNumber: Int) = withContext(Dispatchers.IO) {
        bookmarkDao.delete(surahId, ayahNumber)
    }

    suspend fun saveDiffEntry(entry: QiraatDiffEntry) = withContext(Dispatchers.IO) {
        val entity = QiraatDiffEntity(
            id = entry.id,
            surahId = entry.surahId,
            ayahHafs = entry.ayahHafs,
            wordTitle = entry.wordTitle,
            matchTokensCsv = entry.matchTokens.joinToString(","),
            itemsJson = itemsAdapter.toJson(entry.items),
            isUserEdited = true
        )
        diffDao.insert(entity)
    }

    suspend fun deleteDiffEntry(id: Long) = withContext(Dispatchers.IO) {
        diffDao.deleteById(id)
    }

    fun exportDifferencesJson(entries: List<QiraatDiffEntry>): String {
        val grouped = entries.groupBy { "${it.surahId}:${it.ayahHafs}" }
            .mapValues { (_, list) ->
                list.map { entry ->
                    ExportDiffGroup(
                        match = entry.matchTokens,
                        title = entry.wordTitle,
                        items = entry.items
                    )
                }
            }
        val groupListType = Types.newParameterizedType(List::class.java, ExportDiffGroup::class.java)
        val mapType = Types.newParameterizedType(Map::class.java, String::class.java, groupListType)
        val adapter = moshi.adapter<Map<String, List<ExportDiffGroup>>>(mapType).indent("  ")
        return adapter.toJson(grouped)
    }

    private fun QiraatDiffEntity.toDomain(): QiraatDiffEntry {
        val parsedItems = runCatching { itemsAdapter.fromJson(itemsJson) }.getOrNull().orEmpty()
        val tokens = matchTokensCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return QiraatDiffEntry(
            id = id,
            surahId = surahId,
            ayahHafs = ayahHafs,
            wordTitle = wordTitle,
            matchTokens = tokens,
            items = parsedItems,
            isUserEdited = isUserEdited
        )
    }
}
