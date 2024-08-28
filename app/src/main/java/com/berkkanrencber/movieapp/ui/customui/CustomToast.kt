package com.berkkanrencber.movieapp.ui.customui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.berkkanrencber.movieapp.R

class CustomToast {
    companion object {
        fun show(
            context: Context,
            message: String,
            iconResId: Int,
        ) {
            val inflater = LayoutInflater.from(context)
            val layout: View =
                inflater.inflate(
                    R.layout.custom_toast,
                    null,
                )

            val imageView: ImageView = layout.findViewById(R.id.iv_toast_icon)
            imageView.setImageResource(iconResId)
            val textView: TextView = layout.findViewById(R.id.tv_toast_text)
            textView.text = message

            with(Toast(context)) {
                duration = Toast.LENGTH_SHORT
                view = layout
                show()
            }
        }
    }
}
