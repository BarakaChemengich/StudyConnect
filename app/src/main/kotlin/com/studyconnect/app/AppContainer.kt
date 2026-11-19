package com.studyconnect.app

import android.content.Context
import com.studyconnect.app.data.SessionManager
import com.studyconnect.app.data.local.AppDatabase
import com.studyconnect.app.data.remote.OpenLibraryApi
import com.studyconnect.app.data.repository.AuthRepository
import com.studyconnect.app.data.repository.BookingRepository
import com.studyconnect.app.data.repository.ResourceRepository
import com.studyconnect.app.data.repository.TutorRepository
import com.studyconnect.app.work.ReminderScheduler
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Manual dependency injection: builds every object once and shares it with the ViewModels. */
class AppContainer(context: Context) {
    private val database = AppDatabase.build(context)

    val session = SessionManager(context)
    val reminderScheduler = ReminderScheduler(context)

    val authRepository = AuthRepository(database.userDao(), database.tutorDao(), session)
    val tutorRepository = TutorRepository(database.tutorDao())
    val bookingRepository = BookingRepository(database.bookingDao(), database.tutorDao())

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "StudyConnect/1.0 (university student project)")
                    .build()
            )
        }
        .build()

    private val api: OpenLibraryApi = Retrofit.Builder()
        .baseUrl("https://openlibrary.org/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenLibraryApi::class.java)

    val resourceRepository = ResourceRepository(api)
}