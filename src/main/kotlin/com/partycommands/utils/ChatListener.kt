package com.partycommands.utils

import com.partycommands.config.Config
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component

object ChatListener {
    
    // Hypixel 聊天正则表达式
    private val joinedSelf = Regex("^You have joined ((?:\\[[^]]*?])? ?)?(\\w{1,16})'s? party!$")
    private val joinedOther = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) joined the party\\.$")
    private val joinedLobby = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) joined the lobby!$")
    private val leftParty = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has left the party\\.$")
    private val kickedParty = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has been removed from the party\\.$")
    private val kickedOffline = Regex("^Kicked ((?:\\[[^]]*?])? ?)?(\\w{1,16}) because they were offline\\.$")
    private val kickedDisconnected = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) was removed from your party because they disconnected\\.$")
    private val transferLeave = Regex("^The party was transferred to ((?:\\[[^]]*?])? ?)?(\\w{1,16}) because ((?:\\[[^]]*?])? ?)?(\\w{1,16}) left$")
    private val transferBy = Regex("^The party was transferred to ((?:\\[[^]]*?])? ?)?(\\w{1,16}) by ((?:\\[[^]]*?])? ?)?(\\w{1,16})$")
    private val partyInvite = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) invited ((?:\\[[^]]*?])? ?)?(\\w{1,16}) to the party! They have 60 seconds to accept.$")
    private val leaderDisconnected = Regex("^The party leader, ((?:\\[[^]]*?])? ?)?(\\w{1,16}) has disconnected, they have 5 minutes to rejoin before the party is disbanded\\.$")
    private val leaderRejoined = Regex("^The party leader ((?:\\[[^]]*?])? ?)?(\\w{1,16}) has rejoined\\.$")
    private val memberDisconnected = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has disconnected, they have 5 minutes to rejoin before they are removed from the party\\.$")
    private val memberRejoined = Regex("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has rejoined\\.$")
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
        
        // 加入大厅（切换大厅时刷新队伍状态）
        joinedLobby.find(message)?.let {
            val playerName = it.groupValues[2]
            val myName = mc.player?.name?.string
            // 如果是自己加入大厅，刷新队伍状态
            if (myName != null && playerName.equals(myName, ignoreCase = true)) {
                AutoPartyListUpdater.refresh()
            }
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
        
        // 离线被踢（彻底移除）
        kickedOffline.find(message)?.let {
            PartyUtils.removeMemberWithOffline(it.groupValues[2])
            return
        }
        
        // 断开连接被移除（彻底移除）
        kickedDisconnected.find(message)?.let {
            PartyUtils.removeMemberWithOffline(it.groupValues[2])
            return
        }
        
        // 队长断开连接（标记为掉线）
        leaderDisconnected.find(message)?.let {
            PartyUtils.markOffline(it.groupValues[2])
            return
        }
        
        // 队长重新连接（标记为在线）
        leaderRejoined.find(message)?.let {
            PartyUtils.markOnline(it.groupValues[2])
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
            PartyUtils.markOnline(it.groupValues[2])
            PartyUtils.partyLeader = it.groupValues[2]
            return
        }
        
        // 成员断开连接（标记为掉线）
        memberDisconnected.find(message)?.let {
            PartyUtils.markOffline(it.groupValues[2])
            return
        }
        
        // 成员重新连接（标记为在线）
        memberRejoined.find(message)?.let {
            PartyUtils.markOnline(it.groupValues[2])
            return
        }
        
        // 邀请
        partyInvite.find(message)?.let {
            val inviter = it.groupValues[2]
            val invited = it.groupValues[4]
            val myName = mc.player?.name?.string
            
            // 如果之前不在队伍中，说明这是自己创建的队，自己是队长
            if (!PartyUtils.isInParty) {
                PartyUtils.addMember(inviter)
                PartyUtils.partyLeader = inviter
                // 把自己也加入队伍（如果是邀请者）
                if (myName != null && inviter.equals(myName, ignoreCase = true)) {
                    PartyUtils.addMember(myName)
                }
            } else {
                // 已经在队伍中，只添加邀请者
                PartyUtils.addMember(inviter)
            }
            return
        }
        
        // 队伍解散检测
        for (pattern in disbandPatterns) {
            if (pattern.containsMatchIn(message)) {
                PartyUtils.disband()
                return
            }
        }
        
        // 成员列表 - 由 PartyListHandler 处理，这里不再处理以避免冲突
        // membersList.find(message)?.let { ... }
        
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
        
        // 检测 !cancel（任何聊天，只要包含队友名字）
        handleCancelCommand(message)
        
        // 检测 !mod（队友询问 Mod 信息）
        handleModCommand(message)
    }
    
    /**
     * 处理 !mod 命令
     * 当队友发送 !mod 时，自动回复 Mod 信息
     */
    private fun handleModCommand(message: String) {
        if (!Config.settings.mod) return
        
        // 检查是否是纯 !mod 命令（不是 !mod something）
        val cleanMessage = message.replace(Regex("§[0-9a-fk-or]"), "")
        
        // 必须是 Party Chat 且包含 !mod
        if (!cleanMessage.startsWith("Party >")) return
        if (!cleanMessage.contains("!mod")) return
        
        // 提取发送者
        val pattern = Regex("^Party > (?:\\[.+?] )?(.+?):")
        val match = pattern.find(cleanMessage) ?: return
        
        val senderRaw = match.groupValues[1].trim()
        val senderClean = senderRaw.replace(Regex("\\[.+?]\\s*"), "").trim()
        val myName = mc.player?.name?.string ?: return
        
        // 如果是自己发送的，跳过
        if (senderClean.equals(myName, ignoreCase = true)) return
        
        // 延迟 500ms 回复，避免看起来太像机器人
        Thread {
            Thread.sleep(500)
            mc.execute {
                sendPartyChat("Using PartyCommands Mod by Admin_SR40")
            }
            Thread.sleep(300)
            mc.execute {
                sendPartyChat("Available at GitHub (Admin-SR40/PartyCommands)")
            }
        }.start()
    }
    
    /**
     * 处理 !cancel 命令
     * 检测队伍聊天中的 !cancel 并取消地牢倒计时
     */
    private fun handleCancelCommand(message: String) {
        // 必须包含 !cancel
        if (!message.contains("!cancel")) return
        
        // 获取当前玩家名字
        val myName = mc.player?.name?.string ?: return
        
        // 移除颜色代码后再匹配（避免颜色代码干扰）
        val cleanMessage = message.replace(Regex("§[0-9a-fk-or]"), "")
        
        // 从各种聊天格式中提取发送者名字
        val patterns = listOf(
            // Party > [RANK] PlayerName: message
            Regex("^Party > (?:\\[.+?] )?(.+?):"),
            // Guild > [RANK] PlayerName: message
            Regex("^Guild > (?:\\[.+?] )?(.+?):"),
            // [222] [RANK] PlayerName: message (一般聊天)
            Regex("^\\[\\d+\\] (?:\\[.+?] )?(.+?):"),
            // [RANK] PlayerName: message (简化格式)
            Regex("^(?:\\[.+?] )?(.+?):")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(cleanMessage)
            if (match != null) {
                val senderRaw = match.groupValues[1].trim()
                // 提取纯净名字（移除 rank）
                val senderClean = senderRaw.replace(Regex("\\[.+?]\\s*"), "").trim()
                
                // 如果是自己发送的，跳过
                if (senderClean.equals(myName, ignoreCase = true)) {
                    return
                }
                // 取消倒计时，使用纯净名字
                CountdownManager.tryCancelFromPartyChat(senderClean)
                return
            }
        }
    }
}
