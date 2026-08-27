package com.bigbuttons.remote

import android.app.Activity
import android.content.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : Activity() {

    private lateinit var actionField: EditText
    private lateinit var extraKeyField: EditText
    private lateinit var packageField: EditText
    private lateinit var vibrationSwitch: Switch
    private lateinit var awakeSwitch: Switch
    private val labelFields = mutableListOf<EditText>()
    private val commandFields = mutableListOf<EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    private fun render() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(10, 12, 15))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        applySystemBarPadding(root, horizontalDp = 16, verticalDp = 12)

        scroll.addView(
            root,
            ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT,
            ),
        )

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(this).apply {
            text = "Edit Big Buttons"
            makeSectionTitle()
            textSize = 23f
        }
        top.addView(title, LinearLayout.LayoutParams(0, dp(52), 1f))

        val done = Button(this).apply {
            text = "SAVE"
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(Color.rgb(35, 118, 92))
            setOnClickListener { saveAndFinish() }
        }
        top.addView(done, LinearLayout.LayoutParams(dp(96), dp(48)))
        root.addView(top)

        root.addView(space(8))

        root.addView(sectionTitle("Automate connection"))

        val global = AppPrefs.global(this)
        actionField = field("Broadcast action", global.action)
        extraKeyField = field("Command extra key", global.extraKey)
        packageField = field(
            "Target package (blank = any app)",
            global.targetPackage,
        )

        root.addView(actionField)
        root.addView(extraKeyField)
        root.addView(packageField)

        vibrationSwitch = switchRow(
            "Vibrate when a command is sent",
            global.vibration,
        )
        awakeSwitch = switchRow(
            "Keep driving screen awake",
            global.keepScreenAwake,
        )

        root.addView(vibrationSwitch)
        root.addView(awakeSwitch)

        val note = TextView(this).apply {
            text =
                "Default Automate setup: action com.bigbuttons.COMMAND, " +
                "extra key command, package com.llamalab.automate."
            setTextColor(Color.rgb(171, 178, 190))
            textSize = 13f
            setPadding(0, dp(6), 0, dp(14))
        }
        root.addView(note)

        root.addView(sectionTitle("Buttons"))

        labelFields.clear()
        commandFields.clear()

        AppPrefs.buttons(this).forEachIndexed { index, config ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(10), dp(12), dp(12))
                backgroundTintList = null
                setBackgroundColor(Color.rgb(25, 29, 35))
            }

            val cardTitle = TextView(this).apply {
                text = "Button ${index + 1}"
                setTextColor(Color.WHITE)
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
            }
            card.addView(cardTitle)

            val label = field("Label", config.label)
            val command = field("Command", config.command)
            labelFields += label
            commandFields += command

            card.addView(label)
            card.addView(command)

            root.addView(
                card,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply {
                    bottomMargin = dp(10)
                },
            )
        }

        val reset = Button(this).apply {
            text = "RESET TO DEFAULTS"
            isAllCaps = false
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(Color.rgb(112, 49, 49))
            setOnClickListener {
                AppPrefs.reset(this@SettingsActivity)
                Toast.makeText(
                    this@SettingsActivity,
                    "Defaults restored",
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
        val action = actionField.text.toString().trim()
        val key = extraKeyField.text.toString().trim()

        if (action.isBlank()) {
            actionField.error = "Broadcast action is required"
            return
        }
        if (key.isBlank()) {
            extraKeyField.error = "Extra key is required"
            return
        }

        AppPrefs.saveGlobal(
            this,
            GlobalConfig(
                action = action,
                extraKey = key,
                targetPackage = packageField.text.toString().trim(),
                vibration = vibrationSwitch.isChecked,
                keepScreenAwake = awakeSwitch.isChecked,
            ),
        )

        labelFields.indices.forEach { index ->
            val label = labelFields[index].text.toString().trim()
            val command = commandFields[index].text.toString().trim()

            AppPrefs.saveButton(
                this,
                index,
                BigButtonConfig(
                    label = label.ifBlank { "BUTTON ${index + 1}" },
                    command = command,
                ),
            )
        }

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun sectionTitle(text: String): TextView =
        TextView(this).apply {
            this.text = text
            makeSectionTitle()
            setPadding(0, dp(8), 0, dp(8))
        }

    private fun field(hint: String, value: String): EditText =
        EditText(this).apply {
            this.hint = hint
            setText(value)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(135, 143, 155))
            textSize = 16f
            inputType =
                InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setSingleLine(true)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            backgroundTintList = ColorStateList.valueOf(Color.rgb(155, 164, 180))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58),
            ).apply {
                bottomMargin = dp(6)
            }
        }

    private fun switchRow(label: String, checked: Boolean): Switch =
        Switch(this).apply {
            text = label
            isChecked = checked
            setTextColor(Color.WHITE)
            textSize = 16f
            setPadding(0, dp(4), 0, dp(4))
        }

    private fun space(heightDp: Int) =
        TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(heightDp),
            )
        }
}
