package com.lent.serverstatssender

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin(), CommandExecutor {

    lateinit var jda: JDA private set
    lateinit var statsConfig: Config private set

    val isBotEnabled get() = ::jda.isInitialized

    companion object {
        lateinit var plugin: Main private set

        fun scheduleTimer(intervalTicks: Long, delayTicks: Int = 0, runnable: () -> Unit) {
            Bukkit.getScheduler().runTaskTimer(plugin, Runnable { runnable.invoke() }, delayTicks.toLong(), intervalTicks.toLong())
        }
    }
        //runnable invoke means uses code () -> Unit

    override fun onEnable() {
        plugin = this
        saveDefaultConfig()
        statsConfig = Config.load(this)

        getCommand("sssreload")?.setExecutor(this)
        getCommand("sssactivate")?.setExecutor(ActivateToken())

        if (statsConfig.token.isNotEmpty()) {
            activateBot(statsConfig.token)
        } else {
            println("There is no token, please put bot token in config or use the command /sssactivate <token> <channelId>")
            for (player in Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("sss.access")) { // TODO: Probably move to PlayerJoinEvent
                    player.sendMessage("${ChatColor.RED}${ChatColor.BOLD}Attention! ${ChatColor.WHITE}" +
                        "There is no token in Server stats sender config")}
            }
        }

        scheduleTimer(statsConfig.timeIntervalTicks.toLong()) {
            if (!isBotEnabled || !statsConfig.repeat) return@scheduleTimer
            val channel = jda.getTextChannelById(statsConfig.chanid) ?: return@scheduleTimer
            if (statsConfig.embed) channel.sendMessageEmbeds(getEmbed(statsConfig)).queue()
            else channel.sendMessage(infoField).queue()
        }
    }

    fun onCommand(sender: CommandSender) { // Reload command, god knows where this is called from. (it's probably not)
        if (!sender.hasPermission("sss.access")) return
        reloadConfig()
        statsConfig = Config.load(this)
        sender.sendMessage("${ChatColor.GREEN}${ChatColor.BOLD}Success! ${ChatColor.WHITE} SSS config reloaded")
    }

    fun activateBot(token: String) {
        jda = JDABuilder.createDefault(token)
            .setActivity(Activity.watching("your server stats"))
            .addEventListeners(DiscordListener(this))
            .build()
    }

    fun updateConfig(changes: FileConfiguration.() -> Unit) {
        changes.invoke(config)
        saveConfig()
        statsConfig = Config.load(this)
    }


}
