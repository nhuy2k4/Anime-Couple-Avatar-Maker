package com.app.base.ui.more

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.app.base.R
import com.google.android.material.button.MaterialButton

class MoreDiamondFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.fragment_more_diamond)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val closeButton: ImageButton = dialog.findViewById(R.id.close_button)
        val btnWatch: MaterialButton = dialog.findViewById(R.id.btn_watch)

        closeButton.setOnClickListener { dismiss() }
        btnWatch.setOnClickListener {
            // Launch ad intent (replace with your ad SDK)
            Toast.makeText(requireContext(), "Show ad here", Toast.LENGTH_SHORT).show()
        }
        return dialog
    }
}
