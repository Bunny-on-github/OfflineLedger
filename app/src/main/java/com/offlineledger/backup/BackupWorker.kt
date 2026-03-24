package com.offlineledger.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.offlineledger.MyApp

class BackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val repo = (applicationContext as MyApp).repository
            XmlBackupManager.backup(applicationContext, repo)
            applicationContext
                .getSharedPreferences("ledger_prefs", Context.MODE_PRIVATE)
                .edit()
                .putLong("last_backup_ts", System.currentTimeMillis())
                .apply()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
