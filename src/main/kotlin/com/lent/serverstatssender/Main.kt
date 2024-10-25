package com.lent.serverstatssender

import me.lucko.spark.api.SparkProvider
import me.lucko.spark.api.statistic.StatisticWindow
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
import org.bukkit.plugin.java.JavaPlugin


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
        plugin = this
        val repeat = config.getBoolean("repeat")
        val time = config.getInt("time").toLong()
        val spark = SparkProvider.get()
        val tps: DoubleStatistic<StatisticWindow.TicksPerSecond>? = spark.tps()
        val tpsLast10Secs = tps?.poll(StatisticWindow.TicksPerSecond.SECONDS_10)
        val tpsLast5Mins = tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_5)
        val cpuUsage: DoubleStatistic<StatisticWindow.CpuUsage> = spark.cpuSystem()
        val usageLastMin = cpuUsage.poll(StatisticWindow.CpuUsage.MINUTES_1)
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
                    val msptMean = msptLastMin.mean()
                    val mspt95Percentile = msptLastMin.percentile95th()
                    msptString = "\nMsptMean Usage: $msptMean\nmspt95Percentile: $mspt95Percentile"
                }
                config.options().copyDefaults()
                saveDefaultConfig()
                val chanId = config.getString("chanid")
                if (chanId != null && embed) {
                    embedBuilder.setTitle(embedTitle)
                    jda.getTextChannelById(chanId)?.sendMessage("TPS: $tpsLast10Secs, $tpsLast5Mins\nCPU Usage: $usageLastMin$msptString")?.setEmbeds()?.queue()
                } /*else {
                    if (chanId != null && !embed) {
                        jda.getTextChannelById(chanId)?.sendMessage("TPS: $tpsLast10Secs, $tpsLast5Mins\nCPU Usage: $usageLastMin$msptString")?.queue()
                    }
                }*/

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


}
