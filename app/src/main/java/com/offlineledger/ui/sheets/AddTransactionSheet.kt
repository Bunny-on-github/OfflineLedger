package com.offlineledger.ui.sheets

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.offlineledger.data.model.Transaction
import com.offlineledger.databinding.SheetAddTransactionBinding
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionSheet : BottomSheetDialogFragment() {

    fun interface OnTransactionConfirmListener {
        fun onTransactionConfirmed(amount: Double, label: String, details: String, timestamp: Long)
    }

    var listener: OnTransactionConfirmListener? = null

    private var _b: SheetAddTransactionBinding? = null
    private val b get() = _b!!

    private var isIncoming = true
    private val cal = Calendar.getInstance()

    private val dateFmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())

    override fun onCreateView(inf: LayoutInflater, vg: ViewGroup?, state: Bundle?): View {
        _b = SheetAddTransactionBinding.inflate(inf, vg, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        // Pre-fill if editing
        arguments?.let { args ->
            if (args.containsKey(ARG_EXISTING)) {
                @Suppress("DEPRECATION")
                val existing = args.getParcelable<Transaction>(ARG_EXISTING)
                existing?.let { t ->
                    isIncoming = t.amount >= 0
                    b.etAmount.setText(String.format("%.2f", Math.abs(t.amount)))
                    b.etLabel.setText(t.label)
                    b.etDetails.setText(t.details)
                    cal.timeInMillis = t.timestamp
                    b.tvTitle.text = "Edit Transaction"
                    b.btnSave.text = "Update Transaction"
                }
            }
        }

        updateDirectionUi()
        updateDateTimeUi()

        b.btnIncoming.setOnClickListener { isIncoming = true; updateDirectionUi() }
        b.btnOutgoing.setOnClickListener { isIncoming = false; updateDirectionUi() }

        b.tvDate.setOnClickListener { pickDate() }
        b.tvTime.setOnClickListener { pickTime() }

        b.btnSave.setOnClickListener { save() }
        b.btnCancel.setOnClickListener { dismiss() }
    }

    private fun updateDirectionUi() {
        val ctx = requireContext()
        if (isIncoming) {
            b.btnIncoming.setBackgroundColor(ctx.getColor(android.R.color.transparent))
            b.btnIncoming.setTextColor(ctx.getColor(com.offlineledger.R.color.color_positive))
            b.btnOutgoing.setBackgroundColor(ctx.getColor(android.R.color.transparent))
            b.btnOutgoing.setTextColor(ctx.getColor(com.offlineledger.R.color.hint_text))
        } else {
            b.btnOutgoing.setTextColor(ctx.getColor(com.offlineledger.R.color.color_negative))
            b.btnIncoming.setTextColor(ctx.getColor(com.offlineledger.R.color.hint_text))
        }
        val amtColor = if (isIncoming) com.offlineledger.R.color.color_positive
        else com.offlineledger.R.color.color_negative
        b.etAmount.setTextColor(ctx.getColor(amtColor))
    }

    private fun updateDateTimeUi() {
        b.tvDate.text = dateFmt.format(cal.time)
        b.tvTime.text = timeFmt.format(cal.time)
    }

    private fun pickDate() {
        DatePickerDialog(
            requireContext(),
            { _, y, m, d -> cal.set(y, m, d); updateDateTimeUi() },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun pickTime() {
        TimePickerDialog(
            requireContext(),
            { _, h, min -> cal.set(Calendar.HOUR_OF_DAY, h); cal.set(Calendar.MINUTE, min); updateDateTimeUi() },
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false
        ).show()
    }

    private fun save() {
        val raw = b.etAmount.text?.toString()?.trim() ?: ""
        if (raw.isBlank()) { b.etAmount.error = "Amount is required"; return }
        val abs = raw.toDoubleOrNull()
        if (abs == null || abs <= 0) { b.etAmount.error = "Enter a valid amount"; return }

        val amount = if (isIncoming) abs else -abs
        val label = b.etLabel.text?.toString() ?: ""
        val details = b.etDetails.text?.toString() ?: ""
        listener?.onTransactionConfirmed(amount, label, details, cal.timeInMillis)
        dismiss()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }

    companion object {
        private const val ARG_EXISTING = "arg_existing"

        fun newInstance(existing: Transaction? = null): AddTransactionSheet {
            return AddTransactionSheet().apply {
                arguments = Bundle().apply {
                    existing?.let { putParcelable(ARG_EXISTING, it) }
                }
            }
        }
    }
}
