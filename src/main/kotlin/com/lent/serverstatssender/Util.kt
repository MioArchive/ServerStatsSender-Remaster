package com.lent.serverstatssender

import me.lucko.spark.api.SparkProvider
import me.lucko.spark.api.statistic.StatisticWindow
import me.lucko.spark.api.statistic.StatisticWindow.TicksPerSecond
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo
import me.lucko.spark.api.statistic.types.DoubleStatistic
import me.lucko.spark.api.statistic.types.GenericStatistic
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color
import kotlin.math.roundToInt

val spark = SparkProvider.get()

fun getEmbed(config: Config) = EmbedBuilder()
    .setTitle(config.embedTitle)
    .setColor(Color.BLACK)
    .setImage(config.imageURL)
    .addField("Statistics:", infoField, false)
    .build()

val infoField: String get() {
    val tps: DoubleStatistic<TicksPerSecond>? = spark.tps()
    val tpsLast10Secs = tps?.poll(TicksPerSecond.SECONDS_10)?.roundToInt() ?: 0
    val tpsLast5Mins = tps?.poll(TicksPerSecond.MINUTES_5)?.roundToInt() ?: 0
    val cpuUsage: DoubleStatistic<StatisticWindow.CpuUsage> = spark.cpuSystem()
    val usageLastMin = cpuUsage.poll(StatisticWindow.CpuUsage.MINUTES_1).roundToInt()

    val mspt: GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick>? = spark.mspt()
    val msptpolled = mspt?.poll(StatisticWindow.MillisPerTick.MINUTES_1)
    val msptMean = msptpolled?.percentile95th()?.roundToInt() ?: 0
    val mspt95Percentile = msptpolled?.percentile95th()?.roundToInt() ?: 0

    return """
        TPS: $tpsLast10Secs, Last 5 minutes: $tpsLast5Mins
        CPU Usage Last Min: $usageLastMin%
        Mspt Mean Usage: $msptMean
        mspt 95th Percentile: $mspt95Percentile
    """.trimIndent()
}