package com.studyconnect.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open Library public search API (no API key needed).
 * Example: https://openlibrary.org/search.json?q=statistics&limit=20
 */
interface OpenLibraryApi {
    @GET("search.json")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "key,title,author_name,first_publish_year"
    ): SearchResponse
}

// Every field is nullable because Gson ignores Kotlin null-safety: a missing JSON field becomes null.
data class SearchResponse(
    val numFound: Int?,
    val docs: List<BookDoc>?
)

data class BookDoc(
    val key: String?,
    val title: String?,
    @SerializedName("author_name") val authorNames: List<String>?,
    @SerializedName("first_publish_year") val firstPublishYear: Int?
)
