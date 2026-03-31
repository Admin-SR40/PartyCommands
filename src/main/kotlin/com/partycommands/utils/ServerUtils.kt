package com.partycommands.utils

import net.minecraft.client.Minecraft
import net.minecraft.Util
import kotlin.math.min
import kotlin.math.roundToInt

object ServerUtils {
    private val mc = Minecraft.getInstance()
    
    // TPS 计算
    private val tpsHistory = mutableListOf<Double>()
    private var lastTime = System.currentTimeMillis()
    private var lastTick = 0L
    
    /**
     * 当前延迟 (ms)
     */
    var currentPing: Int = 0
        private set
    
    /**
     * 平均延迟 (ms)
     */
    var averagePing: Int = 0
        private set
    
    /**
     * 当前 FPS
     */
    val currentFps: Int
        get() = mc.fps
    
    /**
     * 平均 TPS (估计值)
     * 注意：客户端无法直接获取服务器真实 TPS，这是基于 tick 的估计
     */
    val averageTps: Double
        get() {
            return if (tpsHistory.isNotEmpty()) {
                tpsHistory.average()
            } else {
                20.0
            }
        }
    
    /**
     * 更新 TPS 计算（应在 tick 事件中调用）
     */
    fun updateTps() {
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastTime
        
        if (elapsed >= 1000) {
            val ticks = mc.player?.tickCount?.toLong() ?: 0L
            val tickDiff = ticks - lastTick
            val tps = (tickDiff * 1000.0 / elapsed).coerceAtMost(20.0)
            
            tpsHistory.add(tps)
            if (tpsHistory.size > 10) {
                tpsHistory.removeAt(0)
            }
            
            lastTime = currentTime
            lastTick = ticks
        }
    }
    
    /**
     * 处理 Pong 响应包（由 Mixin 调用）
     */
    @JvmStatic
    fun onPongResponse(time: Long) {
        currentPing = (Util.getMillis() - time).toInt().coerceAtLeast(0)
        
        val pingLog = mc.gui.debugOverlay.pingLogger
        val sampleSize = min(pingLog.size(), 20)
        
        if (sampleSize == 0) {
            averagePing = currentPing
            return
        }
        
        var total = 0L
        for (i in 0 until sampleSize) {
            total += pingLog.get(i)
        }
        
        averagePing = (total / sampleSize).toInt()
    }
}
