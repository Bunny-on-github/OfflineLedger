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
import com.offlineledger.data.model.Transaction
import com.offlineledger.databinding.ActivityTransactionBinding
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
            title = vm.person.name
        }

        // ── Person info card ──
        b.tvPersonName.text = vm.person.name
        val mobile = vm.person.mobileNumber
        if (mobile.isNotBlank()) {
            b.tvMobile.text = "📱 $mobile"
            b.tvMobile.visibility = View.VISIBLE
        } else {
            b.tvMobile.visibility = View.GONE
        }

        // ── Reminder toggle ──
        b.switchReminder.isChecked = vm.person.reminderEnabled
        b.switchReminder.setOnCheckedChangeListener { _, checked ->
            vm.setReminderEnabled(checked)
        }

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
