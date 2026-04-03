package com.partycommands.utils

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft

/**
 * 自动更新队伍状态
 * 只在首次加入服务器时执行 /p list，单人世界不执行
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
            // 延迟一点执行，确保服务器信息已加载
            Thread {
                Thread.sleep(500)
                mc.execute {
                    if (shouldUpdate()) {
                        scheduleUpdate()
                    }
                }
            }.start()
            hasDoneFirstUpdate = true
        }
        
        // 如果断开连接（返回主菜单），重置标志
        if (wasInGame && !isInGame) {
            hasDoneFirstUpdate = false
        }
        
        wasInGame = isInGame
    }
    
    /**
     * 检查是否应该更新（非单人世界且是多人服务器）
     */
    private fun shouldUpdate(): Boolean {
        // 检查是否在单人世界
        if (mc.isSingleplayer) return false
        
        // 检查是否有连接
        val connection = mc.connection ?: return false
        
        return true
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
            Thread.sleep(1500)
            mc.execute {
                if (mc.player != null && !mc.isSingleplayer) {
                    PartyListHandler.startAutoWaiting()
                    sendCommand("p list")
                }
            }
        }.start()
    }
    
    /**
     * 手动触发更新（用于切换大厅等场景）
     */
    fun refresh() {
        if (!mc.isSingleplayer && mc.player != null) {
            scheduleUpdate()
        }
    }
}
