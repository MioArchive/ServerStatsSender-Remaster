package com.lent.serverstatssender

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin(), CommandExecutor {

    private lateinit var jda: JDA
    lateinit var statsConfig: Config

    companion object {
        lateinit var plugin: Main private set

        fun scheduleTimer(intervalTicks: Long, delayTicks: Int = 0, runnable: () -> Unit) {
            Bukkit.getScheduler().runTaskTimer(plugin, Runnable { runnable.invoke() }, delayTicks.toLong(), intervalTicks.toLong())
        }
    }
        //runnable invoke means uses code () -> Unit

    override fun onEnable() {
        getCommand("sssreload")?.setExecutor(this)
        plugin = this
        saveDefaultConfig()
        statsConfig = Config.load(this)

        scheduleTimer(statsConfig.timeIntervalTicks.toLong()) {
            if (!statsConfig.repeat) return@scheduleTimer
            val channel = jda.getTextChannelById(statsConfig.chanid) ?: return@scheduleTimer
            if (statsConfig.embed) channel.sendMessageEmbeds(getEmbed(statsConfig)).queue()
            else channel.sendMessage(infoField).queue()
        }

        jda = JDABuilder.createDefault(statsConfig.token)
            .setActivity(Activity.watching("your server stats"))
            .addEventListeners(DiscordListener(this))
            .build()

        println("successfully started")
    }

    fun onCommand(sender: CommandSender) {
        if (sender.hasPermission("sss.access")) {
            reloadConfig()
            statsConfig = Config.load(this)
            sender.sendMessage("successfully reloaded config")
        }
    }

}
