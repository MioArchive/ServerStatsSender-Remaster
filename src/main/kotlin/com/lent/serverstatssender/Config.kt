package com.lent.serverstatssender

data class Config(
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
            Config(
                getString("chanid", "")!!,
                getString("token", "")!!,
                getInt("time"),
                getBoolean("repeat", false),
                getBoolean("embed", false),
                getBoolean("embedCmd", false),
                getString("embedTitle", "")!!,
                getString("imageURL", "")!!,
                getString("cmdNameForDiscord", "")!!,
            )
        }
    }
}