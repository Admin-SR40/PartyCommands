package com.partycommands.utils

import net.minecraft.client.Minecraft

object PartyUtils {
    private val mc = Minecraft.getInstance()
    
    // 队伍成员列表（纯净名字，用于匹配）
    val members = mutableListOf<String>()
    
    // 成员名字到带颜色名字的映射
    private val memberColors = mutableMapOf<String, String>()
    
    // 掉线成员列表（纯净名字）
    private val offlineMembers = mutableSetOf<String>()
    
    // 队长名称（纯净名字）
    var partyLeader: String? = null
        internal set
    
    // 是否在队伍中
    var isInParty: Boolean = false
        internal set
    
    /**
     * 检查当前玩家是否是队长
     */
    fun isLeader(): Boolean {
        return partyLeader == mc.player?.name?.string
    }
    
    /**
     * 添加成员
     * @param playerName 玩家名（会自动清理颜色代码、rank 和 ●）
     */
    fun addMember(playerName: String, coloredName: String? = null) {
        if (!isInParty) isInParty = true
        // 清理颜色代码、rank 和 ● 符号
        val cleanName = playerName.noControlCodes
            .replace("[MVP++]", "")
            .replace("[MVP+]", "")
            .replace("[MVP]", "")
            .replace("[VIP+]", "")
            .replace("[VIP]", "")
            .replace("[YOUTUBE]", "")
            .replace("[ADMIN]", "")
            .replace("[GM]", "")
            .replace("[MOD]", "")
            .replace("[HELPER]", "")
            .replace("●", "")
            .trim()
        if (cleanName.isEmpty()) return
        if (cleanName !in members) {
            members.add(cleanName)
        }
        // 保存带颜色的名字
        if (coloredName != null) {
            memberColors[cleanName] = coloredName
        }
    }
    
    /**
     * 移除成员
     */
    fun removeMember(playerName: String) {
        val cleanName = playerName.noControlCodes
        if (cleanName !in members) return
        members.remove(cleanName)
        memberColors.remove(cleanName)
        if (members.isEmpty()) {
            disband()
        }
    }
    
    /**
     * 解散队伍
     */
    fun disband() {
        members.clear()
        memberColors.clear()
        offlineMembers.clear()
        partyLeader = null
        isInParty = false
    }
    
    /**
     * 标记成员为掉线状态
     */
    fun markOffline(playerName: String) {
        val cleanName = playerName.noControlCodes.lowercase()
        offlineMembers.add(cleanName)
    }
    
    /**
     * 标记成员为在线状态（重连）
     */
    fun markOnline(playerName: String) {
        val cleanName = playerName.noControlCodes.lowercase()
        offlineMembers.remove(cleanName)
    }
    
    /**
     * 检查成员是否掉线
     */
    fun isOffline(playerName: String): Boolean {
        return offlineMembers.contains(playerName.noControlCodes.lowercase())
    }
    
    /**
     * 移除成员时同时清除掉线状态
     */
    fun removeMemberWithOffline(playerName: String) {
        val cleanName = playerName.noControlCodes
        members.remove(cleanName)
        memberColors.remove(cleanName)
        offlineMembers.remove(cleanName)
        if (members.isEmpty()) {
            disband()
        }
    }
    
    /**
     * 根据部分名称查找队员
     */
    fun findMember(partialName: String): String {
        return members.find { it.contains(partialName, ignoreCase = true) } ?: partialName
    }
    
    /**
     * 获取带颜色的成员名字
     */
    fun getMemberWithColor(cleanName: String): String {
        return memberColors[cleanName] ?: "§7$cleanName"
    }
    
    /**
     * 重置队伍状态
     */
    fun reset() {
        disband()
    }
}
