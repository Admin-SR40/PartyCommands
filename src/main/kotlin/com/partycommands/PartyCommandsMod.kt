package com.partycommands

import com.partycommands.commands.Commands
import com.partycommands.commands.PartyCommandHandler
import com.partycommands.config.Config
import com.partycommands.gui.ConfigGui
import com.partycommands.utils.AutoPartyListUpdater
import com.partycommands.utils.ChatListener
import com.partycommands.utils.CommandKeyBinding
import com.partycommands.utils.PartyListHandler
import com.partycommands.utils.PartyUtils
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.minecraft.network.chat.Component

class PartyCommandsMod : ClientModInitializer {
    
    companion object {
        const val MOD_ID = "partycommands"
        const val PREFIX = "§aCMD §8» §r"
    }
    
    override fun onInitializeClient() {
        Config.load()
        Commands.init()
        PartyCommandHandler.init()
        ChatListener.init()
        AutoPartyListUpdater.init()
        CommandKeyBinding.init()
        
        // 注册 /partycmds 配置命令
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("partycmds")
                    .then(ClientCommandManager.literal("gui")
                        .executes { context ->
                            ConfigGui.open()
                            context.source.sendFeedback(Component.literal("${PREFIX}§aOpening config GUI..."))
                            1
                        }
                    )
                    .then(ClientCommandManager.literal("reload")
                        .executes { context ->
                            Config.load()
                            Commands.rebuildDispatcher()
                            context.source.sendFeedback(Component.literal("${PREFIX}§aConfig reloaded!"))
                            1
                        }
                    )
                    .then(ClientCommandManager.literal("reset")
                        .executes { context ->
                            PartyUtils.reset()
                            context.source.sendFeedback(Component.literal("${PREFIX}§aParty state reset!"))
                            1
                        }
                    )
                    .executes { context ->
                        context.source.sendFeedback(Component.literal("${PREFIX}§7Use /partycmds gui to open config GUI"))
                        context.source.sendFeedback(Component.literal("${PREFIX}§7Use /partycmds reload to reload config"))
                        context.source.sendFeedback(Component.literal("${PREFIX}§7Use /partycmds reset to reset party state"))
                        1
                    }
            )
        }
    }
}
