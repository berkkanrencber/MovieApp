package com.berkkanrencber.movieapp.ui.customui

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.berkkanrencber.movieapp.databinding.DialogInternetBinding

class NoInternetDialog : DialogFragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: DialogInternetBinding? = null
    private val binding get() = _binding!!

    private var retryCallback: (() -> Unit)? = null

    fun setRetryCallback(callback: () -> Unit) {
        retryCallback = callback
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogInternetBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        binding.btnRetry.setOnClickListener {
            retryCallback?.invoke()
            dismiss()
        }
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
