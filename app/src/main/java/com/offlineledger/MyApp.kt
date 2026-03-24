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
        scheduleBackupIfEnabled()
        scheduleWeeklyReminder()
    }

    fun scheduleBackupIfEnabled() {
        val prefs = getSharedPreferences("ledger_prefs", MODE_PRIVATE)
        val enabled = prefs.getBoolean("backup_enabled", true)
        WorkManager.getInstance(this).cancelUniqueWork("ledger_backup")
        if (!enabled) return

        val intervalDays = prefs.getInt("backup_interval_days", 1).toLong()
        val request = PeriodicWorkRequestBuilder<BackupWorker>(intervalDays, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ledger_backup",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun scheduleWeeklyReminder() {
        // Calculate initial delay to next Sunday 10:00 AM
        val now = Calendar.getInstance()
        val nextSunday = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        val initialDelay = nextSunday.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weekly_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
