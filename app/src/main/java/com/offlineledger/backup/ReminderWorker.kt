package com.offlineledger.backup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.offlineledger.MyApp
import com.offlineledger.data.model.ReminderLog
import com.offlineledger.utils.formatCurrency
import kotlin.math.abs

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Check SMS permission
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.failure()
        }

        return try {
            val app = applicationContext as MyApp
            val repo = app.repository
            val persons = repo.getReminderEnabledPersons()

            for (person in persons) {
                if (person.mobileNumber.isBlank()) continue

                val balance = repo.getBalanceForPersonSync(person.id)
                // Only send if they owe money (negative balance means user owes, positive means they owe user)
                // Based on the app's convention: positive balance = they owe user
                if (balance <= 0) continue

                val amountStr = formatCurrency(abs(balance))
                val message = "You have a pending balance of $amountStr. Please pay at your earliest convenience."

                try {
                    val smsManager = SmsManager.getDefault()
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(
                        person.mobileNumber,
                        null,
                        parts,
                        null,
                        null
                    )

                    // Log the sent message
                    repo.insertReminderLog(
                        ReminderLog(
                            personName = person.name,
                            mobileNumber = person.mobileNumber,
                            message = message,
                            sentAt = System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Continue to next person even if one fails
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
