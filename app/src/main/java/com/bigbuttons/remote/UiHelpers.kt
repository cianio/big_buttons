package com.bigbuttons.remote

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import kotlin.math.roundToInt

fun Activity.dp(value: Int): Int =
    (value * resources.displayMetrics.density).roundToInt()

fun Activity.applySystemBarPadding(view: View, horizontalDp: Int = 12, verticalDp: Int = 10) {
    val h = dp(horizontalDp)
    val v = dp(verticalDp)

    view.setPadding(h, v, h, v)

    view.setOnApplyWindowInsetsListener { target, insets ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bars = insets.getInsets(WindowInsets.Type.systemBars())
            target.setPadding(
                h + bars.left,
                v + bars.top,
                h + bars.right,
                v + bars.bottom,
            )
        } else {
            @Suppress("DEPRECATION")
            target.setPadding(
                h + insets.systemWindowInsetLeft,
                v + insets.systemWindowInsetTop,
                h + insets.systemWindowInsetRight,
                v + insets.systemWindowInsetBottom,
            )
        }
        insets
    }
}

fun TextView.makeSectionTitle() {
    setTextColor(Color.WHITE)
    textSize = 18f
    setTypeface(typeface, Typeface.BOLD)
}
