package com.app.base.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.app.base.R

class DialogProfileFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // chặn bấm back
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.fragment_dialog_profile, null)
        dialog.setContentView(view)

        dialog.setCanceledOnTouchOutside(false)

        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
        val tvProfile = view.findViewById<TextView>(R.id.tvPlayer)

        btnClose.setOnClickListener { dismiss() }
//        btnOk.setOnClickListener { dismiss() }
//
//        btnEdit.setOnClickListener {
//            parentFragmentManager.setFragmentResult(
//                "dialog_profile_request",
//                bundleOf("action" to "EDIT")
//            )
//            dismiss()
//        }

        return dialog
    }

    companion object {
        fun newInstance(name: String, email: String): DialogProfileFragment {
            val fragment = DialogProfileFragment()
            fragment.arguments = bundleOf(
                "name" to name,
                "email" to email
            )
            return fragment
        }
    }
}
