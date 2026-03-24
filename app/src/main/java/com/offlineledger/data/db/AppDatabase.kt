package com.offlineledger.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.offlineledger.data.model.Person
import com.offlineledger.data.model.ReminderLog
import com.offlineledger.data.model.Transaction

@Database(
    entities = [Person::class, Transaction::class, ReminderLog::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun personDao(): PersonDao
    abstract fun transactionDao(): TransactionDao
    abstract fun reminderLogDao(): ReminderLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE persons ADD COLUMN mobileNumber TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE persons ADD COLUMN isBlacklisted INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE persons ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reminder_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        personName TEXT NOT NULL,
                        mobileNumber TEXT NOT NULL,
                        message TEXT NOT NULL,
                        sentAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ledger_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { INSTANCE = it }
            }
    }
}
