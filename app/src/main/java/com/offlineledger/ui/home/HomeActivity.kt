package com.offlineledger.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.offlineledger.MyApp
import com.offlineledger.R
import com.offlineledger.databinding.ActivityHomeBinding
import com.offlineledger.ui.settings.SettingsActivity
import com.offlineledger.ui.sheets.AddPersonSheet
import com.offlineledger.ui.transactions.TransactionActivity
import com.offlineledger.utils.formatCurrency
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

class HomeActivity : AppCompatActivity() {

    private lateinit var b: ActivityHomeBinding
    private val vm: HomeViewModel by viewModels {
        HomeViewModelFactory((application as MyApp).repository)
    }
    private lateinit var adapter: PersonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.title = ""

        adapter = PersonAdapter(
            onItemClick = { pwb ->
                startActivity(
                    Intent(this, TransactionActivity::class.java)
                        .putExtra(TransactionActivity.EXTRA_PERSON, pwb.person)
                )
            },
            onItemLongClick = { pwb ->
                val blacklistLabel = if (pwb.person.isBlacklisted) "Remove from Blacklist" else "Blacklist"
                MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_OfflineLedger_OptionMenuDialog)
                    .setTitle(pwb.person.name)
                    .setItems(arrayOf("🧾 $blacklistLabel", "🗑️ Delete Person")) { _, which ->
                        when (which) {
                            0 -> vm.toggleBlacklist(pwb)
                            1 -> {
                                MaterialAlertDialogBuilder(this)
                                    .setTitle("Delete ${pwb.person.name}?")
                                    .setMessage("All transactions for this person will be permanently deleted.")
                                    .setPositiveButton("Delete") { _, _ -> vm.deletePerson(pwb) }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                            }
                        }
                    }
                    .show()
            }
        )
        b.recyclerView.adapter = adapter

        b.fabAdd.setOnClickListener {
            AddPersonSheet.newInstance().apply {
                listener = AddPersonSheet.OnPersonAddedListener { name, mobile ->
                    vm.addPerson(name, mobile)
                }
            }.show(supportFragmentManager, "add_person")
        }

        // Filter chips
        b.chipAll.setOnClickListener { vm.setFilter(0) }
        b.chipOwed.setOnClickListener { vm.setFilter(1) }
        b.chipOwing.setOnClickListener { vm.setFilter(2) }
        b.chipHideZero.setOnClickListener {
            vm.setHideZeroBalance(b.chipHideZero.isChecked)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            vm.personsWithBalance.collectLatest { list ->
                adapter.submitList(list)
                b.tvEmpty.visibility =
                    if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }

        lifecycleScope.launch {
            vm.netBalance.collectLatest { bal ->
                val prefix = if (bal >= 0) "+" else "−"
                b.tvNetBalance.text = "$prefix${formatCurrency(abs(bal))}"
                b.tvNetBalance.setTextColor(
                    getColor(if (bal >= 0) R.color.color_positive else R.color.color_negative)
                )
            }
        }

        lifecycleScope.launch {
            vm.totalReceivable.collectLatest { b.tvReceivable.text = formatCurrency(it) }
        }

        lifecycleScope.launch {
            vm.totalOwed.collectLatest { b.tvOwed.text = formatCurrency(it) }
        }

        lifecycleScope.launch {
            vm.filter.collectLatest { f ->
                b.chipAll.isChecked = f == 0
                b.chipOwed.isChecked = f == 1
                b.chipOwing.isChecked = f == 2
            }
        }

        lifecycleScope.launch {
            vm.hideZeroBalance.collectLatest { hide ->
                b.chipHideZero.isChecked = hide
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
