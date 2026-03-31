package com.partycommands.utils

import net.minecraft.client.Minecraft

object PartyUtils {
    private val mc = Minecraft.getInstance()
    
    // 队伍成员列表
    val members = mutableListOf<String>()
    
    // 队长名称
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
     */
    fun addMember(playerName: String) {
        if (!isInParty) isInParty = true
        if (playerName !in members) {
            members.add(playerName)
        }
    }
    
    /**
     * 移除成员
     */
    fun removeMember(playerName: String) {
        if (playerName !in members) return
        members.remove(playerName)
        if (members.isEmpty()) {
            disband()
        }
    }
    
    /**
     * 解散队伍
     */
    fun disband() {
        members.clear()
        partyLeader = null
        isInParty = false
    }
    
    /**
     * 根据部分名称查找队员
     */
    fun findMember(partialName: String): String {
        return members.find { it.contains(partialName, ignoreCase = true) } ?: partialName
    }
    
    /**
     * 重置队伍状态
     */
    fun reset() {
        disband()
    }
}
