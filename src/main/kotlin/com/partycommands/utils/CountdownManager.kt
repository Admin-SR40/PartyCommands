package com.partycommands.utils

import com.partycommands.config.Config
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import kotlin.concurrent.thread

object CountdownManager {
    private val mc = Minecraft.getInstance()
    
    // 当前倒计时
    private var currentCountdown: Countdown? = null
    
    // 地牢楼层映射
    private val floorInstances = mapOf(
        "F1" to "catacombs_floor_one",
        "F2" to "catacombs_floor_two",
        "F3" to "catacombs_floor_three",
        "F4" to "catacombs_floor_four",
        "F5" to "catacombs_floor_five",
        "F6" to "catacombs_floor_six",
        "F7" to "catacombs_floor_seven",
        "M1" to "master_catacombs_floor_one",
        "M2" to "master_catacombs_floor_two",
        "M3" to "master_catacombs_floor_three",
        "M4" to "master_catacombs_floor_four",
        "M5" to "master_catacombs_floor_five",
        "M6" to "master_catacombs_floor_six",
        "M7" to "master_catacombs_floor_seven",
        "T1" to "kuudra_normal",
        "T2" to "kuudra_hot",
        "T3" to "kuudra_burning",
        "T4" to "kuudra_fiery",
        "T5" to "kuudra_infernal"
    )
    
    data class Countdown(
        val totalSeconds: Int,
        val label: String,  // 如 "F7", "M7", "Custom"
        var remainingSeconds: Int
    )
    
    /**
     * 开始新的倒计时，会覆盖旧的
     */
    fun startCountdown(seconds: Int, label: String = "Custom"): Boolean {
        if (seconds <= 0) return false
        
        currentCountdown = Countdown(seconds, label, seconds)
        
        // 发送开始消息
        val timeStr = formatTime(seconds)
        val displayLabel = if (label == "Custom") "Custom" else label
        
        if (label == "Custom") {
            // 自定义倒计时
            if (PartyUtils.isInParty) {
                sendPartyChat("Countdown - $displayLabel - Started: $timeStr")
            } else {
                modMessage(formatResponse("Countdown", "§aStarted: $timeStr ($displayLabel)", ""))
            }
        } else {
            // 地牢倒计时：更友好的提示
            if (PartyUtils.isInParty) {
                sendPartyChat("Queued for $displayLabel - entering in $timeStr")
                // 延迟 0.5 秒发送取消提示
                thread {
                    Thread.sleep(500)
                    sendPartyChat("Type !cancel to abort the queue")
                }
            } else {
                modMessage(formatResponse("Countdown", "§aStarted: $timeStr ($displayLabel)", ""))
                modMessage("§7Type §c!clear §7to stop the timer")
            }
        }
        
        return true
    }
    
    /**
     * 清除当前倒计时
     */
    fun clearCountdown() {
        if (currentCountdown != null) {
            currentCountdown = null
            modMessage(formatResponse("Countdown", "§cCleared", ""))
        } else {
            modMessage(formatResponse("Countdown", "§7No active countdown", ""))
        }
    }
    
    /**
     * 每 tick 调用
     */
    fun onTick() {
        val countdown = currentCountdown ?: return
        
        // 每秒更新一次（20 ticks = 1 second）
        if (mc.player?.tickCount?.rem(20) != 0) return
        
        countdown.remainingSeconds--
        
        if (countdown.remainingSeconds <= 0) {
            // 倒计时结束
            val endMessage = if (countdown.label == "Custom") "Custom" else countdown.label
            playLevelUpSound()
            if (PartyUtils.isInParty) {
                sendPartyChat("Countdown - $endMessage - Time's up!")
            } else {
                modMessage(formatResponse("Countdown", "§a§lTime's up! (${endMessage})", ""))
            }
            
            // 地牢倒计时结束后自动队列
            if (countdown.label != "Custom") {
                val instanceId = floorInstances[countdown.label]
                if (instanceId != null) {
                    sendCommand("joininstance $instanceId")
                    modMessage(formatResponse("Queue", "§eAuto-joining ${countdown.label}...", ""))
                }
            }
            
            currentCountdown = null
            return
        }
        
        // 检查是否需要发送提醒
        val remaining = countdown.remainingSeconds
        val total = countdown.totalSeconds
        
        when {
            // 最后5秒，每秒提醒
            remaining <= 5 -> sendReminder(countdown)
            // 10秒时提醒一次
            remaining == 10 -> sendReminder(countdown)
            // 能被30整除时提醒（适用于地牢倒计时）
            countdown.label != "Custom" && remaining % 30 == 0 -> sendReminder(countdown)
            // 自定义倒计时的分级提醒
            countdown.label == "Custom" -> checkCustomReminder(countdown)
        }
    }
    
    /**
     * 发送提醒
     */
    private fun sendReminder(countdown: Countdown) {
        val timeStr = formatTime(countdown.remainingSeconds)
        
        // 播放提示音（只在本地）
        playCountdownSound()
        
        if (countdown.label == "Custom") {
            // 自定义倒计时：不在队伍用 modMessage，在队伍用 /pc
            if (PartyUtils.isInParty) {
                sendPartyChat("Countdown - Custom - $timeStr remaining")
            } else {
                modMessage(formatResponse("Countdown", "§e$timeStr §7remaining", ""))
            }
        } else {
            // 地牢倒计时
            val color = when {
                countdown.remainingSeconds <= 5 -> "§c"
                countdown.remainingSeconds <= 10 -> "§6"
                else -> "§e"
            }
            if (PartyUtils.isInParty) {
                sendPartyChat("Countdown - ${countdown.label} - $timeStr remaining")
            } else {
                modMessage(formatResponse("Countdown", "${color}$timeStr §7remaining (${countdown.label})", ""))
            }
        }
    }
    
    /**
     * 播放倒计时提示音（只在本地）
     */
    private fun playCountdownSound() {
        if (!Config.settings.countdownSound) return
        
        mc.execute {
            mc.soundManager.play(
                SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
            )
        }
    }
    
    /**
     * 播放倒计时结束音效（只在本地）
     */
    private fun playLevelUpSound() {
        if (!Config.settings.countdownSound) return
        
        mc.execute {
            mc.soundManager.play(
                SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f)
            )
        }
    }
    
    /**
     * 检查自定义倒计时的分级提醒
     */
    private fun checkCustomReminder(countdown: Countdown) {
        val remaining = countdown.remainingSeconds
        val total = countdown.totalSeconds
        
        // 最后10秒已经在 sendReminder 中处理
        if (remaining <= 10) return
        
        val shouldRemind = when {
            // 超过2小时：每1小时提醒
            remaining > 7200 -> remaining % 3600 == 0
            // 1-2小时：每30分钟
            remaining > 3600 -> remaining % 1800 == 0
            // 30-60分钟：每20分钟
            remaining > 1800 -> remaining % 1200 == 0
            // 10-30分钟：每10分钟
            remaining > 600 -> remaining % 600 == 0
            // 2-10分钟：每5分钟
            remaining > 120 -> remaining % 300 == 0
            // 0-2分钟：每30秒
            else -> remaining % 30 == 0
        }
        
        if (shouldRemind) {
            val timeStr = formatTime(remaining)
            playCountdownSound()
            if (PartyUtils.isInParty) {
                sendPartyChat("Countdown - Custom - $timeStr remaining")
            } else {
                modMessage(formatResponse("Countdown", "§e$timeStr §7remaining", ""))
            }
        }
    }
    
    /**
     * 格式化时间显示
     */
    private fun formatTime(seconds: Int): String {
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
     * 解析时间字符串
     * 支持格式：
     * - 纯数字：60 = 60秒
     * - 单一单位：5m = 5分钟, 5h = 5小时, 30s = 30秒
     * - 复合单位：5m30s, 1h30m, 1h30m45s
     * 最大12小时 (43200秒)
     */
    fun parseTime(input: String): Int? {
        val trimmed = input.trim().lowercase().replace(" ", "")
        
        if (trimmed.isEmpty()) return null
        
        // 纯数字，默认秒
        if (trimmed.all { it.isDigit() }) {
            val secs = trimmed.toIntOrNull() ?: return null
            return if (secs > 43200) null else secs
        }
        
        // 解析复合格式 (如 5m30s, 1h30m)
        var totalSeconds = 0
        var currentNumber = StringBuilder()
        var hasUnit = false
        
        for (char in trimmed) {
            when (char) {
                'h', 'm', 's' -> {
                    if (currentNumber.isEmpty()) return null // 单位前必须有数字
                    val value = currentNumber.toString().toIntOrNull() ?: return null
                    if (value < 0) return null
                    
                    totalSeconds += when (char) {
                        'h' -> value * 3600
                        'm' -> value * 60
                        's' -> value
                        else -> 0
                    }
                    currentNumber = StringBuilder()
                    hasUnit = true
                }
                in '0'..'9' -> currentNumber.append(char)
                else -> return null // 非法字符
            }
        }
        
        // 如果最后还有数字但没有单位，返回错误（或者可以默认当作秒）
        if (currentNumber.isNotEmpty()) {
            // 允许末尾是纯数字（当作秒），例如 "5m30"
            val value = currentNumber.toString().toIntOrNull() ?: return null
            if (value < 0) return null
            totalSeconds += value
        }
        
        // 必须至少有一个单位，或者纯数字已在上面处理
        if (!hasUnit && currentNumber.isEmpty()) return null
        
        return if (totalSeconds > 43200 || totalSeconds <= 0) null else totalSeconds
    }
    
    /**
     * 获取当前倒计时状态
     */
    fun getCurrentCountdown(): Countdown? = currentCountdown
    
    /**
     * 尝试通过队伍聊天取消地牢倒计时
     * 只有地牢倒计时可以被队友取消，Custom 不行
     * @param playerName 发送 !cancel 的玩家名
     * @return 是否成功取消
     */
    fun tryCancelFromPartyChat(playerName: String): Boolean {
        val countdown = currentCountdown ?: return false
        
        // 只有地牢倒计时可以被取消
        if (countdown.label == "Custom") return false
        
        // 取消倒计时
        currentCountdown = null
        
        // 发送取消消息到队伍聊天（使用 & 代替 §，Hypixel 会自动转换）
        sendPartyChat("Countdown cancelled by $playerName")
        
        return true
    }
}
