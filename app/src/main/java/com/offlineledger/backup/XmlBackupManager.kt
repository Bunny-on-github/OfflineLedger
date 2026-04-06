package com.offlineledger.backup

import android.content.Context
import android.os.Environment
import android.util.Xml
import com.offlineledger.data.model.Person
import com.offlineledger.data.model.Transaction
import com.offlineledger.data.repository.LedgerRepository
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object XmlBackupManager {

    private const val DEFAULT_FOLDER = "BunnysLedger"
    private const val PREF_BACKUP_PATH = "backup_path"

    /** Returns the backup directory, creating it if needed. */
    fun getBackupDir(context: Context): File {
        val prefs = context.getSharedPreferences("ledger_prefs", Context.MODE_PRIVATE)
        val customPath = prefs.getString(PREF_BACKUP_PATH, null)

        val dir = if (!customPath.isNullOrBlank()) {
            File(customPath)
        } else {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                DEFAULT_FOLDER
            )
        }
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun setBackupPath(context: Context, path: String) {
        context.getSharedPreferences("ledger_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_BACKUP_PATH, path)
            .apply()
    }

    fun getBackupPath(context: Context): String {
        val prefs = context.getSharedPreferences("ledger_prefs", Context.MODE_PRIVATE)
        val custom = prefs.getString(PREF_BACKUP_PATH, null)
        return custom ?: File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            DEFAULT_FOLDER
        ).absolutePath
    }

    // ── EXPORT ─────────────────────────────────────────────────────────────

    suspend fun backup(context: Context, repo: LedgerRepository): String {
        val persons = repo.getAllPersonsSync()
        val transactions = repo.getAllTransactionsSync()

        val dir = getBackupDir(context)

        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "ledger_backup_$stamp.xml")

        val fw = FileWriter(file)
        val ser: XmlSerializer = Xml.newSerializer()
        ser.setOutput(fw)
        ser.startDocument("UTF-8", true)
        ser.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)

        ser.startTag(null, "ledger")
        ser.attribute(null, "exportedAt", Date().toString())
        ser.attribute(null, "version", "4")

        for (p in persons) {
            val bal = repo.getBalanceForPersonSync(p.id)
            ser.startTag(null, "person")
            ser.attribute(null, "id", p.id.toString())
            ser.attribute(null, "name", p.name)
            ser.attribute(null, "mobileNumber", p.mobileNumber)
            ser.attribute(null, "isBlacklisted", p.isBlacklisted.toString())
            ser.attribute(null, "reminderEnabled", p.reminderEnabled.toString())
            ser.attribute(null, "reminderFrequency", p.reminderFrequency)
            ser.attribute(null, "reminderMessagePrefix", p.reminderMessagePrefix)
            ser.attribute(null, "reminderMessageSuffix", p.reminderMessageSuffix)
            ser.attribute(null, "createdAt", p.createdAt.toString())
            ser.attribute(null, "balance", String.format("%.2f", bal))

            val personTxns = transactions.filter { it.personId == p.id }
            for (t in personTxns) {
                ser.startTag(null, "transaction")
                ser.attribute(null, "id", t.id.toString())
                ser.attribute(null, "amount", String.format("%.2f", t.amount))
                ser.attribute(null, "label", t.label)
                ser.attribute(null, "details", t.details)
                ser.attribute(null, "timestamp", t.timestamp.toString())
                ser.endTag(null, "transaction")
            }

            ser.endTag(null, "person")
        }

        ser.endTag(null, "ledger")
        ser.endDocument()
        fw.close()

        return file.absolutePath
    }

    // ── IMPORT ─────────────────────────────────────────────────────────────

    /**
     * Auto-detects the XML format and delegates to the correct restore method.
     * - Custom format: root <ledger> contains <person> children
     * - Reference format: root <ledger> contains <accounts> with <account> children
     */
    suspend fun restore(context: Context, repo: LedgerRepository, filePath: String): Int {
        val file = File(filePath)
        if (!file.exists()) throw IllegalArgumentException("File not found: $filePath")

        // Peek at the XML to detect format
        val format = detectFormat(file)
        return if (format == "reference") {
            restoreFromReferenceXml(repo, file)
        } else {
            restoreFromCustomXml(repo, file)
        }
    }

    private fun detectFormat(file: File): String {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(FileReader(file))

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "person" -> return "custom"
                    "accounts", "payees", "categories" -> return "reference"
                }
            }
            eventType = parser.next()
        }
        return "custom" // default fallback
    }

    // ── Custom format restore ──────────────────────────────────────────────

    private suspend fun restoreFromCustomXml(repo: LedgerRepository, file: File): Int {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(FileReader(file))

        var personsImported = 0
        var newPersonId: Long = 0

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "person" -> {
                            val name = parser.getAttributeValue(null, "name") ?: "Unknown"
                            val mobileNumber = parser.getAttributeValue(null, "mobileNumber") ?: ""
                            val isBlacklisted = parser.getAttributeValue(null, "isBlacklisted")?.toBooleanStrictOrNull() ?: false
                            val reminderEnabled = parser.getAttributeValue(null, "reminderEnabled")?.toBooleanStrictOrNull() ?: false
                            val reminderFrequency = parser.getAttributeValue(null, "reminderFrequency") ?: "weekly"
                            val reminderMessagePrefix = parser.getAttributeValue(null, "reminderMessagePrefix")
                                ?: "You have a pending balance of"
                            val reminderMessageSuffix = parser.getAttributeValue(null, "reminderMessageSuffix")
                                ?: "Please pay at your earliest convenience."
                            val createdAt = parser.getAttributeValue(null, "createdAt")?.toLongOrNull()
                                ?: System.currentTimeMillis()

                            // Duplicate detection by name
                            val existing = repo.getPersonByName(name)
                            if (existing != null) {
                                newPersonId = existing.id
                            } else {
                                val person = Person(
                                    name = name,
                                    mobileNumber = mobileNumber,
                                    isBlacklisted = isBlacklisted,
                                    reminderEnabled = reminderEnabled,
                                    reminderFrequency = reminderFrequency,
                                    reminderMessagePrefix = reminderMessagePrefix,
                                    reminderMessageSuffix = reminderMessageSuffix,
                                    createdAt = createdAt
                                )
                                newPersonId = repo.insertPersonDirect(person)
                                personsImported++
                            }
                        }
                        "transaction" -> {
                            val amount = parser.getAttributeValue(null, "amount")?.toDoubleOrNull() ?: 0.0
                            val label = parser.getAttributeValue(null, "label") ?: ""
                            val details = parser.getAttributeValue(null, "details") ?: ""
                            val timestamp = parser.getAttributeValue(null, "timestamp")?.toLongOrNull()
                                ?: System.currentTimeMillis()

                            val transaction = Transaction(
                                personId = newPersonId,
                                amount = amount,
                                label = label,
                                details = details,
                                timestamp = timestamp
                            )
                            repo.insertTransactionDirect(transaction)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return personsImported
    }

    // ── Reference XML format restore ───────────────────────────────────────
    // Format:
    //   <ledger>
    //     <payees> <payee id="..." name="..." /> </payees>
    //     <categories> ... </categories>  (ignored)
    //     <accounts>
    //       <account title="PersonName">
    //         <transaction>
    //           <item payeeId="..." amount="..." timestamp="..." details="..." />
    //         </transaction>
    //       </account>
    //     </accounts>
    //   </ledger>

    private suspend fun restoreFromReferenceXml(repo: LedgerRepository, file: File): Int {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(FileReader(file))

        // First pass: collect payee names
        val payeeMap = mutableMapOf<String, String>() // id -> name
        var personsImported = 0
        var currentPersonId: Long = 0
        var insideAccounts = false
        var insidePayees = false

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "payees" -> insidePayees = true
                        "payee" -> {
                            if (insidePayees) {
                                val id = parser.getAttributeValue(null, "id") ?: ""
                                val name = parser.getAttributeValue(null, "name") ?: ""
                                if (id.isNotBlank()) {
                                    payeeMap[id] = name
                                }
                            }
                        }
                        "accounts" -> insideAccounts = true
                        "account" -> {
                            if (insideAccounts) {
                                val title = parser.getAttributeValue(null, "title")
                                    ?: parser.getAttributeValue(null, "name")
                                    ?: "Unknown"

                                // Duplicate detection
                                val existing = repo.getPersonByName(title)
                                if (existing != null) {
                                    currentPersonId = existing.id
                                } else {
                                    val person = Person(name = title)
                                    currentPersonId = repo.insertPersonDirect(person)
                                    personsImported++
                                }
                            }
                        }
                        "item" -> {
                            if (insideAccounts && currentPersonId > 0) {
                                val amount = parser.getAttributeValue(null, "amount")?.toDoubleOrNull() ?: 0.0
                                val timestamp = parser.getAttributeValue(null, "timestamp")?.toLongOrNull()
                                    ?: parser.getAttributeValue(null, "date")?.toLongOrNull()
                                    ?: System.currentTimeMillis()
                                val details = parser.getAttributeValue(null, "details")
                                    ?: parser.getAttributeValue(null, "note")
                                    ?: ""

                                // Resolve payee ID to name for label
                                val payeeId = parser.getAttributeValue(null, "payeeId")
                                    ?: parser.getAttributeValue(null, "payee_id")
                                    ?: ""
                                val payeeName = if (payeeId.isNotBlank()) {
                                    payeeMap[payeeId] ?: ""
                                } else {
                                    ""
                                }

                                val label = parser.getAttributeValue(null, "label")
                                    ?: payeeName

                                val transaction = Transaction(
                                    personId = currentPersonId,
                                    amount = amount,
                                    label = label,
                                    details = details,
                                    timestamp = timestamp
                                )
                                repo.insertTransactionDirect(transaction)
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "payees" -> insidePayees = false
                        "accounts" -> insideAccounts = false
                    }
                }
            }
            eventType = parser.next()
        }

        return personsImported
    }

    /** Returns list of XML backup files in the backup directory, newest first. */
    fun listBackups(context: Context): List<File> {
        val dir = getBackupDir(context)
        return dir.listFiles { f -> f.extension.equals("xml", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }
}
