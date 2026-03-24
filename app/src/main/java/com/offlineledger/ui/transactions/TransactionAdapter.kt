package com.offlineledger.ui.transactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.offlineledger.R
import com.offlineledger.data.model.Transaction
import com.offlineledger.databinding.ItemDateHeaderBinding
import com.offlineledger.databinding.ItemTransactionBinding
import com.offlineledger.utils.formatCurrency
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/** Flat list item — either a date header or a transaction entry. */
sealed class TransactionListItem {
    data class Header(val dateLabel: String, val dayTotal: Double) : TransactionListItem()
    data class Entry(val transaction: Transaction) : TransactionListItem()
}

fun List<Transaction>.toGroupedItems(): List<TransactionListItem> {
    val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val grouped = groupBy { fmt.format(Date(it.timestamp)) }
    val result = mutableListOf<TransactionListItem>()
    for ((date, txns) in grouped) {
        result += TransactionListItem.Header(date, txns.sumOf { it.amount })
        txns.forEach { result += TransactionListItem.Entry(it) }
    }
    return result
}

class TransactionAdapter(
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : ListAdapter<TransactionListItem, RecyclerView.ViewHolder>(DIFF) {

    // ── View Holders ────────────────────────────────────────────────────────

    inner class HeaderVH(private val b: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(h: TransactionListItem.Header) {
            b.tvDate.text = h.dateLabel
            val total = h.dayTotal
            b.tvDayTotal.text = (if (total >= 0) "+" else "−") + formatCurrency(abs(total))
            b.tvDayTotal.setTextColor(
                b.root.context.getColor(
                    if (total >= 0) R.color.color_positive_muted else R.color.color_negative_muted
                )
            )
        }
    }

    inner class EntryVH(private val b: ItemTransactionBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(e: TransactionListItem.Entry) {
            val t = e.transaction
            val isIn = t.amount >= 0
            val ctx = b.root.context

            b.tvLabel.text = t.label.ifBlank { if (isIn) "Received" else "Paid" }
            b.tvAmount.text = (if (isIn) "+" else "−") + formatCurrency(abs(t.amount))
            b.tvAmount.setTextColor(
                ctx.getColor(if (isIn) R.color.color_positive else R.color.color_negative)
            )

            val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
            b.tvTime.text = timeFmt.format(Date(t.timestamp))

            b.tvDetails.text = t.details
            b.tvDetails.visibility =
                if (t.details.isBlank()) android.view.View.GONE else android.view.View.VISIBLE

            b.ivDirection.setImageResource(
                if (isIn) R.drawable.ic_arrow_incoming else R.drawable.ic_arrow_outgoing
            )
            b.avatarContainer.setBackgroundColor(
                ctx.getColor(
                    if (isIn) R.color.avatar_positive_bg else R.color.avatar_negative_bg
                )
            )

            b.btnEdit.setOnClickListener { onEdit(t) }
            b.root.setOnLongClickListener {
                onDelete(t)
                true
            }
        }
    }

    // ── Adapter overrides ───────────────────────────────────────────────────

    override fun getItemViewType(position: Int) =
        if (getItem(position) is TransactionListItem.Header) TYPE_HEADER else TYPE_ENTRY

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER)
            HeaderVH(ItemDateHeaderBinding.inflate(inf, parent, false))
        else
            EntryVH(ItemTransactionBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TransactionListItem.Header -> (holder as HeaderVH).bind(item)
            is TransactionListItem.Entry -> (holder as EntryVH).bind(item)
        }
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ENTRY = 1

        private val DIFF = object : DiffUtil.ItemCallback<TransactionListItem>() {
            override fun areItemsTheSame(a: TransactionListItem, b: TransactionListItem): Boolean {
                return when {
                    a is TransactionListItem.Header && b is TransactionListItem.Header ->
                        a.dateLabel == b.dateLabel
                    a is TransactionListItem.Entry && b is TransactionListItem.Entry ->
                        a.transaction.id == b.transaction.id
                    else -> false
                }
            }

            override fun areContentsTheSame(a: TransactionListItem, b: TransactionListItem) =
                a == b
        }
    }
}
