package com.app.base.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.app.base.R

class DialogSaveFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Chặn bấm back để đóng
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.fragment_dialog_save, null)
        dialog.setContentView(view)

        // Chặn bấm ra ngoài để đóng
        dialog.setCanceledOnTouchOutside(false)

        // Lấy button trong layout
        val btnNo = view.findViewById<Button>(R.id.btnNo)
        val btnYes = view.findViewById<Button>(R.id.btnYes)
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
        // Gắn sự kiện        val btnYes = view.findViewById<Button>(R.id.btnYes)
        btnNo.setOnClickListener {
            parentFragmentManager.setFragmentResult("dialog_save_request", bundleOf("result" to "NO"))
            dismiss()
        }

        btnYes.setOnClickListener {
            parentFragmentManager.setFragmentResult("dialog_save_request", bundleOf("result" to "YES"))
            dismiss()
        }
        btnClose.setOnClickListener {
            dismiss()
        }
        return dialog
    }

    companion object {
        fun newInstance(): DialogSaveFragment = DialogSaveFragment()
    }
}
