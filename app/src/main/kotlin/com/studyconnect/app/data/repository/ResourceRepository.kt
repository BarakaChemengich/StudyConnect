package com.studyconnect.app.data.repository

import com.studyconnect.app.data.remote.OpenLibraryApi
import com.studyconnect.app.util.AppResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/** A learning resource (book) shown on the Resources screen. */
data class Resource(val key: String, val title: String, val authors: String, val year: Int?)

class ResourceRepository(private val api: OpenLibraryApi) {

    suspend fun search(subject: String): AppResult<List<Resource>> = try {
        val response = api.search(subject)
        val items = response.docs.orEmpty().mapNotNull { doc ->
            val title = doc.title ?: return@mapNotNull null
            Resource(
                key = doc.key.orEmpty(),
                title = title,
                authors = doc.authorNames?.take(2)?.joinToString(", ") ?: "Unknown author",
                year = doc.firstPublishYear
            )
        }
        AppResult.Success(items)
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        AppResult.Failure("No internet connection. Check your network and try again.")
    } catch (e: HttpException) {
        AppResult.Failure("The resource server had a problem (code ${e.code()}). Try again later.")
    } catch (e: Exception) {
        AppResult.Failure("Something went wrong while loading resources.")
    }
}
