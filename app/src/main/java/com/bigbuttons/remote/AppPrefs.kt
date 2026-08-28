package com.bigbuttons.remote

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class ButtonActionType(val stored: String) {
    COMMAND("command"), PAGE("page"), BACK("back");
    companion object {
        fun fromStored(value: String?): ButtonActionType =
            entries.firstOrNull { it.stored == value } ?: COMMAND
    }
}

data class BigButtonConfig(val label: String, val actionType: ButtonActionType, val value: String)
data class PageConfig(val id: String, val name: String, val buttons: List<BigButtonConfig>)
data class GlobalConfig(val vibration: Boolean, val keepScreenAwake: Boolean)

data class ModeConfig(
    val name: String,
    val useAutomateIntegration: Boolean,
    val broadcastAction: String,
    val extraKey: String,
    val targetPackage: String,
    val startupEnabled: Boolean,
    val wakeTarget: Boolean,
    val startupDelayMs: Int,
    val startupCommand: String,
    val returnToBigButtons: Boolean,
    val pages: List<PageConfig>,
) {
    fun effectiveAction() = if (useAutomateIntegration) AppPrefs.DEFAULT_ACTION else broadcastAction
    fun effectiveExtraKey() = if (useAutomateIntegration) AppPrefs.DEFAULT_EXTRA_KEY else extraKey
    fun effectiveTargetPackage() = if (useAutomateIntegration) AppPrefs.DEFAULT_TARGET_PACKAGE else targetPackage
}

object AppPrefs {
    private const val PREFS = "big_buttons"
    private const val SCHEMA_VERSION = 3
    const val MODE_COUNT = 3
    const val MAIN_PAGE_ID = "main"
    const val DEFAULT_ACTION = "com.bigbuttons.COMMAND"
    const val DEFAULT_EXTRA_KEY = "command"
    const val DEFAULT_TARGET_PACKAGE = "com.llamalab.automate"

    private fun command(label: String, value: String) = BigButtonConfig(label, ButtonActionType.COMMAND, value)
    private fun mainPage(buttons: List<BigButtonConfig>) = PageConfig(MAIN_PAGE_ID, "Main", buttons)

    private val defaultModes = listOf(
        ModeConfig("Solo Driving", true, DEFAULT_ACTION, DEFAULT_EXTRA_KEY, DEFAULT_TARGET_PACKAGE, false, true, 900, "solo_start", true,
            listOf(mainPage(listOf(command("Driving Playlist","driving_playlist"),command("Favourites","favourites"),command("Navigation","navigation"),command("Call","call"),command("Home","home"),command("Pause","pause"))))),
        ModeConfig("Family", true, DEFAULT_ACTION, DEFAULT_EXTRA_KEY, DEFAULT_TARGET_PACKAGE, false, true, 900, "family_start", true,
            listOf(mainPage(listOf(command("Family Playlist","family_playlist"),command("Kids Music","kids_music"),command("Navigation","navigation"),command("Volume Down","volume_down"),command("Home","home"),command("Pause","pause"))))),
        ModeConfig("Work", true, DEFAULT_ACTION, DEFAULT_EXTRA_KEY, DEFAULT_TARGET_PACKAGE, false, true, 900, "work_start", true,
            listOf(mainPage(listOf(command("Work Playlist","work_playlist"),command("Navigation","navigation"),command("Next Stop","next_stop"),command("Call","call"),command("Home","home"),command("Pause","pause")))))
    )

    private fun normalizeButtons(buttons: List<BigButtonConfig>): List<BigButtonConfig> {
        val out = buttons.take(6).toMutableList()
        while (out.size < 6) out += BigButtonConfig("", ButtonActionType.COMMAND, "")
        return out
    }

    fun ensureMigrated(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        var version = p.getInt("schema_version", 1)
        if (version < 2) { migrateV1ToV2(p); version = 2 }
        if (version < 3) { migrateV2ToV3(p); version = 3 }
        if (version != SCHEMA_VERSION) p.edit().putInt("schema_version", SCHEMA_VERSION).apply()
    }

    private fun migrateV1ToV2(p: android.content.SharedPreferences) {
        val editor = p.edit()
        val hasV1Data = p.contains("action") || p.contains("target_package") || p.contains("button_0_label")
        if (hasV1Data) {
            val d = defaultModes[0]
            editor.putString("mode_0_name", d.name)
                .putString("mode_0_action", p.getString("action", d.broadcastAction) ?: d.broadcastAction)
                .putString("mode_0_extra_key", p.getString("extra_key", d.extraKey) ?: d.extraKey)
                .putString("mode_0_target_package", p.getString("target_package", d.targetPackage) ?: d.targetPackage)
                .putBoolean("mode_0_startup_enabled", true).putBoolean("mode_0_wake_target", true)
                .putInt("mode_0_startup_delay_ms", 900).putString("mode_0_startup_command", "solo_start")
                .putBoolean("mode_0_return_to_bigbuttons", true)
                .putBoolean("global_vibration", p.getBoolean("vibration", true))
                .putBoolean("global_keep_awake", p.getBoolean("keep_screen_awake", true))
            d.pages.first().buttons.indices.forEach { i ->
                editor.putString("mode_0_button_${i}_label", p.getString("button_${i}_label", d.pages.first().buttons[i].label) ?: d.pages.first().buttons[i].label)
                    .putString("mode_0_button_${i}_command", p.getString("button_${i}_command", d.pages.first().buttons[i].value) ?: d.pages.first().buttons[i].value)
            }
        }
        editor.putInt("schema_version", 2).apply()
    }

    private fun migrateV2ToV3(p: android.content.SharedPreferences) {
        val editor = p.edit()
        repeat(MODE_COUNT) { modeIndex ->
            val d = defaultModes[modeIndex]
            val prefix = "mode_${modeIndex}_"
            if (!p.contains("${prefix}pages_json")) {
                val old = d.pages.first().buttons.indices.map { i ->
                    BigButtonConfig(
                        p.getString("${prefix}button_${i}_label", d.pages.first().buttons[i].label) ?: d.pages.first().buttons[i].label,
                        ButtonActionType.COMMAND,
                        p.getString("${prefix}button_${i}_command", d.pages.first().buttons[i].value) ?: d.pages.first().buttons[i].value
                    )
                }
                editor.putString("${prefix}pages_json", pagesToJson(listOf(PageConfig(MAIN_PAGE_ID, "Main", old))))
            }
            val action = p.getString("${prefix}action", d.broadcastAction) ?: d.broadcastAction
            val key = p.getString("${prefix}extra_key", d.extraKey) ?: d.extraKey
            val target = p.getString("${prefix}target_package", d.targetPackage) ?: d.targetPackage
            editor.putBoolean("${prefix}use_automate", action == DEFAULT_ACTION && key == DEFAULT_EXTRA_KEY && target == DEFAULT_TARGET_PACKAGE)
        }
        editor.putInt("schema_version", 3).apply()
    }

    fun global(context: Context): GlobalConfig {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return GlobalConfig(p.getBoolean("global_vibration", p.getBoolean("vibration", true)), p.getBoolean("global_keep_awake", p.getBoolean("keep_screen_awake", true)))
    }
    fun saveGlobal(context: Context, c: GlobalConfig) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("global_vibration",c.vibration).putBoolean("global_keep_awake",c.keepScreenAwake).apply() }
    fun activeModeIndex(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("active_mode",0).coerceIn(0,MODE_COUNT-1)
    fun setActiveModeIndex(context: Context, index: Int) { require(index in 0 until MODE_COUNT); context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putInt("active_mode",index).apply() }

    fun mode(context: Context, index: Int): ModeConfig {
        require(index in 0 until MODE_COUNT)
        val p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE); val d=defaultModes[index]; val prefix="mode_${index}_"
        return ModeConfig(
            p.getString("${prefix}name",d.name) ?: d.name,
            p.getBoolean("${prefix}use_automate",d.useAutomateIntegration),
            p.getString("${prefix}action",d.broadcastAction) ?: d.broadcastAction,
            p.getString("${prefix}extra_key",d.extraKey) ?: d.extraKey,
            p.getString("${prefix}target_package",d.targetPackage) ?: d.targetPackage,
            p.getBoolean("${prefix}startup_enabled",d.startupEnabled),
            p.getBoolean("${prefix}wake_target",d.wakeTarget),
            p.getInt("${prefix}startup_delay_ms",d.startupDelayMs).coerceIn(0,5000),
            p.getString("${prefix}startup_command",d.startupCommand) ?: d.startupCommand,
            p.getBoolean("${prefix}return_to_bigbuttons",d.returnToBigButtons),
            readPages(p.getString("${prefix}pages_json",null),d.pages)
        )
    }

    fun saveMode(context: Context, index: Int, m: ModeConfig) {
        require(index in 0 until MODE_COUNT); val prefix="mode_${index}_"
        context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
            .putString("${prefix}name",m.name).putBoolean("${prefix}use_automate",m.useAutomateIntegration)
            .putString("${prefix}action",m.broadcastAction).putString("${prefix}extra_key",m.extraKey).putString("${prefix}target_package",m.targetPackage)
            .putBoolean("${prefix}startup_enabled",m.startupEnabled).putBoolean("${prefix}wake_target",m.wakeTarget)
            .putInt("${prefix}startup_delay_ms",m.startupDelayMs.coerceIn(0,5000)).putString("${prefix}startup_command",m.startupCommand)
            .putBoolean("${prefix}return_to_bigbuttons",m.returnToBigButtons).putString("${prefix}pages_json",pagesToJson(m.pages)).apply()
    }

    fun pages(context: Context, modeIndex: Int) = mode(context,modeIndex).pages
    fun savePages(context: Context, modeIndex: Int, pages: List<PageConfig>) { context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString("mode_${modeIndex}_pages_json",pagesToJson(pages)).apply() }
    fun addPage(context: Context, modeIndex: Int, name: String): PageConfig {
        val page=PageConfig(UUID.randomUUID().toString(),name.trim().ifBlank{"New Page"},normalizeButtons(emptyList()))
        savePages(context,modeIndex,pages(context,modeIndex)+page); return page
    }
    fun deletePage(context: Context, modeIndex: Int, pageId: String) {
        if(pageId==MAIN_PAGE_ID) return
        val updated=pages(context,modeIndex).filterNot{it.id==pageId}.map { page -> page.copy(buttons=page.buttons.map { b -> if(b.actionType==ButtonActionType.PAGE && b.value==pageId) BigButtonConfig(b.label,ButtonActionType.COMMAND,"") else b }) }
        savePages(context,modeIndex,updated)
    }
    fun resetMode(context: Context, index: Int) { val p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE); val prefix="mode_${index}_"; val e=p.edit(); p.all.keys.filter{it.startsWith(prefix)}.forEach{e.remove(it)}; e.apply() }

    private fun pagesToJson(pages: List<PageConfig>): String {
        val arr=JSONArray(); pages.forEach { page -> val btns=JSONArray(); normalizeButtons(page.buttons).forEach { b -> btns.put(JSONObject().put("label",b.label).put("type",b.actionType.stored).put("value",b.value)) }; arr.put(JSONObject().put("id",page.id).put("name",page.name).put("buttons",btns)) }; return arr.toString()
    }
    private fun readPages(raw:String?, fallback:List<PageConfig>):List<PageConfig> {
        if(raw.isNullOrBlank()) return fallback
        return try { val arr=JSONArray(raw); val pages=mutableListOf<PageConfig>(); for(i in 0 until arr.length()){ val o=arr.getJSONObject(i); val ba=o.optJSONArray("buttons")?:JSONArray(); val bs=mutableListOf<BigButtonConfig>(); for(j in 0 until ba.length()){ val b=ba.getJSONObject(j); bs+=BigButtonConfig(b.optString("label",""),ButtonActionType.fromStored(b.optString("type","command")),b.optString("value","")) }; pages+=PageConfig(o.optString("id",if(i==0)MAIN_PAGE_ID else UUID.randomUUID().toString()),o.optString("name",if(i==0)"Main" else "Page"),normalizeButtons(bs)) }; if(pages.none{it.id==MAIN_PAGE_ID}) fallback else pages } catch(_:Exception){ fallback }
    }
}
