package com.bigbuttons.remote

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class ModePickerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPrefs.ensureMigrated(this)
        render()
    }

    private fun render() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(AppColors.Background)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        applySystemBarPadding(root, horizontalDp = 16, verticalDp = 14)

        scroll.addView(
            root,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ),
        )

        val title = TextView(this).apply {
            text = "Modes"
            setTextColor(AppColors.Text)
            textSize = 28f
            setTypeface(typeface, Typeface.BOLD)
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "Choose the button layout and startup automation you want to use."
            setTextColor(AppColors.Muted)
            textSize = 15f
            setPadding(0, dp(4), 0, dp(18))
        }
        root.addView(subtitle)

        val active = AppPrefs.activeModeIndex(this)

        repeat(AppPrefs.MODE_COUNT) { index ->
            val mode = AppPrefs.mode(this, index)

            val card = Button(this).apply {
                text =
                    if (index == active) {
                        "${mode.name}\nACTIVE MODE"
                    } else {
                        "${mode.name}\n6 BUTTONS"
                    }
                gravity = Gravity.CENTER_VERTICAL
                textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                textSize = 18f
                setTextColor(AppColors.Text)
                setTypeface(typeface, Typeface.BOLD)
                isAllCaps = false
                minHeight = 0
                minWidth = 0
                setPadding(dp(18), dp(14), dp(18), dp(14))
                background = rippleBackground(
                    fillColor =
                        if (index == active) AppColors.AccentDark
                        else AppColors.SurfaceRaised,
                    radiusDp = 20,
                    strokeColor =
                        if (index == active) AppColors.Accent
                        else AppColors.Border,
                )
                setOnClickListener {
                    AppPrefs.setActiveModeIndex(this@ModePickerActivity, index)
                    finish()
                }
            }

            root.addView(
                card,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(92),
                ).apply {
                    bottomMargin = dp(12)
                },
            )
        }

        val back = Button(this).apply {
            text = "BACK"
            textSize = 14f
            setTextColor(AppColors.Text)
            setTypeface(typeface, Typeface.BOLD)
            isAllCaps = false
            background = rippleBackground(
                fillColor = AppColors.Surface,
                radiusDp = 16,
                strokeColor = AppColors.Border,
            )
            setOnClickListener { finish() }
        }

        root.addView(
            back,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52),
            ).apply {
                topMargin = dp(4)
                bottomMargin = dp(12)
            },
        )

        setContentView(scroll)
    }
}
