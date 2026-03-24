package com.offlineledger.ui.settings

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.offlineledger.MyApp
import com.offlineledger.backup.XmlBackupManager
import com.offlineledger.databinding.ActivitySettingsBinding
import com.offlineledger.ui.logs.LogsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var b: ActivitySettingsBinding
    private val prefs by lazy { getSharedPreferences("ledger_prefs", MODE_PRIVATE) }
    private val repo by lazy { (application as MyApp).repository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "Settings" }

        loadPrefs()
        setupListeners()
    }

    private fun loadPrefs() {
        b.switchAutoBackup.isChecked = prefs.getBoolean("backup_enabled", true)
        val interval = prefs.getInt("backup_interval_days", 1)
        when (interval) {
            7 -> b.rgFrequency.check(b.rbWeekly.id)
            30 -> b.rgFrequency.check(b.rbMonthly.id)
            else -> b.rgFrequency.check(b.rbDaily.id)
        }
        val lastTs = prefs.getLong("last_backup_ts", 0L)
        b.tvLastBackup.text = if (lastTs == 0L) "Never"
        else SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(lastTs))

        // Backup path
        b.tvBackupPath.text = XmlBackupManager.getBackupPath(this)

        updateFrequencyEnabled()
    }

    private fun setupListeners() {
        b.switchAutoBackup.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("backup_enabled", checked).apply()
            updateFrequencyEnabled()
            (application as MyApp).scheduleBackupIfEnabled()
        }

        b.rgFrequency.setOnCheckedChangeListener { _, checkedId ->
            val days = when (checkedId) {
                b.rbWeekly.id -> 7
                b.rbMonthly.id -> 30
                else -> 1
            }
            prefs.edit().putInt("backup_interval_days", days).apply()
            (application as MyApp).scheduleBackupIfEnabled()
        }

        b.btnBackupNow.setOnClickListener { runBackup() }

        // Editable backup path
        b.rowBackupPath.setOnClickListener { showEditBackupPathDialog() }

        // Import
        b.btnImport.setOnClickListener { showImportDialog() }
        b.rowImport.setOnClickListener { showImportDialog() }

        b.rowReminderLogs.setOnClickListener {
            startActivity(Intent(this, LogsActivity::class.java))
        }

        b.btnClearData.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Clear all data?")
                .setMessage("This will permanently delete every person and transaction. This action cannot be undone.")
                .setPositiveButton("Delete Everything") { _, _ -> clearData() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun updateFrequencyEnabled() {
        val en = b.switchAutoBackup.isChecked
        b.rgFrequency.isEnabled = en
        b.rbDaily.isEnabled = en
        b.rbWeekly.isEnabled = en
        b.rbMonthly.isEnabled = en
    }

    // ── Backup ──────────────────────────────────────────────────────────────

    private fun runBackup() {
        lifecycleScope.launch {
            try {
                val path = withContext(Dispatchers.IO) {
                    XmlBackupManager.backup(applicationContext, repo)
                }
                prefs.edit().putLong("last_backup_ts", System.currentTimeMillis()).apply()
                b.tvLastBackup.text = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                    .format(Date())
                Toast.makeText(this@SettingsActivity, "Backup saved to $path", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ── Edit Backup Path ────────────────────────────────────────────────────

    private fun showEditBackupPathDialog() {
        val currentPath = XmlBackupManager.getBackupPath(this)
        val editText = EditText(this).apply {
            setText(currentPath)
            inputType = InputType.TYPE_CLASS_TEXT
            setTextColor(getColor(com.offlineledger.R.color.primary_text))
        }
        val container = FrameLayout(this).apply {
            val dp16 = (16 * resources.displayMetrics.density).toInt()
            setPadding(dp16, dp16, dp16, 0)
            addView(editText)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Backup Location")
            .setMessage("Enter the full folder path for backups:")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newPath = editText.text?.toString()?.trim() ?: ""
                if (newPath.isNotBlank()) {
                    XmlBackupManager.setBackupPath(this, newPath)
                    b.tvBackupPath.text = newPath
                    Toast.makeText(this, "Backup path updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Reset Default") { _, _ ->
                prefs.edit().remove("backup_path").apply()
                b.tvBackupPath.text = XmlBackupManager.getBackupPath(this)
                Toast.makeText(this, "Reset to default path", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Import ──────────────────────────────────────────────────────────────

    private fun showImportDialog() {
        lifecycleScope.launch {
            val backups = withContext(Dispatchers.IO) {
                XmlBackupManager.listBackups(applicationContext)
            }

            if (backups.isEmpty()) {
                Toast.makeText(
                    this@SettingsActivity,
                    "No backup files found in ${XmlBackupManager.getBackupPath(applicationContext)}",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            val names = backups.map { it.name }.toTypedArray()
            MaterialAlertDialogBuilder(this@SettingsActivity)
                .setTitle("Select Backup to Import")
                .setItems(names) { _, which ->
                    confirmImport(backups[which].absolutePath, names[which])
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun confirmImport(filePath: String, fileName: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Import $fileName?")
            .setMessage("This will ADD the data from this backup. Existing data will NOT be deleted. Duplicate entries may be created.")
            .setPositiveButton("Import") { _, _ -> runImport(filePath) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun runImport(filePath: String) {
        lifecycleScope.launch {
            try {
                val count = withContext(Dispatchers.IO) {
                    XmlBackupManager.restore(applicationContext, repo, filePath)
                }
                Toast.makeText(
                    this@SettingsActivity,
                    "Imported $count person(s) successfully!",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@SettingsActivity,
                    "Import failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ── Clear Data ──────────────────────────────────────────────────────────

    private fun clearData() {
        lifecycleScope.launch {
            try {
                val db = (application as MyApp).database
                withContext(Dispatchers.IO) { db.clearAllTables() }
                Toast.makeText(this@SettingsActivity, "All data cleared.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }
}
