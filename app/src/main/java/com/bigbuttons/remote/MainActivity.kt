package com.bigbuttons.remote

import android.app.Activity
import android.content.ColorStateList
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val buttonColors = intArrayOf(
        Color.rgb(30, 96, 145),
        Color.rgb(88, 66, 155),
        Color.rgb(35, 118, 92),
        Color.rgb(159, 76, 58),
        Color.rgb(123, 88, 35),
        Color.rgb(72, 77, 86),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::contentRoot.isInitialized) {
            render()
        }
    }

    private lateinit var contentRoot: LinearLayout

    private fun render() {
        val config = AppPrefs.global(this)

        if (config.keepScreenAwake) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        contentRoot = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(10, 12, 15))
        }
        applySystemBarPadding(contentRoot)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "BIG BUTTONS"
            setTextColor(Color.WHITE)
            textSize = 22f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(
            title,
            LinearLayout.LayoutParams(0, dp(48), 1f),
        )

        val edit = Button(this).apply {
            text = "EDIT"
            textSize = 14f
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(Color.rgb(48, 53, 61))
            contentDescription = "Edit Big Buttons settings"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }
        header.addView(
            edit,
            LinearLayout.LayoutParams(dp(88), dp(48)).apply {
                marginStart = dp(8)
            },
        )

        contentRoot.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ),
        )

        val status = TextView(this).apply {
            text = "Ready"
            setTextColor(Color.rgb(185, 191, 201))
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(8))
        }
        contentRoot.addView(
            status,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ),
        )

        val landscape =
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val columns = if (landscape) 3 else 2
        val rows = if (landscape) 2 else 3

        val grid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        contentRoot.addView(
            grid,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f,
            ),
        )

        val buttons = AppPrefs.buttons(this)

        repeat(rows) { rowIndex ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            grid.addView(
                row,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f,
                ),
            )

            repeat(columns) { columnIndex ->
                val index = rowIndex * columns + columnIndex
                if (index >= buttons.size) return@repeat

                val buttonConfig = buttons[index]
                val bigButton = Button(this).apply {
                    text = buttonConfig.label
                    textSize = if (landscape) 20f else 22f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, Typeface.BOLD)
                    gravity = Gravity.CENTER
                    isAllCaps = false
                    backgroundTintList = ColorStateList.valueOf(buttonColors[index])
                    contentDescription =
                        "${buttonConfig.label}. Sends command ${buttonConfig.command}"

                    setOnClickListener {
                        sendCommand(index, buttonConfig, config, status)
                    }
                }

                row.addView(
                    bigButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f,
                    ).apply {
                        val gap = dp(5)
                        setMargins(gap, gap, gap, gap)
                    },
                )
            }
        }

        setContentView(contentRoot)
    }

    private fun sendCommand(
        index: Int,
        button: BigButtonConfig,
        global: GlobalConfig,
        status: TextView,
    ) {
        val action = global.action.trim()
        val extraKey = global.extraKey.trim()
        val targetPackage = global.targetPackage.trim()
        val command = button.command.trim()

        if (action.isBlank() || extraKey.isBlank() || command.isBlank()) {
            Toast.makeText(
                this,
                "This button is not fully configured.",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        val intent = Intent(action).apply {
            if (targetPackage.isNotBlank()) {
                setPackage(targetPackage)
            }
            putExtra(extraKey, command)
            putExtra("source", "bigbuttons")
            putExtra("button_index", index)
            putExtra("button_label", button.label)
        }

        try {
            sendBroadcast(intent)
            if (global.vibration) vibrateBriefly()

            status.text = "Sent: $command"
            Toast.makeText(this, "Sent: $command", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            status.text = "Could not send command"
            Toast.makeText(
                this,
                "Could not send: ${e.message ?: e.javaClass.simpleName}",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun vibrateBriefly() {
        val vibrator: Vibrator? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as? Vibrator
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    55,
                    VibrationEffect.DEFAULT_AMPLITUDE,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(55)
        }
    }
}
