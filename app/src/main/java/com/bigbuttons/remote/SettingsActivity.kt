package com.bigbuttons.remote

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : Activity() {

    private var modeIndex: Int = 0

    private lateinit var nameField: EditText
    private lateinit var actionField: EditText
    private lateinit var extraKeyField: EditText
    private lateinit var packageField: EditText

    private lateinit var startupEnabledSwitch: Switch
    private lateinit var wakeTargetSwitch: Switch
    private lateinit var startupDelayField: EditText
    private lateinit var startupCommandField: EditText
    private lateinit var returnSwitch: Switch

    private lateinit var vibrationSwitch: Switch
    private lateinit var awakeSwitch: Switch

    private val labelFields = mutableListOf<EditText>()
    private val commandFields = mutableListOf<EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPrefs.ensureMigrated(this)
        modeIndex = AppPrefs.activeModeIndex(this)
        render()
    }

    private fun render() {
        val mode = AppPrefs.mode(this, modeIndex)
        val global = AppPrefs.global(this)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(AppColors.Background)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        applySystemBarPadding(root, horizontalDp = 16, verticalDp = 12)

        scroll.addView(
            root,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ),
        )

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val eyebrow = TextView(this).apply {
            text = "EDIT MODE"
            setTextColor(AppColors.Muted)
            textSize = 11f
            letterSpacing = 0.08f
            setTypeface(typeface, Typeface.BOLD)
        }
        titleBlock.addView(eyebrow)

        val title = TextView(this).apply {
            text = mode.name
            setTextColor(AppColors.Text)
            textSize = 23f
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 1
        }
        titleBlock.addView(title)

        top.addView(
            titleBlock,
            LinearLayout.LayoutParams(0, dp(58), 1f),
        )

        val save = Button(this).apply {
            text = "SAVE"
            setTextColor(AppColors.Text)
            setTypeface(typeface, Typeface.BOLD)
            isAllCaps = false
            background = rippleBackground(
                fillColor = AppColors.AccentDark,
                radiusDp = 14,
                strokeColor = AppColors.Accent,
            )
            setOnClickListener { saveAndFinish() }
        }
        top.addView(
            save,
            LinearLayout.LayoutParams(dp(92), dp(46)),
        )

        root.addView(top)
        root.addView(space(10))

        root.addView(sectionTitle("Mode"))
        root.addView(
            sectionCard().apply {
                nameField = field("Mode name", mode.name)
                addView(nameField)
            },
        )

        root.addView(sectionTitle("Receiver"))
        root.addView(
            sectionCard().apply {
                actionField = field(
                    "Broadcast action",
                    mode.broadcastAction,
                )
                extraKeyField = field(
                    "Command extra key",
                    mode.extraKey,
                )
                packageField = field(
                    "Target package",
                    mode.targetPackage,
                )

                addView(actionField)
                addView(extraKeyField)
                addView(packageField)

                addView(
                    helperText(
                        "Default target: com.llamalab.automate. " +
                            "Leave the package blank to send an untargeted broadcast.",
                    ),
                )
            },
        )

        root.addView(sectionTitle("Startup automation"))
        root.addView(
            sectionCard().apply {
                startupEnabledSwitch = switchRow(
                    "Run startup when this mode opens",
                    mode.startupEnabled,
                )
                wakeTargetSwitch = switchRow(
                    "Open target app before startup command",
                    mode.wakeTarget,
                )
                startupDelayField = numberField(
                    "Delay after opening target (ms)",
                    mode.startupDelayMs.toString(),
                )
                startupCommandField = field(
                    "Startup command",
                    mode.startupCommand,
                )
                returnSwitch = switchRow(
                    "Return to BigButtons after opening target",
                    mode.returnToBigButtons,
                )

                addView(startupEnabledSwitch)
                addView(wakeTargetSwitch)
                addView(startupDelayField)
                addView(startupCommandField)
                addView(returnSwitch)

                addView(
                    helperText(
                        "Example: Solo Driving can open Automate, wait 900 ms, " +
                            "then send command = solo_start.",
                    ),
                )
            },
        )

        root.addView(sectionTitle("Driving screen"))
        root.addView(
            sectionCard().apply {
                vibrationSwitch = switchRow(
                    "Vibrate when commands are sent",
                    global.vibration,
                )
                awakeSwitch = switchRow(
                    "Keep the screen awake",
                    global.keepScreenAwake,
                )
                addView(vibrationSwitch)
                addView(awakeSwitch)
            },
        )

        root.addView(sectionTitle("Buttons"))

        labelFields.clear()
        commandFields.clear()

        mode.buttons.forEachIndexed { index, config ->
            val card = sectionCard()

            val cardTitle = TextView(this).apply {
                text = "Button ${index + 1}"
                setTextColor(AppColors.Text)
                textSize = 15f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, 0, 0, dp(5))
            }
            card.addView(cardTitle)

            val label = field("Label", config.label)
            val command = field("Command", config.command)
            labelFields += label
            commandFields += command

            card.addView(label)
            card.addView(command)

            root.addView(card)
        }

        val reset = Button(this).apply {
            text = "RESET THIS MODE"
            isAllCaps = false
            setTextColor(AppColors.Text)
            setTypeface(typeface, Typeface.BOLD)
            background = rippleBackground(
                fillColor = AppColors.Danger,
                radiusDp = 16,
            )
            setOnClickListener {
                AppPrefs.resetMode(this@SettingsActivity, modeIndex)
                Toast.makeText(
                    this@SettingsActivity,
                    "Mode reset",
                    Toast.LENGTH_SHORT,
                ).show()
                render()
            }
        }

        root.addView(
            reset,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52),
            ).apply {
                topMargin = dp(4)
                bottomMargin = dp(18)
            },
        )

        setContentView(scroll)
    }

    private fun saveAndFinish() {
        val name = nameField.text.toString().trim()
        val action = actionField.text.toString().trim()
        val extraKey = extraKeyField.text.toString().trim()
        val startupDelay =
            startupDelayField.text.toString().trim().toIntOrNull()
                ?.coerceIn(0, 5000)
                ?: 900

        if (name.isBlank()) {
            nameField.error = "Mode name is required"
            return
        }
        if (action.isBlank()) {
            actionField.error = "Broadcast action is required"
            return
        }
        if (extraKey.isBlank()) {
            extraKeyField.error = "Extra key is required"
            return
        }

        val buttons = labelFields.indices.map { index ->
            BigButtonConfig(
                label = labelFields[index].text.toString().trim()
                    .ifBlank { "Button ${index + 1}" },
                command = commandFields[index].text.toString().trim(),
            )
        }

        AppPrefs.saveMode(
            this,
            modeIndex,
            ModeConfig(
                name = name,
                broadcastAction = action,
                extraKey = extraKey,
                targetPackage = packageField.text.toString().trim(),
                startupEnabled = startupEnabledSwitch.isChecked,
                wakeTarget = wakeTargetSwitch.isChecked,
                startupDelayMs = startupDelay,
                startupCommand =
                    startupCommandField.text.toString().trim(),
                returnToBigButtons = returnSwitch.isChecked,
                buttons = buttons,
            ),
        )

        AppPrefs.saveGlobal(
            this,
            GlobalConfig(
                vibration = vibrationSwitch.isChecked,
                keepScreenAwake = awakeSwitch.isChecked,
            ),
        )

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun sectionTitle(text: String): TextView =
        TextView(this).apply {
            this.text = text
            makeSectionTitle()
            setPadding(0, dp(12), 0, dp(7))
        }

    private fun sectionCard(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = roundedBackground(
                fillColor = AppColors.Surface,
                radiusDp = 18,
                strokeColor = AppColors.Border,
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                bottomMargin = dp(10)
            }
        }

    private fun field(hint: String, value: String): EditText =
        EditText(this).apply {
            this.hint = hint
            setText(value)
            setTextColor(AppColors.Text)
            setHintTextColor(AppColors.Muted)
            textSize = 16f
            inputType =
                InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setSingleLine(true)
            setPadding(dp(10), dp(7), dp(10), dp(7))
            backgroundTintList = ColorStateList.valueOf(AppColors.Muted)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56),
            ).apply {
                bottomMargin = dp(5)
            }
        }

    private fun numberField(hint: String, value: String): EditText =
        field(hint, value).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }

    private fun switchRow(label: String, checked: Boolean): Switch =
        Switch(this).apply {
            text = label
            isChecked = checked
            setTextColor(AppColors.Text)
            textSize = 15f
            setPadding(0, dp(5), 0, dp(5))
        }

    private fun helperText(text: String): TextView =
        TextView(this).apply {
            this.text = text
            setTextColor(AppColors.Muted)
            textSize = 13f
            setPadding(0, dp(6), 0, 0)
        }

    private fun space(heightDp: Int): TextView =
        TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(heightDp),
            )
        }
}
