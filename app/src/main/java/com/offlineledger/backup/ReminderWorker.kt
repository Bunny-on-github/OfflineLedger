package com.offlineledger.backup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.offlineledger.MyApp
import com.offlineledger.data.model.Person
import com.offlineledger.data.model.ReminderFrequency
import com.offlineledger.data.model.ReminderLog
import com.offlineledger.utils.formatCurrency
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.first
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
            val today = Calendar.getInstance()
            val dayOfMonth = today.get(Calendar.DAY_OF_MONTH)
            val isSunday = today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val sentTodayPeople = repo.getAllReminderLogs().first()
                .asSequence()
                .filter { formatDateKey(it.sentAt) == todayKey }
                .map { it.personName }
                .toMutableSet()

            for (person in persons) {
                if (person.mobileNumber.isBlank()) continue
                if (!isDueToday(person, dayOfMonth, isSunday)) continue

                val balance = repo.getBalanceForPersonSync(person.id)
                // Only send if they owe money (negative balance means user owes, positive means they owe user)
                // Based on the app's convention: positive balance = they owe user
                if (balance <= 0) continue

                val amountStr = formatCurrency(abs(balance))
                val message = formatReminderMessage(person, amountStr)
                if (sentTodayPeople.contains(person.name)) continue

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
                    sentTodayPeople.add(person.name)
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

    private fun isDueToday(person: Person, dayOfMonth: Int, isSunday: Boolean): Boolean {
        return when (ReminderFrequency.fromValue(person.reminderFrequency)) {
            ReminderFrequency.DAILY -> true
            ReminderFrequency.WEEKLY -> isSunday
            ReminderFrequency.TEN_DAYS -> dayOfMonth == 1 || dayOfMonth == 11 || dayOfMonth == 21 || dayOfMonth == 31
            ReminderFrequency.EVERY_THREE_DAYS_OF_MONTH -> dayOfMonth % 3 == 0
        }
    }

    private fun formatReminderMessage(person: Person, amount: String): String {
        val prefix = person.reminderMessagePrefix.trim()
        val suffix = person.reminderMessageSuffix.trim()
        return listOf(prefix, amount, suffix)
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }

    private fun formatDateKey(ts: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(ts))
}
