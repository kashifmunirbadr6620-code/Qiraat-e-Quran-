package com.example.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class SurahDto(
    val id: Int,
    val name: String,
    val transliteration: String = "",
    val type: String = "",
    @Json(name = "total_verses") val totalVerses: Int = 0
)

@JsonClass(generateAdapter = true)
data class VerseDto(
    val id: Int,
    val text: String,
    @Json(name = "number_in_hafs") val numberInHafs: NumberInHafs? = null
)

data class NumberInHafs(val refs: List<Int>)

class NumberInHafsAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): NumberInHafs? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                null
            }
            JsonReader.Token.NUMBER -> {
                NumberInHafs(listOf(reader.nextInt()))
            }
            JsonReader.Token.BEGIN_ARRAY -> {
                val list = mutableListOf<Int>()
                reader.beginArray()
                while (reader.hasNext()) {
                    list.add(reader.nextInt())
                }
                reader.endArray()
                NumberInHafs(list)
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: NumberInHafs?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.beginArray()
            value.refs.forEach { writer.value(it) }
            writer.endArray()
        }
    }
}

@JsonClass(generateAdapter = true)
data class ChapterContentDto(
    val id: Int,
    val verses: List<VerseDto>
)

interface QuranApiService {
    @GET("chapters.json")
    suspend fun getChapters(): List<SurahDto>

    @GET("text/{script}/chapters/{surahId}.json")
    suspend fun getChapter(
        @Path("script") script: String,
        @Path("surahId") surahId: Int
    ): ChapterContentDto

    companion object {
        private const val BASE_URL = "https://quran-json.risanb.com/"

        fun createMoshi(): Moshi = Moshi.Builder()
            .add(NumberInHafsAdapter())
            .build()

        fun create(moshi: Moshi = createMoshi()): QuranApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(QuranApiService::class.java)
        }
    }
}
