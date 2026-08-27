package com.bigbuttons.remote

import android.content.Context
import android.content.Intent

object CommandDispatcher {
    fun send(
        context: Context,
        modeIndex: Int,
        mode: ModeConfig,
        command: String,
        eventType: String,
        buttonIndex: Int? = null,
        buttonLabel: String? = null,
    ) {
        val action = mode.broadcastAction.trim()
        val extraKey = mode.extraKey.trim()
        val targetPackage = mode.targetPackage.trim()
        val cleanCommand = command.trim()

        require(action.isNotBlank()) { "Broadcast action is blank" }
        require(extraKey.isNotBlank()) { "Command extra key is blank" }
        require(cleanCommand.isNotBlank()) { "Command is blank" }

        val intent = Intent(action).apply {
            if (targetPackage.isNotBlank()) {
                setPackage(targetPackage)
            }

            putExtra(extraKey, cleanCommand)
            putExtra("source", "bigbuttons")
            putExtra("event_type", eventType)
            putExtra("mode_index", modeIndex)
            putExtra("mode_name", mode.name)

            if (buttonIndex != null) {
                putExtra("button_index", buttonIndex)
            }
            if (buttonLabel != null) {
                putExtra("button_label", buttonLabel)
            }
        }

        context.sendBroadcast(intent)
    }
}
