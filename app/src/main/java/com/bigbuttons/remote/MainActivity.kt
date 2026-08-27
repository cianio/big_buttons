package com.bigbuttons.remote

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private var firstResume = true
    private var renderedModeIndex = -1

    private lateinit var root: LinearLayout
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPrefs.ensureMigrated(this)
    }

    override fun onResume() {
        super.onResume()

        val modeIndex = AppPrefs.activeModeIndex(this)
        val modeChanged = modeIndex != renderedModeIndex

        render(modeIndex)

        if (firstResume || modeChanged) {
            firstResume = false
            renderedModeIndex = modeIndex
            runModeStartup(modeIndex, AppPrefs.mode(this, modeIndex))
        } else {
            renderedModeIndex = modeIndex
        }
    }

    private fun render(modeIndex: Int) {
        val global = AppPrefs.global(this)
        val mode = AppPrefs.mode(this, modeIndex)

        if (global.keepScreenAwake) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppColors.Background)
        }
        applySystemBarPadding(root, horizontalDp = 12, verticalDp = 10)

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val modeBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val eyebrow = TextView(this).apply {
            text = "ACTIVE MODE"
            setTextColor(AppColors.Muted)
            textSize = 11f
            letterSpacing = 0.08f
            setTypeface(typeface, Typeface.BOLD)
        }
        modeBlock.addView(eyebrow)

        val modeTitle = TextView(this).apply {
            text = mode.name
            setTextColor(AppColors.Text)
            textSize = 23f
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 1
        }
        modeBlock.addView(modeTitle)

        topBar.addView(
            modeBlock,
            LinearLayout.LayoutParams(0, dp(58), 1f),
        )

        val modesButton = smallTopButton("MODES") {
            startActivity(Intent(this, ModePickerActivity::class.java))
        }
        topBar.addView(
            modesButton,
            LinearLayout.LayoutParams(dp(84), dp(44)).apply {
                marginStart = dp(6)
            },
        )

        val editButton = smallTopButton("EDIT") {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        topBar.addView(
            editButton,
            LinearLayout.LayoutParams(dp(72), dp(44)).apply {
                marginStart = dp(6)
            },
        )

        root.addView(topBar)

        statusText = TextView(this).apply {
            text =
                if (mode.startupEnabled) "Startup automation enabled"
                else "Ready"
            setTextColor(AppColors.Muted)
            textSize = 13f
            gravity = Gravity.START
            setPadding(dp(2), dp(4), 0, dp(8))
        }
        root.addView(
            statusText,
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
        root.addView(
            grid,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f,
            ),
        )

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
                val buttonIndex = rowIndex * columns + columnIndex
                val config = mode.buttons[buttonIndex]

                val bigButton = Button(this).apply {
                    text = config.label
                    textSize = if (landscape) 18f else 20f
                    setTextColor(AppColors.Text)
                    setTypeface(typeface, Typeface.BOLD)
                    gravity = Gravity.CENTER
                    isAllCaps = false
                    minHeight = 0
                    minWidth = 0
                    background = rippleBackground(
                        fillColor = AppColors.SurfaceRaised,
                        radiusDp = 20,
                        strokeColor = AppColors.Border,
                    )
                    contentDescription =
                        "${config.label}. Sends ${config.command}"

                    setOnClickListener {
                        sendButtonCommand(
                            modeIndex = modeIndex,
                            mode = mode,
                            buttonIndex = buttonIndex,
                            config = config,
                        )
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

        setContentView(root)
    }

    private fun smallTopButton(
        label: String,
        action: () -> Unit,
    ): Button =
        Button(this).apply {
            text = label
            textSize = 12f
            setTextColor(AppColors.Text)
            setTypeface(typeface, Typeface.BOLD)
            isAllCaps = false
            minHeight = 0
            minWidth = 0
            background = rippleBackground(
                fillColor = AppColors.Surface,
                radiusDp = 14,
                strokeColor = AppColors.Border,
            )
            setOnClickListener { action() }
        }

    private fun sendButtonCommand(
        modeIndex: Int,
        mode: ModeConfig,
        buttonIndex: Int,
        config: BigButtonConfig,
    ) {
        try {
            CommandDispatcher.send(
                context = this,
                modeIndex = modeIndex,
                mode = mode,
                command = config.command,
                eventType = "button",
                buttonIndex = buttonIndex,
                buttonLabel = config.label,
            )

            if (AppPrefs.global(this).vibration) {
                vibrateBriefly()
            }

            statusText.text = "Sent: ${config.command}"
        } catch (e: Exception) {
            statusText.text = "Command failed"
            Toast.makeText(
                this,
                e.message ?: "Could not send command",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun runModeStartup(
        modeIndex: Int,
        mode: ModeConfig,
    ) {
        if (!mode.startupEnabled) return

        val command = mode.startupCommand.trim()
        if (command.isBlank()) {
            statusText.text = "Startup command is blank"
            return
        }

        statusText.text = "Starting ${mode.name}"

        if (mode.wakeTarget && mode.targetPackage.isNotBlank()) {
            launchTargetPackage(mode.targetPackage.trim())
        }

        handler.postDelayed(
            {
                try {
                    CommandDispatcher.send(
                        context = applicationContext,
                        modeIndex = modeIndex,
                        mode = mode,
                        command = command,
                        eventType = "startup",
                    )

                    if (AppPrefs.global(this).vibration) {
                        vibrateBriefly()
                    }

                    statusText.text = "Startup sent: $command"

                    if (mode.wakeTarget && mode.returnToBigButtons) {
                        bringBigButtonsToFront()
                    }
                } catch (e: Exception) {
                    statusText.text = "Startup command failed"
                    Toast.makeText(
                        applicationContext,
                        e.message ?: "Could not send startup command",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            },
            mode.startupDelayMs.toLong(),
        )
    }

    private fun launchTargetPackage(packageName: String) {
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(launchIntent)
        } catch (_: ActivityNotFoundException) {
            statusText.text = "Target app could not be opened"
            Toast.makeText(
                this,
                "Could not open $packageName",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun bringBigButtonsToFront() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
            )
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            // The startup broadcast has already been sent; returning to the
            // app is a convenience only.
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
                    45,
                    VibrationEffect.DEFAULT_AMPLITUDE,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(45)
        }
    }
}
