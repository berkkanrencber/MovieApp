package com.berkkanrencber.movieapp.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

object ScreenSizeUtil {
    fun getScreenWidth(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }
}