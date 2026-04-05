package com.offlineledger.ui.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.offlineledger.databinding.SheetAddPersonBinding

class AddPersonSheet : DialogFragment() {

    fun interface OnPersonAddedListener {
        fun onPersonAdded(name: String, mobileNumber: String)
    }

    var listener: OnPersonAddedListener? = null

    private var _b: SheetAddPersonBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inf: LayoutInflater, vg: ViewGroup?, state: Bundle?): View {
        _b = SheetAddPersonBinding.inflate(inf, vg, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        // Pre-fill if editing
        arguments?.let { args ->
            val editName = args.getString(ARG_NAME)
            val editMobile = args.getString(ARG_MOBILE)
            if (!editName.isNullOrBlank()) {
                b.etName.setText(editName)
                b.etMobile.setText(editMobile ?: "")
                b.tvSheetTitle.text = "Edit Person"
                b.btnAdd.text = "Update"
            }
        }

        b.etName.requestFocus()

        b.btnAdd.setOnClickListener {
            val name = b.etName.text?.toString()?.trim() ?: ""
            if (name.isBlank()) {
                b.etName.error = "Name is required"
                return@setOnClickListener
            }
            val mobile = b.etMobile.text?.toString()?.trim() ?: ""
            listener?.onPersonAdded(name, mobile)
            dismiss()
        }

        b.btnCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        private const val ARG_NAME = "arg_name"
        private const val ARG_MOBILE = "arg_mobile"

        fun newInstance(name: String? = null, mobile: String? = null): AddPersonSheet {
            return AddPersonSheet().apply {
                arguments = Bundle().apply {
                    name?.let { putString(ARG_NAME, it) }
                    mobile?.let { putString(ARG_MOBILE, it) }
                }
            }
        }
    }
}
