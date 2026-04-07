package com.partycommands.gui

import com.partycommands.commands.Commands
import com.partycommands.config.Config
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.isxander.yacl3.api.controller.StringControllerBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

object ConfigGui {
    
    fun createScreen(parent: Screen?): Screen {
        return YetAnotherConfigLib.createBuilder()
            .title(Component.literal("PartyCommands"))
            .save(this::saveConfig)
            .category(createMainCategory())
            .build()
            .generateScreen(parent)
    }
    
    private fun saveConfig() {
        Config.save()
        Commands.rebuildDispatcher()
    }
    
    private fun createMainCategory(): ConfigCategory {
        return ConfigCategory.createBuilder()
            .name(Component.literal("PartyCommands"))
            .tooltip(Component.literal("PartyCommands configuration"))
            // Basic Settings
            .group(createBasicSettingsGroup())
            // Response Settings
            .group(createResponseGroup())
            // Party Management
            .group(createToggleGroup("Party Management", "Warp, invite, kick, promote, demote, transfer, disband, leave", mapOf(
                "!warp" to Binding({ Config.settings.warp }, { Config.settings.warp = it }),
                "!allinvite" to Binding({ Config.settings.allinvite }, { Config.settings.allinvite = it }),
                "!kick" to Binding({ Config.settings.kick }, { Config.settings.kick = it }),
                "!kickoffline" to Binding({ Config.settings.kickoffline }, { Config.settings.kickoffline = it }),
                "!kickall" to Binding({ Config.settings.kickall }, { Config.settings.kickall = it }),
                "!promote" to Binding({ Config.settings.promote }, { Config.settings.promote = it }),
                "!demote" to Binding({ Config.settings.demote }, { Config.settings.demote = it }),
                "!transfer" to Binding({ Config.settings.transfer }, { Config.settings.transfer = it }),
                "!disband" to Binding({ Config.settings.disband }, { Config.settings.disband = it }),
                "!invite" to Binding({ Config.settings.invite }, { Config.settings.invite = it }),
                "!leave" to Binding({ Config.settings.leave }, { Config.settings.leave = it })
            )))
            // Queue Commands
            .group(createToggleGroup("Queue Commands", "Dungeon and Kuudra queue", mapOf(
                "!f1-f7 / !m1-m7 / !t1-t5" to Binding({ Config.settings.queueInstance }, { Config.settings.queueInstance = it })
            )))
            // Info Commands
            .group(createToggleGroup("Info Commands", "Ping, TPS, FPS, time, location, coords, holding, status, countdown", mapOf(
                "!ping" to Binding({ Config.settings.ping }, { Config.settings.ping = it }),
                "!tps" to Binding({ Config.settings.tps }, { Config.settings.tps = it }),
                "!fps" to Binding({ Config.settings.fps }, { Config.settings.fps = it }),
                "!time" to Binding({ Config.settings.time }, { Config.settings.time = it }),
                "!location" to Binding({ Config.settings.location }, { Config.settings.location = it }),
                "!coords" to Binding({ Config.settings.coords }, { Config.settings.coords = it }),
                "!holding" to Binding({ Config.settings.holding }, { Config.settings.holding = it }),
                "!status" to Binding({ Config.settings.status }, { Config.settings.status = it }),
                "!cd (Countdown)" to Binding({ Config.settings.countdown }, { Config.settings.countdown = it })
            )))
            // Fun Commands
            .group(createToggleGroup("Fun Commands", "cf, 8ball, dice, boop, random", mapOf(
                "!fun cf" to Binding({ Config.settings.coinflip }, { Config.settings.coinflip = it }),
                "!fun 8ball" to Binding({ Config.settings.eightball }, { Config.settings.eightball = it }),
                "!fun dice" to Binding({ Config.settings.dice }, { Config.settings.dice = it }),
                "!fun boop" to Binding({ Config.settings.boop }, { Config.settings.boop = it })
            )))
            // Note & Sound
            .group(createNoteGroup())
            .build()
    }
    
    private fun createBasicSettingsGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.literal("Basic Settings"))
            .description(OptionDescription.of(Component.literal("General mod settings")))
            .collapsed(false)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal("Mod Enabled"))
                    .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("Master toggle for the mod")))
                    .binding(true, { Config.settings.enabled }, { Config.settings.enabled = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.literal("Command Prefix"))
                    .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("Prefix for party commands (default: !)")))
                    .binding("!", { Config.settings.prefix }, { Config.settings.prefix = it })
                    .controller(StringControllerBuilder::create)
                    .build()
            )
            .build()
    }
    
    private fun createResponseGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.literal("Response Settings"))
            .description(OptionDescription.of(Component.literal("Where to show command responses")))
            .collapsed(false)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal("Respond in Party Chat"))
                    .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("Send command responses to party chat")))
                    .binding(true, { Config.settings.respondInPartyChat }, { Config.settings.respondInPartyChat = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal("Show Response Locally"))
                    .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("Show command responses in your own chat HUD")))
                    .binding(true, { Config.settings.showResponseLocally }, { Config.settings.showResponseLocally = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal("Remove Separator Lines"))
                    .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("Hide decorative separator lines (---) from Hypixel chat")))
                    .binding(true, { Config.settings.removeSeparator }, { Config.settings.removeSeparator = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }
    
    private fun createToggleGroup(name: String, desc: String, toggles: Map<String, Binding<Boolean>>): OptionGroup {
        val groupBuilder = OptionGroup.createBuilder()
            .name(Component.literal(name))
            .description(OptionDescription.of(Component.literal(desc)))
            .collapsed(true)
        
        toggles.forEach { (commandName, binding) ->
            groupBuilder.option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal(commandName))
                    .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("Enable $commandName command")))
                    .binding(true, binding.getter, binding.setter)
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
        }
        
        return groupBuilder.build()
    }
    
    private fun createNoteGroup(): OptionGroup {
        return OptionGroup.createBuilder()
            .name(Component.literal("Note & Sound"))
            .description(OptionDescription.of(Component.literal("Note message and countdown sound settings")))
            .collapsed(true)
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<String>()
                    .name(Component.literal("Note Message"))
                    .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("Message to send when using !note (use !note <msg> to set in-game)")))
                    .binding("", { Config.settings.note }, { Config.settings.note = it })
                    .controller(StringControllerBuilder::create)
                    .build()
            )
            .option(
                dev.isxander.yacl3.api.Option.createBuilder<Boolean>()
                    .name(Component.literal("Countdown Sound"))
                    .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("Play sound on countdown reminders")))
                    .binding(true, { Config.settings.countdownSound }, { Config.settings.countdownSound = it })
                    .controller(TickBoxControllerBuilder::create)
                    .build()
            )
            .build()
    }
    
    data class Binding<T>(val getter: () -> T, val setter: (T) -> Unit)
    
    fun open() {
        val mc = Minecraft.getInstance()
        // 延迟到下一 tick 执行，避免在命令执行期间打开 GUI 导致问题
        Thread {
            Thread.sleep(50) // 50ms ≈ 1 tick
            mc.execute {
                mc.setScreen(createScreen(mc.screen))
            }
        }.start()
    }
}
