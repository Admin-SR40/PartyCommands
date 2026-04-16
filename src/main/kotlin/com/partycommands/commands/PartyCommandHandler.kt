package com.partycommands.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.partycommands.config.Config
import com.partycommands.gui.ConfigGui
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
            PartyListHandler.onTick()
            CountdownManager.onTick()
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
                    if (Config.settings.ping) {
                        val ping = ServerUtils.currentPing
                        val color = getPingColor(ping)
                        respond(formatResponse("Current Ping", "${ping}ms", color))
                    } else {
                        respondDisabled("ping")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("tps", "Show server TPS") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.tps) {
                        val tps = ServerUtils.averageTps
                        if (tps < 0) {
                            respond(formatResponse("Current TPS", "§7Updating TPS, please wait...", ""))
                        } else {
                            val color = getTpsColor(tps)
                            respond(formatResponse("Current TPS", tps.toFixed(1), color))
                        }
                    } else {
                        respondDisabled("tps")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("fps", "Show current FPS") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.fps) {
                        val fps = ServerUtils.currentFps
                        val color = getFpsColor(fps)
                        respond(formatResponse("Current FPS", fps.toString(), color))
                    } else {
                        respondDisabled("fps")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("time", "Show current time") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.time) {
                        val time = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
                        respond(formatResponse("Current Time", time, "§f"))
                    } else {
                        respondDisabled("time")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("location", "Show current coordinates", "loc") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.location) {
                        val pos = getPositionString()
                        respond(formatResponse("Coordinates", pos, "§f"))
                    } else {
                        respondDisabled("location")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("coords", "Show current coordinates", "co") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.coords) {
                        val pos = getPositionString()
                        respond(formatResponse("Current Coordinates", pos, "§f"))
                    } else {
                        respondDisabled("coords")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("holding", "Show held item", "hold") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.holding) {
                        val item = mc.player?.mainHandItem?.displayName?.string ?: "Air"
                        respond(formatResponse("Holding", item, "§f"))
                    } else {
                        respondDisabled("holding")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("status", "Show party status") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.status) {
                        PartyListHandler.startWaiting()
                        sendCommand("p list")
                    } else {
                        respondDisabled("status")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        // 娱乐命令 - 合并到 !fun
        Commands.add(object : Command("fun", "Fun commands") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                // !fun cf - 抛硬币
                builder.then(Command.literal("cf")
                    .executes {
                        if (Config.settings.coinflip) {
                            val result = if (Random.nextBoolean()) "§6heads" else "§ftails"
                            respond(formatResponse("Coinflip", result, ""))
                        } else {
                            respondDisabled("fun cf")
                        }
                        Command.SINGLE_SUCCESS
                    })
                
                // !fun 8ball - 魔法8号球
                builder.then(Command.literal("8ball")
                    .executes {
                        if (Config.settings.eightball) {
                            respond(formatResponse("8-Ball", eightBallResponses.random(), "§d"))
                        } else {
                            respondDisabled("fun 8ball")
                        }
                        Command.SINGLE_SUCCESS
                    })
                
                // !fun dice - 掷骰子
                builder.then(Command.literal("dice")
                    .executes {
                        if (Config.settings.dice) {
                            val roll = (1..6).random()
                            val color = when (roll) {
                                6 -> "§2"  // 深绿色
                                5 -> "§a"  // 绿色
                                4 -> "§e"  // 黄色
                                3 -> "§6"  // 橙色
                                else -> "§c" // 红色
                            }
                            respond(formatResponse("Dice Roll", roll.toString(), color))
                        } else {
                            respondDisabled("fun dice")
                        }
                        Command.SINGLE_SUCCESS
                    })
                
                // !fun boop <player> - boop玩家
                builder.then(Command.literal("boop")
                    .then(Command.argument("player", StringArgumentType.word())
                        .executes { ctx ->
                            if (Config.settings.boop) {
                                val target = StringArgumentType.getString(ctx, "player")
                                sendCommand("boop $target")
                                respond(formatResponse("Boop", "§aBooped $target", ""))
                            } else {
                                respondDisabled("fun boop")
                            }
                            Command.SINGLE_SUCCESS
                        })
                    .executes {
                        respond(formatResponse("Usage", "§c!fun boop <player>", ""))
                        Command.SINGLE_SUCCESS
                    })
                
                // !fun random [min] [max] - 随机数
                builder.then(Command.literal("random")
                    .then(Command.argument("min", StringArgumentType.word())
                        .then(Command.argument("max", StringArgumentType.word())
                            .executes { ctx ->
                                val minStr = StringArgumentType.getString(ctx, "min")
                                val maxStr = StringArgumentType.getString(ctx, "max")
                                val min = minStr.toIntOrNull()
                                val max = maxStr.toIntOrNull()
                                
                                if (min == null || max == null) {
                                    respond(formatResponse("Error", "§cInvalid numbers! Usage: !fun random <min> <max>", ""))
                                } else {
                                    val actualMin = minOf(min, max)
                                    val actualMax = maxOf(min, max)
                                    val result = (actualMin..actualMax).random()
                                    respond("§bRandom Value: §e$result §7[§f$actualMin§7-§f$actualMax§7]")
                                }
                                Command.SINGLE_SUCCESS
                            })
                        .executes { ctx ->
                            val maxStr = StringArgumentType.getString(ctx, "min")
                            val max = maxStr.toIntOrNull()
                            if (max == null || max < 1) {
                                respond(formatResponse("Error", "§cInvalid number! Usage: !fun random <max>", ""))
                            } else {
                                val result = (1..max).random()
                                respond("§bRandom Value: §e$result §7[§f1§7-§f$max§7]")
                            }
                            Command.SINGLE_SUCCESS
                        })
                    .executes {
                        val result = (1..100).random()
                        respond("§bRandom Value: §e$result §7[§f1§7-§f100§7]")
                        Command.SINGLE_SUCCESS
                    })
                
                builder.executes {
                    respond(formatResponse("Fun Commands", "§ecf, 8ball, dice, boop <player>, random [min] [max]", ""))
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
                            respond(formatResponse("Warp", "§aSent warp request", ""))
                        } else {
                            respond(formatResponse("Error", "§cYou are not the leader!", ""))
                        }
                    } else {
                        respondDisabled("warp")
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
                            respond(formatResponse("All Invite", "§aEnabled", ""))
                        } else {
                            respond(formatResponse("Error", "§cYou are not the leader!", ""))
                        }
                    } else {
                        respondDisabled("allinvite")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("transfer", "Transfer party leader", "pt") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .suggests { _, suggestionsBuilder ->
                        val myName = mc.player?.name?.string ?: ""
                        PartyUtils.members
                            .filter { 
                                // 清理 ● 和空格后再比较
                                val cleanMember = it.replace("●", "").trim()
                                !cleanMember.equals(myName, ignoreCase = true) 
                            }
                            .distinct()
                            .forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes { ctx ->
                        if (!PartyUtils.isInParty) {
                            respond(formatResponse("Error", "§cYou are not in a party!", ""))
                        } else if (PartyUtils.isLeader()) {
                            val input = StringArgumentType.getString(ctx, "player")
                            val target = PartyUtils.findMember(input)
                            sendCommand("p transfer $target")
                            respond(formatResponse("Transfer", "§aTransferred to $target", ""))
                        } else {
                            respond(formatResponse("Error", "§cYou are not the leader!", ""))
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    respond(formatResponse("Usage", "§c!transfer <player>", ""))
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("promote", "Promote member") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .suggests { _, suggestionsBuilder ->
                        val myName = mc.player?.name?.string ?: ""
                        PartyUtils.members
                            .filter { 
                                val cleanMember = it.replace("●", "").trim()
                                !cleanMember.equals(myName, ignoreCase = true) 
                            }
                            .distinct()
                            .forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes { ctx ->
                        if (Config.settings.promote) {
                            if (PartyUtils.isLeader()) {
                                val input = StringArgumentType.getString(ctx, "player")
                                val target = PartyUtils.findMember(input)
                                sendCommand("party promote $target")
                                respond(formatResponse("Promote", "§aPromoted $target", ""))
                            } else {
                                respond(formatResponse("Error", "§cYou are not the leader!", ""))
                            }
                        } else {
                            respondDisabled("promote")
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    if (Config.settings.promote) {
                        respond(formatResponse("Usage", "§c!promote <player>", ""))
                    } else {
                        respondDisabled("promote")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("demote", "Demote member") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .suggests { _, suggestionsBuilder ->
                        val myName = mc.player?.name?.string ?: ""
                        PartyUtils.members
                            .filter { 
                                val cleanMember = it.replace("●", "").trim()
                                !cleanMember.equals(myName, ignoreCase = true) 
                            }
                            .distinct()
                            .forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes { ctx ->
                        if (Config.settings.demote) {
                            if (PartyUtils.isLeader()) {
                                val input = StringArgumentType.getString(ctx, "player")
                                val target = PartyUtils.findMember(input)
                                sendCommand("party demote $target")
                                respond(formatResponse("Demote", "§aDemoted $target", ""))
                            } else {
                                respond(formatResponse("Error", "§cYou are not the leader!", ""))
                            }
                        } else {
                            respondDisabled("demote")
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    if (Config.settings.demote) {
                        respond(formatResponse("Usage", "§c!demote <player>", ""))
                    } else {
                        respondDisabled("demote")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("disband", "Disband party") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.disband) {
                        if (PartyUtils.isLeader()) {
                            sendCommand("p disband")
                            // 强制本地显示，不发送到队伍聊天（队伍已解散）
                            modMessage(formatResponse("Disband", "§aParty disbanded", ""))
                        } else {
                            respond(formatResponse("Error", "§cYou are not the leader!", ""))
                        }
                    } else {
                        respondDisabled("disband")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("leave", "Leave party") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.leave) {
                        sendCommand("p leave")
                        // 强制本地显示，不发送到队伍聊天（已经离开了）
                        modMessage(formatResponse("Leave", "§aLeft party", ""))
                    } else {
                        respondDisabled("leave")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("kick", "Kick member", "k") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .suggests { _, suggestionsBuilder ->
                        val myName = mc.player?.name?.string ?: ""
                        PartyUtils.members
                            .filter { 
                                val cleanMember = it.replace("●", "").trim()
                                !cleanMember.equals(myName, ignoreCase = true) 
                            }
                            .distinct()
                            .forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .then(Command.argument("reason", StringArgumentType.greedyString())
                        .executes { ctx ->
                            if (Config.settings.kick) {
                                if (PartyUtils.isLeader()) {
                                    val input = StringArgumentType.getString(ctx, "player")
                                    val target = PartyUtils.findMember(input)
                                    val reason = StringArgumentType.getString(ctx, "reason").noControlCodes
                                    // 先发送原因到队伍聊天
                                    sendPartyChat("Kicking $target : $reason")
                                    // 延迟 500ms 再踢出，确保消息先显示
                                    Thread {
                                        Thread.sleep(500)
                                        mc.execute {
                                            sendCommand("p kick $target")
                                        }
                                    }.start()
                                    respond(formatResponse("Kick", "§aKicked $target", ""))
                                } else {
                                    respond(formatResponse("Error", "§cYou are not the leader!", ""))
                                }
                            } else {
                                respondDisabled("kick")
                            }
                            Command.SINGLE_SUCCESS
                        })
                    .executes { ctx ->
                        if (Config.settings.kick) {
                            if (PartyUtils.isLeader()) {
                                val input = StringArgumentType.getString(ctx, "player")
                                val target = PartyUtils.findMember(input)
                                sendCommand("p kick $target")
                                respond(formatResponse("Kick", "§aKicked $target", ""))
                            } else {
                                respond(formatResponse("Error", "§cYou are not the leader!", ""))
                            }
                        } else {
                            respondDisabled("kick")
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    respond(formatResponse("Usage", "§c!kick <player> [reason]", ""))
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("kickoffline", "Kick offline members") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.kickoffline) {
                        if (PartyUtils.isLeader()) {
                            sendCommand("p kickoffline")
                            respond(formatResponse("Kickoffline", "§aKicked offline members", ""))
                        } else {
                            respond(formatResponse("Error", "§cYou are not the leader!", ""))
                        }
                    } else {
                        respondDisabled("kickoffline")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("kickall", "Kick all members except specified") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("exceptions", StringArgumentType.greedyString())
                    .suggests { _, suggestionsBuilder ->
                        val myName = mc.player?.name?.string ?: ""
                        PartyUtils.members
                            .filter { 
                                val cleanMember = it.replace("●", "").trim()
                                !cleanMember.equals(myName, ignoreCase = true) 
                            }
                            .distinct()
                            .forEach { suggestionsBuilder.suggest(it) }
                        suggestionsBuilder.buildFuture()
                    }
                    .executes { ctx ->
                        if (Config.settings.kickall) {
                            if (PartyUtils.isLeader()) {
                                val exceptionsInput = StringArgumentType.getString(ctx, "exceptions")
                                val exceptions = exceptionsInput.split(" ").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                                val myName = mc.player?.name?.string ?: ""
                                exceptions.add(myName)
                                
                                val toKick = PartyUtils.members.filter { member ->
                                    exceptions.none { exception -> member.equals(exception, ignoreCase = true) }
                                }
                                
                                if (toKick.isNotEmpty()) {
                                    sendPartyChat("Kicking all members...")
                                    toKick.forEachIndexed { index, target ->
                                        Thread {
                                            Thread.sleep(500L * (index + 1))
                                            mc.execute {
                                                sendCommand("p kick $target")
                                            }
                                        }.start()
                                    }
                                    respond(formatResponse("Kickall", "§aKicking ${toKick.size} member(s)", ""))
                                } else {
                                    respond(formatResponse("Kickall", "§cNo members to kick", ""))
                                }
                            } else {
                                respond(formatResponse("Error", "§cYou are not the leader!", ""))
                            }
                        } else {
                            respondDisabled("kickall")
                        }
                        Command.SINGLE_SUCCESS
                    })
                
                builder.executes {
                    if (Config.settings.kickall) {
                        if (PartyUtils.isLeader()) {
                            val myName = mc.player?.name?.string ?: ""
                            val toKick = PartyUtils.members.filter { !it.equals(myName, ignoreCase = true) }
                            
                            if (toKick.isNotEmpty()) {
                                sendPartyChat("Kicking all members...")
                                toKick.forEachIndexed { index, target ->
                                    Thread {
                                        Thread.sleep(500L * (index + 1))
                                        mc.execute {
                                            sendCommand("p kick $target")
                                        }
                                    }.start()
                                }
                                respond(formatResponse("Kickall", "§aKicking ${toKick.size} member(s)", ""))
                            } else {
                                respond(formatResponse("Kickall", "§cNo members to kick", ""))
                            }
                        } else {
                            respond(formatResponse("Error", "§cYou are not the leader!", ""))
                        }
                    } else {
                        respondDisabled("kickall")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        // 地牢排队命令（支持倒计时，不需要队伍）
        floorInstances.keys.forEach { cmd ->
            Commands.add(object : Command(cmd, "Queue $cmd") {
                override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                    // 支持可选的时间参数：!f7 60
                    builder.then(Command.argument("seconds", StringArgumentType.word())
                        .executes { ctx ->
                            if (Config.settings.queueInstance) {
                                val seconds = StringArgumentType.getString(ctx, "seconds").toIntOrNull()
                                if (seconds != null && seconds > 0) {
                                    // 最大5分钟
                                    val actualSeconds = minOf(seconds, 300)
                                    CountdownManager.startCountdown(actualSeconds, cmd.uppercase())
                                } else {
                                    respond(formatResponse("Error", "§cInvalid time!", ""))
                                }
                            } else {
                                respondDisabled(cmd)
                            }
                            Command.SINGLE_SUCCESS
                        })
                    
                    // 无参数：直接排队
                    builder.executes {
                        if (Config.settings.queueInstance) {
                            val instance = floorInstances[cmd]!!
                            respond(formatResponse("Queue", "§e${cmd.uppercase()}", ""))
                            sendCommand("joininstance $instance")
                        } else {
                            respondDisabled(cmd)
                        }
                        Command.SINGLE_SUCCESS
                    }
                }
            })
        }

        // 其他命令
        Commands.add(object : Command("invite", "Invite player to party", "inv") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("player", StringArgumentType.word())
                    .executes { ctx ->
                        if (Config.settings.invite) {
                            val target = StringArgumentType.getString(ctx, "player")
                            sendCommand("p invite $target")
                            respond(formatResponse("Invite", "§aInvited $target", ""))
                        } else {
                            respondDisabled("invite")
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    respond(formatResponse("Usage", "§c!invite <player>", ""))
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
                    respond(formatResponse("Forward", status, ""))
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("reload", "Reload config") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    Config.load()
                    Commands.rebuildDispatcher()
                    respond(formatResponse("Config", "§aReloaded", ""))
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("cd", "Start countdown", "countdown") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("time", StringArgumentType.word())
                    .executes { ctx ->
                        if (Config.settings.countdown) {
                            val timeInput = StringArgumentType.getString(ctx, "time")
                            val seconds = CountdownManager.parseTime(timeInput)
                            if (seconds != null) {
                                CountdownManager.startCountdown(seconds, "Custom")
                            } else {
                                respond(formatResponse("Error", "§cInvalid time! Use: 60, 5m, 1h, 5m30s (max 12h)", ""))
                            }
                        } else {
                            respondDisabled("cd")
                        }
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    if (Config.settings.countdown) {
                        val currentCountdown = CountdownManager.getCurrentCountdown()
                        if (currentCountdown != null) {
                            val remaining = currentCountdown.remainingSeconds
                            val timeStr = formatDuration(remaining)
                            val label = if (currentCountdown.label == "Custom") "" else " (${currentCountdown.label})"
                            respond(formatResponse("Countdown", "§e$timeStr §7remaining$label", ""))
                        } else {
                            respond(formatResponse("Countdown", "§7No active countdown", ""))
                        }
                    } else {
                        respondDisabled("cd")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("clear", "Clear countdown") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    if (Config.settings.countdown) {
                        CountdownManager.clearCountdown()
                    } else {
                        respondDisabled("clear")
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        // Note 命令
        Commands.add(object : Command("note", "Send saved note to party") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.then(Command.argument("message", StringArgumentType.greedyString())
                    .executes { ctx ->
                        val message = StringArgumentType.getString(ctx, "message")
                        Config.settings.note = message
                        Config.save()
                        respond(formatResponse("Note", "§aSaved: §f$message", ""))
                        Command.SINGLE_SUCCESS
                    })
                builder.executes {
                    val note = Config.settings.note.noControlCodes
                    if (note.isNotEmpty()) {
                        if (PartyUtils.isInParty) {
                            sendPartyChat(note)
                            modMessage(formatResponse("Note", "§aSent to party", ""))
                        } else {
                            modMessage(formatResponse("Note", "§cYou are not in a party!", ""))
                        }
                    } else {
                        respond(formatResponse("Note", "§cNo note saved. Use !note <message> to set one.", ""))
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("gui", "Open config GUI") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    ConfigGui.open()
                    modMessage(formatResponse("GUI", "§aOpening config GUI...", ""))
                    Command.SINGLE_SUCCESS
                }
            }
        })

        Commands.add(object : Command("ver", "Show version info", "version") {
            override fun build(builder: LiteralArgumentBuilder<SharedSuggestionProvider>) {
                builder.executes {
                    rawMessage("§b§l===== PartyCommands =====")
                    rawMessage("§eVersion: §a1.1")
                    rawMessage("§eBased on: §aOdin Mod / Meteor Client")
                    rawMessage("§eAuthor: §aAdmin_SR40")
                    rawMessage("§b§l========================")
                    Command.SINGLE_SUCCESS
                }
            }
        })
    }

    /**
     * 格式化持续时间显示（用于倒计时）
     */
    private fun formatDuration(seconds: Int): String {
        return when {
            seconds >= 3600 -> {
                val hours = seconds / 3600
                val mins = (seconds % 3600) / 60
                val secs = seconds % 60
                if (mins > 0 || secs > 0) "${hours}h ${mins}m ${secs}s"
                else "${hours}h"
            }
            seconds >= 60 -> {
                val mins = seconds / 60
                val secs = seconds % 60
                if (secs > 0) "${mins}m ${secs}s" else "${mins}m"
            }
            else -> "${seconds}s"
        }
    }

    /**
     * 发送命令已禁用的提示
     */
    private fun respondDisabled(command: String) {
        respond(formatResponse("Error", "§c!$command is disabled in config.", ""))
    }

    /**
     * 发送响应
     */
    private fun respond(message: String) {
        if (Config.settings.respondInPartyChat && PartyUtils.isInParty) {
            // 移除颜色代码后再发送到聊天
            sendPartyChat("CMD >> " + message.noControlCodes)
        }
        if (Config.settings.showResponseLocally) {
            modMessage("§f$message")
        }
    }

    /**
     * 显示帮助
     */
    private fun showHelp() {
        rawMessage("§b§l===== Available Commands =====")
        rawMessage("§e!help §7- Show this message")
        rawMessage("§e!warp §7- Warp members to this hub")
        rawMessage("§e!allinvite §7- Enable all invite")
        rawMessage("§e!pt <player> §7- Transfer party leader")
        rawMessage("§e!promote <player> §7- Promote member")
        rawMessage("§e!demote <player> §7- Demote member")
        rawMessage("§e!kick <player> §7- Kick member from party")
        rawMessage("§e!kickoffline §7- Kick offline members")
        rawMessage("§e!kickall [players...] §7- Kick all members except specified")
        rawMessage("§e!disband §7- Disband the party")
        rawMessage("§e!leave §7- Leave the party")
        rawMessage("§e!ping §7- Show latency")
        rawMessage("§e!tps §7- Show TPS")
        rawMessage("§e!fps §7- Show FPS")
        rawMessage("§e!time §7- Show current time")
        rawMessage("§e!coords §7- Show coordinates")
        rawMessage("§e!loc §7- Show location")
        rawMessage("§e!hold §7- Show held item")
        rawMessage("§e!status §7- Show party status")
        rawMessage("§e!cd <time> §7- Countdown (60, 5m, 1h)")
        rawMessage("§e!clear §7- Clear countdown")
        rawMessage("§e!fun <cf/8ball/dice/boop/random> §7- Fun commands")
        rawMessage("§e!t1-5 §7- Kuudra")
        rawMessage("§e!f1-7 / m1-7 §7- Dungeon")
        rawMessage("§e!note [message] §7- Send/set note to party")
        rawMessage("§e!boop <player> §7- Boop a player")
        rawMessage("§e!invite <player> §7- Invite player to party")
        rawMessage("§e!forward §7- Toggle party chat forwarding")
        rawMessage("§e!reload §7- Reload config")
        rawMessage("§e!gui §7- Open config GUI")
        rawMessage("§e!ver §7- Show version info")
        rawMessage("§b§l============================")
    }

}
