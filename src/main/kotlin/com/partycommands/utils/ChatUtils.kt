package com.partycommands.utils

import com.partycommands.PartyCommandsMod
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

val mc: Minecraft
    get() = Minecraft.getInstance()

/**
 * 在本地显示 Mod 消息（带前缀）
 */
fun modMessage(message: String) {
    mc.execute {
        mc.gui.chat.addMessage(Component.literal(PartyCommandsMod.PREFIX + message))
    }
}

/**
 * 在本地显示原始消息（不带前缀）
 */
fun rawMessage(message: String) {
    mc.execute {
        mc.gui.chat.addMessage(Component.literal(message))
    }
}

/**
 * 发送到队伍聊天
 */
fun sendPartyChat(message: String) {
    mc.execute {
        mc.player?.connection?.sendCommand("pc $message")
    }
}

/**
 * 发送普通聊天消息
 */
fun sendChatMessage(message: String) {
    mc.execute {
        mc.player?.connection?.sendChat(message)
    }
}

/**
 * 发送命令
 */
fun sendCommand(command: String) {
    mc.execute {
        mc.player?.connection?.sendCommand(command)
    }
}

/**
 * 获取当前坐标字符串
 */
fun getPositionString(): String {
    val player = mc.player ?: return "未知位置"
    return "x: ${player.blockPosition().x}, y: ${player.blockPosition().y}, z: ${player.blockPosition().z}"
}

/**
 * 格式化数字为固定小数位
 */
fun Double.toFixed(decimals: Int = 1): String = String.format("%.${decimals}f", this)

/**
 * 移除颜色代码
 */
val String.noControlCodes: String
    get() = this.replace(Regex("§[0-9a-fk-or]"), "")
