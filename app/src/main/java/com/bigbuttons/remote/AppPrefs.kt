package com.bigbuttons.remote

import android.content.Context

data class BigButtonConfig(
    val label: String,
    val command: String,
)

data class GlobalConfig(
    val action: String,
    val extraKey: String,
    val targetPackage: String,
    val vibration: Boolean,
    val keepScreenAwake: Boolean,
)

object AppPrefs {
    private const val PREFS = "big_buttons"

    const val DEFAULT_ACTION = "com.bigbuttons.COMMAND"
    const val DEFAULT_EXTRA_KEY = "command"
    const val DEFAULT_TARGET_PACKAGE = "com.llamalab.automate"

    private val defaultButtons = listOf(
        BigButtonConfig("🏠  HOME", "home"),
        BigButtonConfig("🎵  MUSIC", "music"),
        BigButtonConfig("🧭  NAV", "navigate"),
        BigButtonConfig("📞  CALL", "call"),
        BigButtonConfig("🚪  GATE", "gate"),
        BigButtonConfig("⭐  CUSTOM", "custom"),
    )

    fun global(context: Context): GlobalConfig {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return GlobalConfig(
            action = p.getString("action", DEFAULT_ACTION) ?: DEFAULT_ACTION,
            extraKey = p.getString("extra_key", DEFAULT_EXTRA_KEY) ?: DEFAULT_EXTRA_KEY,
            targetPackage = p.getString("target_package", DEFAULT_TARGET_PACKAGE)
                ?: DEFAULT_TARGET_PACKAGE,
            vibration = p.getBoolean("vibration", true),
            keepScreenAwake = p.getBoolean("keep_screen_awake", true),
        )
    }

    fun button(context: Context, index: Int): BigButtonConfig {
        require(index in 0..5)
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val default = defaultButtons[index]
        return BigButtonConfig(
            label = p.getString("button_${index}_label", default.label) ?: default.label,
            command = p.getString("button_${index}_command", default.command) ?: default.command,
        )
    }

    fun buttons(context: Context): List<BigButtonConfig> =
        (0..5).map { button(context, it) }

    fun saveGlobal(context: Context, config: GlobalConfig) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("action", config.action)
            .putString("extra_key", config.extraKey)
            .putString("target_package", config.targetPackage)
            .putBoolean("vibration", config.vibration)
            .putBoolean("keep_screen_awake", config.keepScreenAwake)
            .apply()
    }

    fun saveButton(context: Context, index: Int, config: BigButtonConfig) {
        require(index in 0..5)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("button_${index}_label", config.label)
            .putString("button_${index}_command", config.command)
            .apply()
    }

    fun reset(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
