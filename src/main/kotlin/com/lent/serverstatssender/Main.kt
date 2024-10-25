package com.lent.serverstatssender

import me.lucko.spark.api.SparkProvider
import me.lucko.spark.api.statistic.StatisticWindow
import me.lucko.spark.api.statistic.StatisticWindow.TicksPerSecond
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo
import me.lucko.spark.api.statistic.types.DoubleStatistic
import me.lucko.spark.api.statistic.types.GenericStatistic
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Color
import java.io.File
import kotlin.math.roundToInt


class Main : JavaPlugin(), CommandExecutor {

    private lateinit var jda: JDA



    companion object {
        lateinit var plugin: Main private set
        fun scheduleTimer(intervalTicks: Long, delayTicks: Int = 0, runnable: () -> Unit) {
            Bukkit.getScheduler()
                .runTaskTimer(plugin, Runnable { runnable.invoke() }, delayTicks.toLong(), intervalTicks.toLong())
        }

    }
        //runnable invoke means uses code () -> Unit

    override fun onEnable() {
        getCommand("sssreload")?.setExecutor(this)
        plugin = this

        val cmdName = config.getString("cmdNameForDiscord")
        val repeat = config.getBoolean("repeat")
        val time = config.getInt("time").toLong()
        val spark = SparkProvider.get()
        val imageURL = Main.plugin.config.getString("imgURL")
        val tps: DoubleStatistic<StatisticWindow.TicksPerSecond>? = spark.tps()
        val tpsLast10Secs = tps!!.poll(TicksPerSecond.SECONDS_10).roundToInt()
        val tpsLast5Mins = tps.poll(TicksPerSecond.MINUTES_5).roundToInt()
        val cpuUsage: DoubleStatistic<StatisticWindow.CpuUsage> = spark.cpuSystem()
        val usageLastMin = cpuUsage.poll(StatisticWindow.CpuUsage.MINUTES_1).roundToInt()
        val mspt: GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick>? = spark.mspt()
        var msptString = ""

        config.options().copyDefaults()
        saveDefaultConfig()
        val embed = config.getBoolean("embed")
        val embedTitle = plugin.config.getString("embedTitle")
        var embedBuilder = EmbedBuilder()

        scheduleTimer(time) {
            if (repeat) {
                mspt?.poll(StatisticWindow.MillisPerTick.MINUTES_1)?.let { msptLastMin ->
                    val msptMean = msptLastMin.percentile95th().roundToInt()
                    val mspt95Percentile = msptLastMin.percentile95th().roundToInt()
                    msptString = "\nMsptMean Usage: $msptMean\nmspt95Percentile: $mspt95Percentile"
                }
                config.options().copyDefaults()
                saveDefaultConfig()
                val chanId = config.getString("chanid")
                if (chanId != null && embed) {
                    embedBuilder.setTitle(embedTitle)
                    embedBuilder.setColor(Color.BLACK)
                    embedBuilder.setImage(imageURL)
                    embedBuilder.addField("Statistics:", "TPS: $tpsLast10Secs, Last 5 minutes: $tpsLast5Mins\nCPU Usage Last Min: $usageLastMin%$msptString", false)
                    jda.getTextChannelById(chanId)?.sendMessageEmbeds(embedBuilder.build())?.queue()
                }
            }
        }

        config.options().copyDefaults()
        saveDefaultConfig()
        val token = config.getString("token")!!
        val builder = JDABuilder.createDefault(token)
        builder.setActivity(Activity.watching("your server stats"))
        builder.addEventListeners(DiscordListener())
        jda = builder.build()
        println("successfully started")
    }

    fun onCommand(sender: CommandSender) {
    if (sender.hasPermission("sss.access"))
        reloadConfig()
        sender.sendMessage("successfully reloaded config")
    }

}
