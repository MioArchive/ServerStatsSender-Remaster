package com.lent.serverstatssender.manager

import com.lent.serverstatssender.Main.Companion.plugin
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ActivateToken: CommandExecutor {

    private fun CommandSender.send(msg: String) = sendMessage(msg).let { true }

    // /sssactivate <token> <channelID>
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("sss.token") && !sender.hasPermission("sss.access")) return true
        if (args.size != 2) return sender.send("/sssactivate <token> <channelId>")

        val (token, channelId) = args

        // TODO: Verify that bot token and channel look as expected
        if (token.length != 72 && channelId.length != 19) sender.send("Error occured.\n/sssactivate <token> <channelId>")

        plugin.updateConfig {
            set("token", token)
            set("chanid", channelId)
        }

        plugin.activateBot(token)

        return sender.send("Successfully activated the bot!")
    }
}