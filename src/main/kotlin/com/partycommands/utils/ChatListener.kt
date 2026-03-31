package com.partycommands.utils

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component

object ChatListener {
    
    // Hypixel 聊天正则表达式
    private val joinedSelf = Regex("^You have joined ((?:\\[[^]]*?])? ?)?(\\w{1,16})'s? party!$")
    private val joinedOther = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) joined the party\\.$")
    private val leftParty = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has left the party\\.$")
    private val kickedParty = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has been removed from the party\\.$")
    private val kickedOffline = Regex("^Kicked ((?:\\[[^]]*?])? ?)?(\\w{1,16}) because they were offline\\.$")
    private val kickedDisconnected = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) was removed from your party because they disconnected\\.$")
    private val transferLeave = Regex("^The party was transferred to ((?:\\[[^]]*?])? ?)?(\\w{1,16}) because ((?:\\[[^]]*?])? ?)?(\\w{1,16}) left$")
    private val transferBy = Regex("^The party was transferred to ((?:\\[[^]]*?])? ?)?(\\w{1,16}) by ((?:\\[[^]]*?])? ?)?(\\w{1,16})$")
    private val partyInvite = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) invited ((?:\\[[^]]*?])? ?)?(\\w{1,16}) to the party! They have 60 seconds to accept.$")
    private val leaderDisconnected = Regex("^The party leader, ((?:\\[[^]]*?])? ?)?(\\w{1,16}) has disconnected, they have 5 minutes to rejoin before the party is disbanded\\.$")
    private val leaderRejoined = Regex("^The party leader ((?:\\[[^]]*?])? ?)?(\\w{1,16}) has rejoined\\.$")
    private val memberFormat = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16})$")
    private val membersList = Regex("^Party (Leader|Moderators|Members): (.+)$")
    private val dungeonJoin = Regex("^Party Finder > (\\w{1,16}) joined the dungeon group! ")
    private val kuudraJoin = Regex("^Party Finder > ((?:\\[[^]]*?])? ?)?(\\w{1,16}) joined the group!")
    
    private val disbandPatterns = listOf(
        Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has disbanded the party!$"),
        Regex("^You have been kicked from the party by ((?:\\[[^]]*?])? ?)?(\\w{1,16})$"),
        Regex("^The party was disbanded because all invites expired and the party was empty.$"),
        Regex("^The party was disbanded because the party leader disconnected.$"),
        Regex("^You left the party.$"),
        Regex("^You are not currently in a party.$")
    )
    
    fun init() {
        ClientReceiveMessageEvents.GAME.register { message: Component, _: Boolean ->
            val text = message.string
            handleMessage(text)
        }
    }
    
    private fun handleMessage(message: String) {
        // 有人加入队伍
        joinedOther.find(message)?.let {
            PartyUtils.addMember(it.groupValues[2])
            return
        }
        
        // 自己加入队伍
        joinedSelf.find(message)?.let {
            PartyUtils.addMember(it.groupValues[2])
            PartyUtils.partyLeader = it.groupValues[2]
            PartyUtils.addMember(mc.player?.name?.string ?: return)
            return
        }
        
        // 有人离开
        leftParty.find(message)?.let {
            PartyUtils.removeMember(it.groupValues[2])
            return
        }
        
        // 被踢出
        kickedParty.find(message)?.let {
            PartyUtils.removeMember(it.groupValues[2])
            return
        }
        
        // 离线被踢
        kickedOffline.find(message)?.let {
            PartyUtils.removeMember(it.groupValues[2])
            return
        }
        
        // 断开连接被移除
        kickedDisconnected.find(message)?.let {
            PartyUtils.removeMember(it.groupValues[2])
            return
        }
        
        // 队长转让（主动）
        transferBy.find(message)?.let {
            PartyUtils.addMember(it.groupValues[2])
            PartyUtils.addMember(it.groupValues[4])
            PartyUtils.partyLeader = it.groupValues[2]
            return
        }
        
        // 队长转让（离开后）
        transferLeave.find(message)?.let {
            PartyUtils.addMember(it.groupValues[2])
            PartyUtils.partyLeader = it.groupValues[2]
            PartyUtils.removeMember(it.groupValues[4])
            return
        }
        
        // 队长断开连接
        leaderDisconnected.find(message)?.let {
            PartyUtils.partyLeader = it.groupValues[2]
            return
        }
        
        // 队长重新加入
        leaderRejoined.find(message)?.let {
            PartyUtils.partyLeader = it.groupValues[2]
            return
        }
        
        // 邀请
        partyInvite.find(message)?.let {
            PartyUtils.addMember(it.groupValues[2])
            if (PartyUtils.partyLeader == null) PartyUtils.partyLeader = it.groupValues[2]
            return
        }
        
        // 队伍解散检测
        for (pattern in disbandPatterns) {
            if (pattern.containsMatchIn(message)) {
                PartyUtils.disband()
                return
            }
        }
        
        // 成员列表
        membersList.find(message)?.let { match ->
            val type = match.groupValues[1]
            match.groupValues[2].split(" ●").forEach { segment ->
                val memberMatch = memberFormat.find(segment.trim()) ?: return@forEach
                PartyUtils.addMember(memberMatch.groupValues[2])
                if (type == "Leader") PartyUtils.partyLeader = memberMatch.groupValues[2]
            }
            return
        }
        
        // 地牢加入
        dungeonJoin.find(message)?.let {
            PartyUtils.addMember(it.groupValues[1])
            return
        }
        
        // Kuudra 加入
        kuudraJoin.find(message)?.let {
            PartyUtils.addMember(it.groupValues[2])
            return
        }
    }
}
