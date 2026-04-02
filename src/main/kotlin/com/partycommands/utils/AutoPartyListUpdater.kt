package com.partycommands.utils

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft

/**
 * 自动更新队伍状态
 * 只在首次加入服务器时执行 /p list，代理切换不触发
 */
object AutoPartyListUpdater {
    private val mc = Minecraft.getInstance()
    
    // 是否曾经在游戏中（用于检测从主菜单进入）
    private var wasInGame = false
    
    // 是否已经执行过首次更新
    private var hasDoneFirstUpdate = false
    
    // 上次更新时间
    private var lastUpdateTime = 0L
    private const val UPDATE_COOLDOWN = 60000L // 1分钟冷却
    
    fun init() {
        // 使用 tick 事件检测游戏状态变化
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            checkGameState()
        }
    }
    
    /**
     * 检测游戏状态变化
     */
    private fun checkGameState() {
        val isInGame = mc.player != null && mc.connection != null
        
        // 从主菜单进入游戏（wasInGame = false -> isInGame = true）
        if (!wasInGame && isInGame && !hasDoneFirstUpdate) {
            scheduleUpdate()
            hasDoneFirstUpdate = true
        }
        
        // 如果断开连接（返回主菜单），重置标志
        if (wasInGame && !isInGame) {
            hasDoneFirstUpdate = false
        }
        
        wasInGame = isInGame
    }
    
    /**
     * 安排一次自动更新（带冷却）
     */
    private fun scheduleUpdate() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < UPDATE_COOLDOWN) return
        lastUpdateTime = currentTime
        
        // 延迟执行，确保连接稳定
        Thread {
            Thread.sleep(1000)
            mc.execute {
                if (mc.player != null) {
                    PartyListHandler.startAutoWaiting()
                    sendCommand("p list")
                }
            }
        }.start()
    }
}
