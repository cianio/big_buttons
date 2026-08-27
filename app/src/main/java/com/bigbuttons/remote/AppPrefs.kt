package com.bigbuttons.remote

import android.content.Context

data class BigButtonConfig(
    val label: String,
    val command: String,
)

data class GlobalConfig(
    val vibration: Boolean,
    val keepScreenAwake: Boolean,
)

data class ModeConfig(
    val name: String,
    val broadcastAction: String,
    val extraKey: String,
    val targetPackage: String,
    val startupEnabled: Boolean,
    val wakeTarget: Boolean,
    val startupDelayMs: Int,
    val startupCommand: String,
    val returnToBigButtons: Boolean,
    val buttons: List<BigButtonConfig>,
)

object AppPrefs {
    private const val PREFS = "big_buttons"
    private const val SCHEMA_VERSION = 2

    const val MODE_COUNT = 3
    const val DEFAULT_ACTION = "com.bigbuttons.COMMAND"
    const val DEFAULT_EXTRA_KEY = "command"
    const val DEFAULT_TARGET_PACKAGE = "com.llamalab.automate"

    private val defaultModes = listOf(
        ModeConfig(
            name = "Solo Driving",
            broadcastAction = DEFAULT_ACTION,
            extraKey = DEFAULT_EXTRA_KEY,
            targetPackage = DEFAULT_TARGET_PACKAGE,
            startupEnabled = false,
            wakeTarget = true,
            startupDelayMs = 900,
            startupCommand = "solo_start",
            returnToBigButtons = true,
            buttons = listOf(
                BigButtonConfig("Driving Playlist", "driving_playlist"),
                BigButtonConfig("Favourites", "favourites"),
                BigButtonConfig("Navigation", "navigation"),
                BigButtonConfig("Call", "call"),
                BigButtonConfig("Home", "home"),
                BigButtonConfig("Pause", "pause"),
            ),
        ),
        ModeConfig(
            name = "Family",
            broadcastAction = DEFAULT_ACTION,
            extraKey = DEFAULT_EXTRA_KEY,
            targetPackage = DEFAULT_TARGET_PACKAGE,
            startupEnabled = false,
            wakeTarget = true,
            startupDelayMs = 900,
            startupCommand = "family_start",
            returnToBigButtons = true,
            buttons = listOf(
                BigButtonConfig("Family Playlist", "family_playlist"),
                BigButtonConfig("Kids Music", "kids_music"),
                BigButtonConfig("Navigation", "navigation"),
                BigButtonConfig("Volume Down", "volume_down"),
                BigButtonConfig("Home", "home"),
                BigButtonConfig("Pause", "pause"),
            ),
        ),
        ModeConfig(
            name = "Work",
            broadcastAction = DEFAULT_ACTION,
            extraKey = DEFAULT_EXTRA_KEY,
            targetPackage = DEFAULT_TARGET_PACKAGE,
            startupEnabled = false,
            wakeTarget = true,
            startupDelayMs = 900,
            startupCommand = "work_start",
            returnToBigButtons = true,
            buttons = listOf(
                BigButtonConfig("Work Playlist", "work_playlist"),
                BigButtonConfig("Navigation", "navigation"),
                BigButtonConfig("Next Stop", "next_stop"),
                BigButtonConfig("Call", "call"),
                BigButtonConfig("Home", "home"),
                BigButtonConfig("Pause", "pause"),
            ),
        ),
    )

    fun ensureMigrated(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (p.getInt("schema_version", 1) >= SCHEMA_VERSION) return

        val editor = p.edit()
        val hasV1Data =
            p.contains("action") ||
            p.contains("target_package") ||
            p.contains("button_0_label")

        if (hasV1Data) {
            val default = defaultModes[0]

            editor
                .putString(
                    "mode_0_name",
                    default.name,
                )
                .putString(
                    "mode_0_action",
                    p.getString("action", default.broadcastAction)
                        ?: default.broadcastAction,
                )
                .putString(
                    "mode_0_extra_key",
                    p.getString("extra_key", default.extraKey)
                        ?: default.extraKey,
                )
                .putString(
                    "mode_0_target_package",
                    p.getString("target_package", default.targetPackage)
                        ?: default.targetPackage,
                )
                .putBoolean("mode_0_startup_enabled", true)
                .putBoolean("mode_0_wake_target", true)
                .putInt("mode_0_startup_delay_ms", 900)
                .putString("mode_0_startup_command", "solo_start")
                .putBoolean("mode_0_return_to_bigbuttons", true)
                .putBoolean(
                    "global_vibration",
                    p.getBoolean("vibration", true),
                )
                .putBoolean(
                    "global_keep_awake",
                    p.getBoolean("keep_screen_awake", true),
                )

            default.buttons.indices.forEach { index ->
                editor
                    .putString(
                        "mode_0_button_${index}_label",
                        p.getString(
                            "button_${index}_label",
                            default.buttons[index].label,
                        ) ?: default.buttons[index].label,
                    )
                    .putString(
                        "mode_0_button_${index}_command",
                        p.getString(
                            "button_${index}_command",
                            default.buttons[index].command,
                        ) ?: default.buttons[index].command,
                    )
            }
        }

        editor
            .putInt("schema_version", SCHEMA_VERSION)
            .apply()
    }

    fun global(context: Context): GlobalConfig {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return GlobalConfig(
            vibration = p.getBoolean(
                "global_vibration",
                p.getBoolean("vibration", true),
            ),
            keepScreenAwake = p.getBoolean(
                "global_keep_awake",
                p.getBoolean("keep_screen_awake", true),
            ),
        )
    }

    fun saveGlobal(context: Context, config: GlobalConfig) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("global_vibration", config.vibration)
            .putBoolean("global_keep_awake", config.keepScreenAwake)
            .apply()
    }

    fun activeModeIndex(context: Context): Int {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return p.getInt("active_mode", 0).coerceIn(0, MODE_COUNT - 1)
    }

    fun setActiveModeIndex(context: Context, index: Int) {
        require(index in 0 until MODE_COUNT)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt("active_mode", index)
            .apply()
    }

    fun mode(context: Context, index: Int): ModeConfig {
        require(index in 0 until MODE_COUNT)
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val default = defaultModes[index]
        val prefix = "mode_${index}_"

        val buttons = default.buttons.indices.map { buttonIndex ->
            BigButtonConfig(
                label = p.getString(
                    "${prefix}button_${buttonIndex}_label",
                    default.buttons[buttonIndex].label,
                ) ?: default.buttons[buttonIndex].label,
                command = p.getString(
                    "${prefix}button_${buttonIndex}_command",
                    default.buttons[buttonIndex].command,
                ) ?: default.buttons[buttonIndex].command,
            )
        }

        return ModeConfig(
            name = p.getString("${prefix}name", default.name) ?: default.name,
            broadcastAction =
                p.getString("${prefix}action", default.broadcastAction)
                    ?: default.broadcastAction,
            extraKey =
                p.getString("${prefix}extra_key", default.extraKey)
                    ?: default.extraKey,
            targetPackage =
                p.getString("${prefix}target_package", default.targetPackage)
                    ?: default.targetPackage,
            startupEnabled =
                p.getBoolean("${prefix}startup_enabled", default.startupEnabled),
            wakeTarget =
                p.getBoolean("${prefix}wake_target", default.wakeTarget),
            startupDelayMs =
                p.getInt("${prefix}startup_delay_ms", default.startupDelayMs)
                    .coerceIn(0, 5000),
            startupCommand =
                p.getString("${prefix}startup_command", default.startupCommand)
                    ?: default.startupCommand,
            returnToBigButtons =
                p.getBoolean(
                    "${prefix}return_to_bigbuttons",
                    default.returnToBigButtons,
                ),
            buttons = buttons,
        )
    }

    fun saveMode(context: Context, index: Int, mode: ModeConfig) {
        require(index in 0 until MODE_COUNT)
        val prefix = "mode_${index}_"
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("${prefix}name", mode.name)
            .putString("${prefix}action", mode.broadcastAction)
            .putString("${prefix}extra_key", mode.extraKey)
            .putString("${prefix}target_package", mode.targetPackage)
            .putBoolean("${prefix}startup_enabled", mode.startupEnabled)
            .putBoolean("${prefix}wake_target", mode.wakeTarget)
            .putInt(
                "${prefix}startup_delay_ms",
                mode.startupDelayMs.coerceIn(0, 5000),
            )
            .putString("${prefix}startup_command", mode.startupCommand)
            .putBoolean(
                "${prefix}return_to_bigbuttons",
                mode.returnToBigButtons,
            )

        mode.buttons.take(6).forEachIndexed { buttonIndex, button ->
            editor
                .putString(
                    "${prefix}button_${buttonIndex}_label",
                    button.label,
                )
                .putString(
                    "${prefix}button_${buttonIndex}_command",
                    button.command,
                )
        }

        editor.apply()
    }

    fun resetMode(context: Context, index: Int) {
        require(index in 0 until MODE_COUNT)
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val prefix = "mode_${index}_"
        val keys = p.all.keys.filter { it.startsWith(prefix) }
        val editor = p.edit()
        keys.forEach(editor::remove)
        editor.apply()
    }
}
