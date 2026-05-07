package com.offlineledger.ui.home

import android.content.Intent
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import android.widget.EditText
import android.widget.TextView
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                MaterialAlertDialogBuilder(this)
                    .setTitle(pwb.person.name)
                    .setItems(arrayOf("🧾 $blacklistLabel", "✏️ Edit Person", "🗑️ Delete Person")) { _, which ->
                        when (which) {
                            0 -> vm.toggleBlacklist(pwb)
                            1 -> authenticateThen(this, "Edit Person") { showEditPersonDialog(pwb) }
                            2 -> {
                                MaterialAlertDialogBuilder(this)
                                    .setTitle("Delete ${pwb.person.name}?")
                                    .setMessage("All transactions for this person will be permanently deleted.")
                                    .setPositiveButton("Delete") { _, _ ->
                                        authenticateThen(this, "Delete Person") { vm.deletePerson(pwb) }
                                    }
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

        b.btnSortToggle.setOnClickListener { vm.toggleSortMode() }

        // Summary tiles replace receive/owe filter chips
        b.tileYouWillReceive.setOnClickListener {
            vm.setFilter(if (vm.filter.value == 1) 0 else 1)
        }
        b.tileYouOwe.setOnClickListener {
            vm.setFilter(if (vm.filter.value == 2) 0 else 2)
        }

        b.chipAll.setOnClickListener { vm.setFilter(0) }
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
                updateFilterTileState(
                    tile = b.tileYouWillReceive,
                    title = b.tileYouWillReceive.getChildAt(0) as TextView,
                    amount = b.tileYouWillReceive.getChildAt(1) as TextView,
                    selected = f == 1
                )
                updateFilterTileState(
                    tile = b.tileYouOwe,
                    title = b.tileYouOwe.getChildAt(0) as TextView,
                    amount = b.tileYouOwe.getChildAt(1) as TextView,
                    selected = f == 2
                )
            }
        }

        lifecycleScope.launch {
            vm.hideZeroBalance.collectLatest { hide ->
                b.chipHideZero.isChecked = hide
            }
        }

        lifecycleScope.launch {
            vm.sortByAmount.collectLatest { byAmount ->
                b.btnSortToggle.text = if (byAmount) "Sort: Amount" else "Sort: A-Z"
            }
        }
    }


    private fun updateFilterTileState(
        tile: LinearLayout,
        title: TextView,
        amount: TextView,
        selected: Boolean
    ) {
        val startColor = ((tile.tag as? Int) ?: ContextCompat.getColor(this, R.color.bg_card))
        val endColor = ContextCompat.getColor(
            this,
            if (selected) R.color.chip_selected_bg else R.color.bg_card
        )

        ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor).apply {
            duration = 180
            addUpdateListener { animator ->
                val color = animator.animatedValue as Int
                tile.setBackgroundColor(color)
                tile.tag = color
            }
            start()
        }

        tile.animate()
            .scaleX(if (selected) 1.03f else 1f)
            .scaleY(if (selected) 1.03f else 1f)
            .translationZ(if (selected) 16f else 0f)
            .setDuration(180)
            .start()

        title.setTextColor(ContextCompat.getColor(this, if (selected) R.color.primary_text else R.color.hint_text))
        amount.alpha = if (selected) 1f else 0.92f
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView
        searchView?.queryHint = "Search people"
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                vm.setSearchQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                vm.setSearchQuery(newText.orEmpty())
                return true
            }
        })
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                vm.setSearchQuery("")
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showEditPersonDialog(pwb: com.offlineledger.data.model.PersonWithBalance) {
        val name = EditText(this).apply {
            hint = "Name"
            setText(pwb.person.name)
        }
        val mobile = EditText(this).apply {
            hint = "Mobile"
            inputType = InputType.TYPE_CLASS_PHONE
            setText(pwb.person.mobileNumber)
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, 0)
            addView(name)
            addView(mobile)
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Person")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newName = name.text?.toString()?.trim().orEmpty()
                val newMobile = mobile.text?.toString()?.trim().orEmpty()
                if (newName.isNotBlank()) vm.updatePerson(pwb, newName, newMobile)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun authenticateThen(context: HomeActivity, action: String, onSuccess: () -> Unit) {
        val pass = SimpleDateFormat("HHmm", Locale.getDefault()).format(Date()).reversed()
        val input = EditText(context).apply { inputType = InputType.TYPE_CLASS_NUMBER }
        MaterialAlertDialogBuilder(context)
            .setTitle("$action authentication")
            .setMessage("Enter passcode.") // reverse of current 24h time
            .setView(input)
            .setPositiveButton("Verify") { _, _ ->
                if (input.text?.toString() == pass) onSuccess()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
