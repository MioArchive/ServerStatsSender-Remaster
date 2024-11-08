package com.lent.serverstatssender.manager

import com.lent.serverstatssender.Main

data class ConfigManager(
    val chanid: String,
    val token: String,
    val timeIntervalTicks: Int,
    val repeat: Boolean,
    val embed: Boolean,
    val embedCmd: Boolean,
    val embedTitle: String,
    val imageURL: String,
    val cmdNameForDiscord: String,
) {

    companion object {
        fun load(plugin: Main) = with(plugin.config) {
            ConfigManager(
                getString("chanid", "")!!,
                getString("token", "")!!,
                getInt("time"),
                getBoolean("repeat", false),
                getBoolean("embed", false),
                getBoolean("embedCmd", false),
                getString("embedTitle", "")!!,
                getString("imgURL", "")!!,
                getString("cmdNameForDiscord", "")!!,
            )
        }
    }
}