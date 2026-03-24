package com.offlineledger.ui.home

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.offlineledger.R
import com.offlineledger.data.model.PersonWithBalance
import com.offlineledger.databinding.ItemPersonBinding
import com.offlineledger.utils.formatCurrency
import kotlin.math.abs

class PersonAdapter(
    private val onItemClick: (PersonWithBalance) -> Unit,
    private val onItemLongClick: (PersonWithBalance) -> Unit
) : ListAdapter<PersonWithBalance, PersonAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemPersonBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(pwb: PersonWithBalance) {
            val name = pwb.person.name
            b.tvName.text = name
            b.tvInitials.text = initials(name)

            val bal = pwb.balance
            b.tvBalance.text = (if (bal >= 0) "+" else "−") + formatCurrency(abs(bal))
            b.tvBalanceLabel.text = if (bal >= 0) "to receive" else "you owe"

            val posColor = b.root.context.getColor(R.color.color_positive)
            val negColor = b.root.context.getColor(R.color.color_negative)
            val posAvatarBg = b.root.context.getColor(R.color.avatar_positive_bg)
            val negAvatarBg = b.root.context.getColor(R.color.avatar_negative_bg)

            val isPositive = bal >= 0
            b.tvBalance.setTextColor(if (isPositive) posColor else negColor)
            b.tvBalanceLabel.setTextColor(
                b.root.context.getColor(
                    if (isPositive) R.color.color_positive_muted else R.color.color_negative_muted
                )
            )
            b.tvInitials.setTextColor(if (isPositive) posColor else negColor)
            b.avatarContainer.setBackgroundColor(if (isPositive) posAvatarBg else negAvatarBg)

            // ── Blacklist styling (colors only, no size changes) ──
            if (pwb.person.isBlacklisted) {
                b.root.setBackgroundColor(b.root.context.getColor(R.color.blacklist_bg))

                val blacklistTextColor = b.root.context.getColor(R.color.blacklist_text)
                b.tvName.paintFlags = b.tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                b.tvName.setTextColor(blacklistTextColor)
                b.tvInitials.setTextColor(blacklistTextColor)
                b.tvBalance.setTextColor(blacklistTextColor)
                b.tvBalanceLabel.setTextColor(blacklistTextColor)

                b.avatarContainer.setBackgroundColor(b.root.context.getColor(R.color.blacklist_bg))
                b.tvBlacklistBadge.visibility = android.view.View.VISIBLE
            } else {
                b.root.setBackgroundColor(b.root.context.getColor(R.color.bg_card))
                b.tvName.paintFlags = b.tvName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                b.tvName.setTextColor(b.root.context.getColor(R.color.primary_text))
                b.tvBlacklistBadge.visibility = android.view.View.GONE
            }

            b.root.setOnClickListener { onItemClick(pwb) }
            b.root.setOnLongClickListener { onItemLongClick(pwb); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PersonWithBalance>() {
            override fun areItemsTheSame(a: PersonWithBalance, b: PersonWithBalance) =
                a.person.id == b.person.id

            override fun areContentsTheSame(a: PersonWithBalance, b: PersonWithBalance) =
                a == b
        }

        fun initials(name: String): String {
            val words = name.trim().split(" ").filter { it.isNotBlank() }
            return when {
                words.size >= 2 -> "${words[0][0]}${words[1][0]}".uppercase()
                words.size == 1 && words[0].length >= 2 -> words[0].take(2).uppercase()
                words.size == 1 -> words[0].take(1).uppercase()
                else -> "?"
            }
        }
    }
}
