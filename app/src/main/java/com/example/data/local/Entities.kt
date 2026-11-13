package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "cached_surahs")
data class CachedSurahEntity(
    @PrimaryKey val id: Int,
    val nameArabic: String,
    val transliteration: String,
    val revelationType: String,
    val totalVerses: Int
)

@Entity(
    tableName = "cached_ayahs",
    primaryKeys = ["riwayahCode", "surahId", "ayahNumber"],
    indices = [
        Index(value = ["riwayahCode", "surahId"]),
        Index(value = ["textClean"])
    ]
)
data class CachedAyahEntity(
    val riwayahCode: String,
    val surahId: Int,
    val ayahNumber: Int,
    val numberInHafsCsv: String,
    val text: String,
    val textClean: String
) {
    fun hafsRefs(): List<Int> {
        if (numberInHafsCsv.isBlank()) return listOf(ayahNumber)
        return numberInHafsCsv.split(",").mapNotNull { it.trim().toIntOrNull() }
            .ifEmpty { listOf(ayahNumber) }
    }
}

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["surahId", "ayahNumber"], unique = true)]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surahId: Int,
    val ayahNumber: Int,
    val riwayahCode: String,
    val surahName: String,
    val ayahPreview: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "qiraat_diffs",
    indices = [Index(value = ["surahId", "ayahHafs"])]
)
data class QiraatDiffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surahId: Int,
    val ayahHafs: Int,
    val wordTitle: String,
    val matchTokensCsv: String,
    val itemsJson: String,
    val isUserEdited: Boolean = false
)
