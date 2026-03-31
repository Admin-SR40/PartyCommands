package com.partycommands.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.partycommands.config.Config
import com.partycommands.utils.*
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.commands.SharedSuggestionProvider
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object PartyCommandHandler {
    private val mc = Minecraft.getInstance()

    // 8ball 回答
    private val eightBallResponses = arrayOf(
        "It is certain", "It is decidedly so", "Without a doubt",
        "Yes definitely", "You may rely on it", "As I see it, yes",
        "Most likely", "Outlook good", "Yes", "Signs point to yes",
        "Reply hazy try again", "Ask again later", "Better not tell you now",
        "Cannot predict now", "Concentrate and ask again", "Don't count on it",
        "My reply is no", "My sources say no", "Outlook not so good", "Very doubtful"
    )

    // 地牢楼层映射
    private val floorInstances = mapOf(
        "f1" to "catacombs_floor_one",
        "f2" to "catacombs_floor_two",
        "f3" to "catacombs_floor_three",
        "f4" to "catacombs_floor_four",
        "f5" to "catacombs_floor_five",
        "f6" to "catacombs_floor_six",
        "f7" to "catacombs_floor_seven",
        "m1" to "master_catacombs_floor_one",
        "m2" to "master_catacombs_floor_two",
        "m3" to "master_catacombs_floor_three",
        "m4" to "master_catacombs_floor_four",
        "m5" to "master_catacombs_floor_five",
        "m6" to "master_catacombs_floor_six",
        "m7" to "master_catacombs_floor_seven",
        "t1" to "kuudra_normal",
        "t2" to "kuudra_hot",
        "t3" to "kuudra_burning",
        "t4" to "kuudra_fiery",
        "t5" to "kuudra_infernal"
    )

    fun init() {
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            ServerUtils.updateTps()
        }
        registerCommands()
        Commands.rebuildDispatcher()
    }

    fun registerCommands() {
        // 帮助
        Commands.add(object : Command("help", "Show help message", "h") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    showHelp()
                    Command.SINGLE_SUCCESS
                }
            }
        })

        // 信息类命令
        Commands.add(object : Command("ping", "Show latency") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.ping) respond("Current Ping: ${ServerUtils.currentPing}ms")
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("tps", "Show server TPS") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.tps) respond("Current TPS: ${ServerUtils.averageTps.toFixed(1)}")
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("fps", "Show current FPS") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.fps) respond("Current FPS: ${ServerUtils.currentFps}")
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("time", "Show current time") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.time) {
                        val time = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
                        respond("Current Time: $time")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("location", "Show current location", "loc") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.location) respond("Current Location: Unknown")
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("coords", "Show current coordinates", "co") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.location) {
                        val pos = getPositionString()
                        respond("Current Coordinates: $pos")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("holding", "Show held item", "hold") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.location) {
                        val item = mc.player?.mainHandItem?.displayName?.string ?: "Air"
                        respond("Holding: $item")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        // 娱乐命令
        Commands.add(object : Command("cf", "Coin flip", "coinflip") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.coinflip) respond(if (Random.nextBoolean()) "heads" else "tails")
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("8ball", "Magic 8 Ball") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.eightball) respond(eightBallResponses.random())
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("dice", "Roll a dice") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.dice) respond((1..6).random().toString())
                    Command.SINGLE_SUCCESS
                }
            }
        })

        // 队伍管理命令
        Commands.add(object : Command("warp", "Warp party members", "w") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.warp) {
                        if (PartyUtils.isLeader()) {
                            sendCommand("party warp")
                            modMessage("§aExecuted: /party warp")
                        } else {
                            modMessage("§cYou are not the leader!")
                        }
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("allinvite", "Enable all invite", "allinv") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.allinvite) {
                        if (PartyUtils.isLeader()) {
                            sendCommand("party settings allinvite")
                            modMessage("§aExecuted: /party settings allinvite")
                        } else {
                            modMessage("§cYou are not the leader!")
                        }
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("transfer", "Transfer party leader", "pt") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .suggests { _, suggestionsBuilder ->
                        PartyUtils.members.forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes { ctx ->
                        if (!PartyUtils.isInParty) {
                            modMessage("§cYou are not in a party!")
                        } else if (PartyUtils.isLeader()) {
                            val input = StringArgumentType.getString(ctx, "player")
                            val target = PartyUtils.findMember(input)
                            sendCommand("p transfer $target")
                            modMessage("§aExecuted: /p transfer $target")
                        } else {
                            modMessage("§cYou are not the leader!")
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    modMessage("§cUsage: !transfer <player>")
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("promote", "Promote member") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .suggests { _, suggestionsBuilder ->
                        PartyUtils.members.forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes { ctx ->
                        if (Config.settings.promote) {
                            if (PartyUtils.isLeader()) {
                                val input = StringArgumentType.getString(ctx, "player")
                                val target = PartyUtils.findMember(input)
                                sendCommand("party promote $target")
                                modMessage("§aExecuted: /party promote $target")
                            } else {
                                modMessage("§cYou are not the leader!")
                            }
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    if (Config.settings.promote) {
                        val playerName = mc.player?.name?.string ?: "Unknown"
                        sendCommand("party promote $playerName")
                        modMessage("§aExecuted: /party promote $playerName")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("demote", "Demote member") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .suggests { _, suggestionsBuilder ->
                        PartyUtils.members.forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes { ctx ->
                        if (Config.settings.demote) {
                            if (PartyUtils.isLeader()) {
                                val input = StringArgumentType.getString(ctx, "player")
                                val target = PartyUtils.findMember(input)
                                sendCommand("party demote $target")
                                modMessage("§aExecuted: /party demote $target")
                            } else {
                                modMessage("§cYou are not the leader!")
                            }
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    if (Config.settings.demote) {
                        val playerName = mc.player?.name?.string ?: "Unknown"
                        sendCommand("party demote $playerName")
                        modMessage("§aExecuted: /party demote $playerName")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("kick", "Kick member", "k") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .suggests { _, suggestionsBuilder ->
                        PartyUtils.members.forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .then(Command.argument("reason", StringArgumentType.greedyString())
                        .executes { ctx ->
                            if (Config.settings.kick) {
                                if (PartyUtils.isLeader()) {
                                    val target = StringArgumentType.getString(ctx, "player")
                                    val reason = StringArgumentType.getString(ctx, "reason")
                                    sendPartyChat("Kicking $target : $reason")
                                    sendCommand("p kick $target")
                                    modMessage("§aExecuted: /p kick $target")
                                } else {
                                    modMessage("§cYou are not the leader!")
                                }
                            }
                            Command.SINGLE_SUCCESS
                        })
                    .executes { ctx ->
                        if (Config.settings.kick) {
                            if (PartyUtils.isLeader()) {
                                val target = StringArgumentType.getString(ctx, "player")
                                sendCommand("p kick $target")
                                modMessage("§aExecuted: /p kick $target")
                            } else {
                                modMessage("§cYou are not the leader!")
                            }
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    modMessage("§cUsage: !kick <player> [reason]")
                    Command.SINGLE_SUCCESS
                }
            }
        })

        // 地牢排队命令
        floorInstances.keys.forEach { cmd ->
            Commands.add(object : Command(cmd, "Queue $cmd") {
                override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                    builder.executes {
                        if (Config.settings.queueInstance) {
                            if (!PartyUtils.isInParty || PartyUtils.isLeader()) {
                                val instance = floorInstances[cmd]!!
                                modMessage("§8Entering -> §e${cmd.uppercase()}")
                                sendCommand("joininstance $instance")
                            } else {
                                modMessage("§cYou are not the leader!")
                            }
                        }
                        Command.SINGLE_SUCCESS
                    }
                }
            })
        }

        // 其他命令
        Commands.add(object : Command("boop", "Boop a player") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .executes { ctx ->
                        if (Config.settings.boop) {
                            val target = StringArgumentType.getString(ctx, "player")
                            sendCommand("boop $target")
                            modMessage("§aExecuted: /boop $target")
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    modMessage("§cUsage: !boop <player>")
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("invite", "Invite player to party", "inv") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .executes { ctx ->
                        if (Config.settings.invite) {
                            val target = StringArgumentType.getString(ctx, "player")
                            sendCommand("p invite $target")
                            modMessage("§aExecuted: /p invite $target")
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    modMessage("§cUsage: !invite <player>")
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("forward", "Toggle party chat forwarding") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    Config.settings.respondInPartyChat = !Config.settings.respondInPartyChat
                    Config.save()
                    Config.load()
                    Commands.rebuildDispatcher()
                    val status = if (Config.settings.respondInPartyChat) "§aON" else "§cOFF"
                    modMessage("§fParty chat forwarding: $status")
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("reload", "Reload config") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    Config.load()
                    Commands.rebuildDispatcher()
                    modMessage("§aConfig reloaded!")
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("ver", "Show version info", "version") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    rawMessage("§b===== PartyCommands =====")
                    rawMessage("§fVersion: §a0.1")
                    rawMessage("§fBased on: §aOdin Mod / Meteor Client")
                    rawMessage("§fAuthor: §aAdmin_SR40")
                    rawMessage("§b========================")
                    Command.SINGLE_SUCCESS
                }
            }
        })
    }

    /**
     * 发送响应
     */
    private fun respond(message: String) {
        if (Config.settings.respondInPartyChat && PartyUtils.isInParty) {
            sendPartyChat("CMD » $message")
        }
        if (Config.settings.showResponseLocally) {
            modMessage("§f$message")
        }
    }

    /**
     * 显示帮助
     */
    private fun showHelp() {
        rawMessage("§b===== Available Commands =====")
        rawMessage("§f!help §7- Show this message")
        rawMessage("§f!warp §7- Warp members to this hub")
        rawMessage("§f!allinvite §7- Enable all invite")
        rawMessage("§f!pt <player> §7- Transfer party leader")
        rawMessage("§f!promote <player> §7- Promote member")
        rawMessage("§f!demote <player> §7- Demote member")
        rawMessage("§f!kick <player> §7- Kick member from party")
        rawMessage("§f!ping §7- Show latency")
        rawMessage("§f!tps §7- Show TPS")
        rawMessage("§f!fps §7- Show FPS")
        rawMessage("§f!time §7- Show current time")
        rawMessage("§f!coords §7- Show coordinates")
        rawMessage("§f!loc §7- Show location")
        rawMessage("§f!hold §7- Show held item")
        rawMessage("§f!cf §7- Coin flip")
        rawMessage("§f!8ball §7- Magic 8 Ball")
        rawMessage("§f!dice §7- Roll a dice")
        rawMessage("§f!t1-5 §7- Kuudra")
        rawMessage("§f!f1-7 / m1-7 §7- Dungeon")
        rawMessage("§f!boop <player> §7- Boop a player")
        rawMessage("§f!invite <player> §7- Invite player to party")
        rawMessage("§f!forward §7- Toggle party chat forwarding")
        rawMessage("§f!reload §7- Reload config")
        rawMessage("§f!ver §7- Show version info")
        rawMessage("§b============================")
    }
}
