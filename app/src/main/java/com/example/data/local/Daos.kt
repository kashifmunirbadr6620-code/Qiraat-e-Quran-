package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class AyahSearchResult(
    val surahId: Int,
    val ayahNumber: Int,
    val text: String,
    val surahName: String
)

@Dao
interface SurahDao {
    @Query("SELECT * FROM cached_surahs ORDER BY id ASC")
    fun observeAllSurahs(): Flow<List<CachedSurahEntity>>

    @Query("SELECT * FROM cached_surahs WHERE id = :surahId LIMIT 1")
    suspend fun getSurahById(surahId: Int): CachedSurahEntity?

    @Query("SELECT COUNT(*) FROM cached_surahs")
    suspend fun countSurahs(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(surahs: List<CachedSurahEntity>)
}

@Dao
interface AyahDao {
    @Query(
        "SELECT * FROM cached_ayahs WHERE riwayahCode = :riwayahCode AND surahId = :surahId ORDER BY ayahNumber ASC"
    )
    suspend fun getSurahAyahs(riwayahCode: String, surahId: Int): List<CachedAyahEntity>

    @Query("SELECT DISTINCT surahId FROM cached_ayahs WHERE riwayahCode = 'uthmani'")
    fun observeCachedHafsSurahIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ayahs: List<CachedAyahEntity>)

    @Query(
        """
        SELECT a.surahId AS surahId, a.ayahNumber AS ayahNumber, a.text AS text,
               COALESCE(s.nameArabic, 'سورة ' || a.surahId) AS surahName
        FROM cached_ayahs a
        LEFT JOIN cached_surahs s ON a.surahId = s.id
        WHERE a.riwayahCode = 'uthmani' AND a.textClean LIKE '%' || :cleanQuery || '%'
        ORDER BY a.surahId ASC, a.ayahNumber ASC
        LIMIT 50
        """
    )
    suspend fun searchHafsAyahs(cleanQuery: String): List<AyahSearchResult>
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT ayahNumber FROM bookmarks WHERE surahId = :surahId")
    fun observeBookmarkedAyahNumbers(surahId: Int): Flow<List<Int>>

    @Query("SELECT * FROM bookmarks WHERE surahId = :surahId AND ayahNumber = :ayahNumber LIMIT 1")
    suspend fun findBookmark(surahId: Int, ayahNumber: Int): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE surahId = :surahId AND ayahNumber = :ayahNumber")
    suspend fun delete(surahId: Int, ayahNumber: Int)
}

@Dao
interface QiraatDiffDao {
    @Query("SELECT * FROM qiraat_diffs WHERE surahId = :surahId ORDER BY ayahHafs ASC, id ASC")
    fun observeDiffsForSurah(surahId: Int): Flow<List<QiraatDiffEntity>>

    @Query("SELECT * FROM qiraat_diffs ORDER BY surahId ASC, ayahHafs ASC, id ASC")
    fun observeAllDiffs(): Flow<List<QiraatDiffEntity>>

    @Query("SELECT COUNT(*) FROM qiraat_diffs")
    suspend fun countDiffs(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QiraatDiffEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<QiraatDiffEntity>)

    @Query("DELETE FROM qiraat_diffs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
