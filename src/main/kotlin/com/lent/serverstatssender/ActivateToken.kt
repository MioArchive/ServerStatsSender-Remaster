package com.lent.serverstatssender

import com.lent.serverstatssender.Main.Companion.plugin
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class ActivateToken: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
         lateinit var jda: JDA
        var config = plugin.config.getString("token")
        val tokenPLAYER = args[1]
        val player = sender as Player

        if (!player.hasPermission("sss.token") && !player.hasPermission("sss.acess")) return true

        if (args[0] != "activate") return true
        if (plugin.config.getString("token") == "") {
            plugin.config.set("token", tokenPLAYER)
            plugin.saveConfig()
            jda = JDABuilder.createDefault(plugin.statsConfig.token)
                .setActivity(Activity.watching("your server stats"))
                .addEventListeners(DiscordListener(Main()))
                .build()
            println("successfully started")
        }

    return true
    }
}