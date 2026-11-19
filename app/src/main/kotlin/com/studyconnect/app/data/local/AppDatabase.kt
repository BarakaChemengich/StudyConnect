package com.studyconnect.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Defines the Room database and the entities used by the application.
@Database(
    entities = [User::class, Tutor::class, Booking::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tutorDao(): TutorDao
    abstract fun bookingDao(): BookingDao

    companion object {

        // Holds the single database instance shared across the application.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Returns the existing database instance or creates one if it does not exist.
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studyconnect.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        // Maintains compatibility with existing calls to AppDatabase.build(context).
        fun build(context: Context): AppDatabase = getInstance(context)
    }
}