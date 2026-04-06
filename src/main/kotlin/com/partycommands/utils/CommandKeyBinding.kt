package com.partycommands.utils

import com.partycommands.config.Config
import com.partycommands.gui.ConfigGui
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.resources.Identifier
import com.mojang.blaze3d.platform.InputConstants
import org.lwjgl.glfw.GLFW

/**
 * 命令快捷键绑定
 * 按 ` 键直接打开聊天栏并自动填充命令前缀
 */
object CommandKeyBinding {
    
    private lateinit var commandKey: KeyMapping
    private lateinit var guiKey: KeyMapping
    private lateinit var toggleKey: KeyMapping
    
    // 创建 Party Commands 专用分类
    private val CATEGORY_PARTY_COMMANDS = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("partycommands", "general")
    )
    
    fun init() {
        // 注册 ` 键 (Grave Accent / Tilde key) - 打开命令聊天
        commandKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "key.partycommands.open",  // 翻译键
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,  // ` 键
                CATEGORY_PARTY_COMMANDS  // 使用 Party Commands 专用分类
            )
        )
        
        // 注册打开 GUI 的按键 - 默认不绑定
        guiKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "key.partycommands.gui",  // 翻译键
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,  // 默认不绑定
                CATEGORY_PARTY_COMMANDS
            )
        )
        
        // 注册开关 Mod 的按键 - 默认不绑定
        toggleKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "key.partycommands.toggle",  // 翻译键
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,  // 默认不绑定
                CATEGORY_PARTY_COMMANDS
            )
        )
        
        // 注册 tick 事件监听按键
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            // 打开命令聊天
            while (commandKey.consumeClick()) {
                val prefix = Config.settings.prefix
                client.setScreen(ChatScreen(prefix, false))
            }
            
            // 打开 GUI
            while (guiKey.consumeClick()) {
                ConfigGui.open()
                modMessage("§aOpening config GUI...")
            }
            
            // 开关 Mod
            while (toggleKey.consumeClick()) {
                Config.settings.enabled = !Config.settings.enabled
                Config.save()
                val status = if (Config.settings.enabled) "§aenabled" else "§cdisabled"
                modMessage("§eParty Commands §7is now $status")
            }
        }
    }
}
