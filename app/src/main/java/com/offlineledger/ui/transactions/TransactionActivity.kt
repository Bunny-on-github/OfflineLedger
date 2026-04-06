package com.offlineledger.ui.transactions

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.offlineledger.MyApp
import com.offlineledger.R
import com.offlineledger.data.model.Person
import com.offlineledger.data.model.ReminderFrequency
import com.offlineledger.data.model.Transaction
import com.offlineledger.databinding.ActivityTransactionBinding
import com.offlineledger.databinding.DialogReminderConfigBinding
import com.offlineledger.ui.sheets.AddTransactionSheet
import com.offlineledger.utils.formatCurrency
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

class TransactionActivity : AppCompatActivity() {

    private lateinit var b: ActivityTransactionBinding
    private lateinit var adapter: TransactionAdapter
    private val vm: TransactionViewModel by viewModels {
        val person = intent.getParcelableExtra<Person>(EXTRA_PERSON)!!
        TransactionViewModelFactory((application as MyApp).repository, person)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = vm.person.value.name
        }

        // ── Person info card ──
        val mobile = vm.person.value.mobileNumber
        if (mobile.isNotBlank()) {
            b.tvMobile.text = "📱 $mobile"
            b.tvMobile.visibility = View.VISIBLE
            b.reminderContainer.visibility = View.VISIBLE
            b.tvReminderUnavailable.visibility = View.GONE
        } else {
            b.tvMobile.visibility = View.GONE
            b.reminderContainer.visibility = View.GONE
            b.tvReminderUnavailable.visibility = View.VISIBLE
        }

        // ── Reminder toggle ──
        b.switchReminder.setOnCheckedChangeListener { _, checked ->
            vm.setReminderEnabled(checked)
        }
        b.btnConfigureReminder.setOnClickListener { showReminderConfigDialog() }

        adapter = TransactionAdapter(
            onEdit = { t -> showEditSheet(t) },
            onDelete = { t ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Delete transaction?")
                    .setMessage("This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> vm.deleteTransaction(t) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        b.recyclerView.adapter = adapter

        b.fabAddTransaction.setOnClickListener { showAddSheet() }

        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            vm.person.collectLatest { person ->
                b.tvPersonName.text = person.name
                b.switchReminder.isChecked = person.reminderEnabled
                b.tvReminderSummary.text = when (ReminderFrequency.fromValue(person.reminderFrequency)) {
                    ReminderFrequency.DAILY -> "Daily • 10:00 AM"
                    ReminderFrequency.WEEKLY -> "Weekly • Sunday 10:00 AM"
                    ReminderFrequency.TEN_DAYS -> "1st/11th/21st/31st • 10:00 AM"
                }
            }
        }

        lifecycleScope.launch {
            vm.transactions.collectLatest { list ->
                adapter.submitList(list.toGroupedItems())
                b.tvEmpty.visibility =
                    if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            vm.balance.collectLatest { bal ->
                val prefix = if (bal >= 0) "+" else "−"
                b.tvBalance.text = "$prefix${formatCurrency(abs(bal))}"
                b.tvBalance.setTextColor(
                    getColor(if (bal >= 0) R.color.color_positive else R.color.color_negative)
                )
                b.tvBalanceLabel.text = if (bal >= 0) "owes you" else "you owe"
                b.tvBalanceLabel.setTextColor(
                    getColor(if (bal >= 0) R.color.color_positive_muted else R.color.color_negative_muted)
                )
            }
        }
    }

    private fun showReminderConfigDialog() {
        val person = vm.person.value
        if (person.mobileNumber.isBlank()) return

        val dialogBinding = DialogReminderConfigBinding.inflate(layoutInflater)
        when (ReminderFrequency.fromValue(person.reminderFrequency)) {
            ReminderFrequency.DAILY -> dialogBinding.rgFrequency.check(dialogBinding.rbDaily.id)
            ReminderFrequency.WEEKLY -> dialogBinding.rgFrequency.check(dialogBinding.rbWeekly.id)
            ReminderFrequency.TEN_DAYS -> dialogBinding.rgFrequency.check(dialogBinding.rbTenDays.id)
        }
        dialogBinding.etPrefix.setText(person.reminderMessagePrefix)
        dialogBinding.etSuffix.setText(person.reminderMessageSuffix)

        MaterialAlertDialogBuilder(this)
            .setTitle("Reminder Settings")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val frequency = when (dialogBinding.rgFrequency.checkedRadioButtonId) {
                    dialogBinding.rbDaily.id -> ReminderFrequency.DAILY.value
                    dialogBinding.rbTenDays.id -> ReminderFrequency.TEN_DAYS.value
                    else -> ReminderFrequency.WEEKLY.value
                }
                vm.updateReminderConfig(
                    enabled = b.switchReminder.isChecked,
                    frequency = frequency,
                    prefix = dialogBinding.etPrefix.text?.toString()?.trim().orEmpty(),
                    suffix = dialogBinding.etSuffix.text?.toString()?.trim().orEmpty()
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddSheet() {
        AddTransactionSheet.newInstance().apply {
            listener = AddTransactionSheet.OnTransactionConfirmListener { amount, label, details, ts ->
                vm.addTransaction(amount, label, details, ts)
            }
        }.show(supportFragmentManager, "add_txn")
    }

    private fun showEditSheet(t: Transaction) {
        AddTransactionSheet.newInstance(t).apply {
            listener = AddTransactionSheet.OnTransactionConfirmListener { amount, label, details, ts ->
                vm.updateTransaction(t.copy(amount = amount, label = label, details = details, timestamp = ts))
            }
        }.show(supportFragmentManager, "edit_txn")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_PERSON = "extra_person"
    }
}
