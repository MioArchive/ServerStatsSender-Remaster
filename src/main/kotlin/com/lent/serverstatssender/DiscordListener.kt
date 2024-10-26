package com.lent.serverstatssender

import me.lucko.spark.api.SparkProvider
import me.lucko.spark.api.statistic.StatisticWindow.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.awt.Color
import kotlin.math.roundToInt
import kotlin.math.roundToLong


//

class DiscordListener: ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        super.onSlashCommandInteraction(event)
        val spark = SparkProvider.get()

        val cmdName = Main.plugin.config.getString("cmdNameForDiscord")
        val imageURL = Main.plugin.config.getString("imgURL")
        val embedCmd = Main.plugin.config.getBoolean("embedCmd")
        val embedTitle = Main.plugin.config.getString("embedTitle")
        val embedBuilder = EmbedBuilder()

        val tps = spark.tps()
        val tpsLast10Secs = tps!!.poll(TicksPerSecond.SECONDS_10).roundToInt()
        val tpsLast5Mins = tps.poll(TicksPerSecond.MINUTES_5).roundToInt()
        val cpuUsage = spark.cpuSystem()
        val usagelastMin = cpuUsage.poll(CpuUsage.MINUTES_1).roundToLong()

        val mspt = spark.mspt()
        var msptstring = ""


        if (embedCmd && event.name == cmdName) {
            embedBuilder.setTitle(embedTitle)
            embedBuilder.setColor(Color.BLACK)
            embedBuilder.setImage(imageURL)
            embedBuilder.addField("Statistics:", "TPS: $tpsLast10Secs, Last 5 minutes: $tpsLast5Mins\nCPU Usage Last Min: $usagelastMin%$msptstring", false)
            event.channel.sendMessageEmbeds(embedBuilder.build()).queue()

        } else {
                if (event.name == cmdName && !embedCmd) {
                event.reply("TPS: $tpsLast10Secs, $tpsLast5Mins\nCPU Usage: $usagelastMin$msptstring").queue()
                }
            }

    }

    override fun onGuildReady(event: GuildReadyEvent) {
        val cmdName = Main.plugin.config.getString("cmdNameForDiscord")
        val guild = event.guild
        guild.updateCommands()
            .addCommands(cmdName?.let { Commands.slash(it, "Shows the server stats.") })
            .queue();
    }

}