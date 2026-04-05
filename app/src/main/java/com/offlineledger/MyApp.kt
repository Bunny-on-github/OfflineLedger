package com.offlineledger

import android.app.Application
import androidx.work.*
import com.offlineledger.backup.BackupWorker
import com.offlineledger.backup.ReminderWorker
import com.offlineledger.data.db.AppDatabase
import com.offlineledger.data.repository.LedgerRepository
import java.util.*
import java.util.concurrent.TimeUnit

class MyApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy {
        LedgerRepository(database.personDao(), database.transactionDao(), database.reminderLogDao())
    }

    override fun onCreate() {
        super.onCreate()
        scheduleBackupIfEnabled(forceReschedule = false)
        scheduleReminderChecks()
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

    private fun scheduleReminderChecks() {
        // Run every day at 10:00 AM and let worker decide which persons are due.
        val now = Calendar.getInstance()
        val nextRun = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val initialDelay = nextRun.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "person_reminder_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
