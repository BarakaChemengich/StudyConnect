package com.studyconnect.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [User::class, Tutor::class, Booking::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tutorDao(): TutorDao
    abstract fun bookingDao(): BookingDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "studyconnect.db")
                .addCallback(SeedCallback)
                .build()
    }
}

/**
 * Runs once, when the database file is first created. It inserts the Peer Support Office
 * (the Programme Administrator from the concept note) and a few sample tutors so the
 * list is not empty on first launch. All later data is created by the user.
 */
private object SeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.execSQL(
            "INSERT INTO users (id, name, admissionNo, email, passwordHash) " +
                "VALUES (1, 'Peer Support Office', 'ADMIN000', 'admin@studyconnect.app', '!')"
        )
        // name, subject, rating, bio, available (1 = yes)
        val sample = listOf(
            "'Amina Wanjiru', 'Statistics', 4.8, 'Scored an A in Statistics. Happy to explain hypothesis testing and regression.', 1",
            "'Brian Otieno', 'Programming', 4.6, 'Kotlin, Java and OOP. I like working through code together.', 1",
            "'Cynthia Mwangi', 'Databases', 4.7, 'SQL, ER diagrams and normalisation made simple.', 1",
            "'David Kamau', 'Mathematics', 4.4, 'Calculus and discrete maths revision sessions.', 1",
            "'Esther Njeri', 'Accounting', 4.9, 'Financial accounting and bookkeeping basics.', 0",
            "'Felix Mutua', 'Networking', 4.2, 'Subnetting, routing and exam practice for networks.', 1"
        )
        sample.forEach { values ->
            db.execSQL(
                "INSERT INTO tutors (ownerId, name, subject, rating, bio, available) VALUES (1, $values)"
            )
        }
    }
}
