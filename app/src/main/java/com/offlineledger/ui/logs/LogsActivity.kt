package com.offlineledger.ui.logs

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.offlineledger.MyApp
import com.offlineledger.databinding.ActivityLogsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LogsActivity : AppCompatActivity() {

    private lateinit var b: ActivityLogsBinding
    private lateinit var adapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Reminder Logs"
        }

        adapter = LogAdapter()
        b.recyclerView.adapter = adapter

        val repo = (application as MyApp).repository
        lifecycleScope.launch {
            repo.getAllReminderLogs().collectLatest { logs ->
                adapter.submitList(logs)
                b.tvEmpty.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
                b.recyclerView.visibility = if (logs.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
