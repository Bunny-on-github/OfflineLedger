package com.offlineledger

import android.app.Application
import androidx.work.ExistingWorkPolicy
import androidx.work.*
import com.offlineledger.backup.BackupWorker
import com.offlineledger.backup.ReminderWorker
import com.offlineledger.data.db.AppDatabase
import com.offlineledger.data.repository.LedgerRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MyApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy {
        LedgerRepository(database.personDao(), database.transactionDao(), database.reminderLogDao())
    }

    override fun onCreate() {
        super.onCreate()
        scheduleBackupIfEnabled(forceReschedule = false)
        WorkManager.getInstance(this).cancelUniqueWork(LEGACY_REMINDER_WORK_NAME)
        checkAndRunDailyReminderIfNeeded()
    }

    fun scheduleBackupIfEnabled(forceReschedule: Boolean = true) {
        val prefs = getSharedPreferences("ledger_prefs", MODE_PRIVATE)
        val enabled = prefs.getBoolean("backup_enabled", true)
        if (!enabled) {
            WorkManager.getInstance(this).cancelUniqueWork("ledger_backup")
            return
        }

        val intervalDays = prefs.getInt("backup_interval_days", 1).toLong()
        val request = PeriodicWorkRequestBuilder<BackupWorker>(intervalDays, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ledger_backup",
            if (forceReschedule) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun checkAndRunDailyReminderIfNeeded() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val today = SimpleDateFormat(DATE_FORMAT, Locale.US).format(Date())
        val lastRunDate = prefs.getString(KEY_LAST_REMINDER_RUN_DATE, null)
        if (lastRunDate == today) return

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setConstraints(Constraints.Builder().build())
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            DAILY_REMINDER_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        const val PREFS_NAME = "ledger_prefs"
        const val KEY_LAST_REMINDER_RUN_DATE = "last_reminder_run_date"
        const val DATE_FORMAT = "yyyy-MM-dd"
        const val DAILY_REMINDER_WORK_NAME = "daily_person_reminder"
        private const val LEGACY_REMINDER_WORK_NAME = "person_reminder_check"
    }
}
