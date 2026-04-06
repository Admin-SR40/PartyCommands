package com.partycommands.utils

import com.partycommands.utils.formatResponse

object PartyListHandler {
    
    // 是否正在等待 /p list 输出
    var isWaitingForList = false
    
    // 是否是静默模式（自动更新，不显示给用户）
    private var silentMode = false
    
    // 等待超时的 tick 数
    private var waitTicks = 0
    
    // 收集到的行
    private val collectedLines = mutableListOf<String>()
    
    // 标记上一条消息是否为"不在队伍"
    private var lastMessageWasNotInParty = false
    
    // 标记下一个分隔符是否需要被拦截
    private var shouldInterceptNextSeparator = false
    
    // 标记上一条消息是否为分隔线（用于折叠连续分隔线）
    private var lastMessageWasSeparator = false
    
    // /p list 输出的正则
    private val notInPartyPattern = Regex("^You are not currently in a party\\.$")
    private val partySizePattern = Regex("^Party Members \\((\\d+)\\)$")
    private val leaderPattern = Regex("^Party Leader: (.+)$")
    private val membersPattern = Regex("^Party Members: (.+)$")
    
    // 需要拦截后面分隔符的消息
    private val interceptSeparatorPatterns = listOf(
        Regex("^Party Finder > .+ joined the dungeon group!"),
        Regex("^\\[.+?] .+ joined the party\\.$")
    )
    
    /**
     * 开始等待 /p list 输出
     */
    fun startWaiting() {
        isWaitingForList = true
        silentMode = false
        waitTicks = 0
        collectedLines.clear()
        lastMessageWasNotInParty = false
        lastMessageWasSeparator = false
    }
    
    /**
     * 开始静默等待（用于自动更新，不显示给用户）
     */
    fun startAutoWaiting() {
        isWaitingForList = true
        silentMode = true
        waitTicks = 0
        collectedLines.clear()
        lastMessageWasNotInParty = false
        lastMessageWasSeparator = false
    }
    
    /**
     * 每 tick 调用，用于超时检测
     */
    fun onTick() {
        if (!isWaitingForList) return
        
        waitTicks++
        // 2秒超时（40 ticks）
        if (waitTicks > 40) {
            val wasSilent = silentMode
            isWaitingForList = false
            lastMessageWasNotInParty = false
            silentMode = false
            lastMessageWasSeparator = false
            if (!wasSilent) {
                modMessage(formatResponse("Party Status", "§cFailed to get party list (timeout)", ""))
            }
        }
    }
    
    /**
     * 处理聊天消息，返回 true 表示已拦截
     */
    fun handleMessage(text: String): Boolean {
        val trimmed = text.trim()
        
        // 检测是否需要拦截后面的分隔符
        for (pattern in interceptSeparatorPatterns) {
            if (pattern.containsMatchIn(trimmed)) {
                shouldInterceptNextSeparator = true
                return false // 不拦截这条消息本身
            }
        }
        
        // 如果需要拦截这个分隔符
        if (shouldInterceptNextSeparator && isSeparatorLine(trimmed)) {
            shouldInterceptNextSeparator = false
            if (com.partycommands.config.Config.settings.removeSeparator) {
                lastMessageWasSeparator = true
                return true
            }
        }
        
        // 全局拦截纯分隔线消息（不在等待 /p list 时）
        if (!isWaitingForList && isSeparatorLine(trimmed)) {
            if (com.partycommands.config.Config.settings.removeSeparator) {
                lastMessageWasSeparator = true
                return true
            }
            return false
        }
        
        // 如果检测到 /p list 的输出格式但不在等待模式，不自动收集
        // 只有主动调用 !status 时才收集，避免拦截用户手动输入的 /p list
        // if (!isWaitingForList && trimmed.startsWith("Party Members (")) {
        //     isWaitingForList = true
        //     silentMode = true
        //     collectedLines.clear()
        //     collectedLines.add(trimmed)
        //     lastMessageWasSeparator = false
        //     return true
        // }
        
        if (!isWaitingForList) {
            lastMessageWasSeparator = false
            return false
        }
        
        // 检测分隔线（开头或结尾的）
        if (isSeparatorLine(trimmed)) {
            // 如果检测到不在队伍的消息后，再遇到分隔线，直接拦截
            if (lastMessageWasNotInParty) {
                isWaitingForList = false
                lastMessageWasNotInParty = false
                lastMessageWasSeparator = false
                return true
            }
            // 正常收集流程的分隔线
            if (collectedLines.isEmpty()) {
                // 开始收集
                return true
            } else {
                // 结束收集，解析并显示（静默模式下只解析不显示）
                if (silentMode) {
                    parseSilently()
                    silentMode = false
                } else {
                    parseAndDisplay()
                }
                isWaitingForList = false
                lastMessageWasSeparator = false
                return true
            }
        }
        
        // 检测不在队伍
        if (notInPartyPattern.matches(trimmed)) {
            lastMessageWasNotInParty = true
            PartyUtils.disband() // 更新队伍状态
            if (!silentMode) {
                modMessage(formatResponse("Party Status", "§cYou are not in a party!", ""))
            } else {
                silentMode = false
            }
            return true
        }
        
        // 如果在收集中，保存行
        if (collectedLines.isNotEmpty() || trimmed.startsWith("Party Members")) {
            collectedLines.add(trimmed)
            return true
        }
        
        return false
    }
    
    /**
     * 解析收集到的数据并显示
     */
    private fun parseAndDisplay() {
        var leader: String? = null
        val members = mutableListOf<String>()
        var memberCount = 0
        
        for (line in collectedLines) {
            // 解析人数
            partySizePattern.find(line)?.let {
                memberCount = it.groupValues[1].toInt()
                return@let
            }
            
            // 解析队长
            leaderPattern.find(line)?.let {
                val leaderName = it.groupValues[1]
                leader = formatMember(leaderName)
                // 清理：颜色代码 + rank + ●
                val cleanName = leaderName.noControlCodes
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
                PartyUtils.partyLeader = cleanName
                PartyUtils.addMember(cleanName, leaderName)
                return@let
            }
            
            // 解析成员
            membersPattern.find(line)?.let { match ->
                val membersText = match.groupValues[1]
                // 按 ● 分割，因为 ● 是成员之间的分隔符
                // 格式: "I1me ● [VIP+] piper045 ● [MVP+] EgoistBlaze ●"
                membersText.split("●").forEach { member ->
                    val memberFull = member.trim()
                    if (memberFull.isNotEmpty()) {
                        val formatted = formatMember(memberFull)
                        members.add(formatted)
                        // 清理：颜色代码、rank
                        val cleanName = memberFull.noControlCodes
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
                            .trim()
                        PartyUtils.addMember(cleanName, memberFull)
                    }
                }
            }
        }
        displayResult(leader, members, memberCount)
    }
    
    /**
     * 静默解析（只更新状态，不显示给用户）
     */
    private fun parseSilently() {
        for (line in collectedLines) {
            // 解析队长
            leaderPattern.find(line)?.let {
                val leaderName = it.groupValues[1]
                // 提取纯净名字
                val cleanName = leaderName.noControlCodes
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
                PartyUtils.partyLeader = cleanName
                PartyUtils.addMember(cleanName, leaderName)
                return@let
            }
            
            // 解析成员（同时检测 ● 颜色）
            membersPattern.find(line)?.let { match ->
                val membersText = match.groupValues[1]
                // 使用正则匹配每个成员和后面的 ● 颜色
                // 格式: "I1me §c● [VIP+] piper045 §a● ..." 
                // ● 的颜色表示前面成员的状态
                val memberWithBulletPattern = Regex("((?:\\[.+?])?\\s*[a-zA-Z0-9_]+)(?:\\s*(§[0-9a-f])?●)?")
                memberWithBulletPattern.findAll(membersText).forEach { memberMatch ->
                    val memberName = memberMatch.groupValues[1].trim()
                    val bulletColor = memberMatch.groupValues[2] // 后面 ● 的颜色
                    
                    if (memberName.isEmpty()) return@forEach
                    
                    val cleanName = memberName.noControlCodes
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
                        .trim()
                    
                    if (cleanName.isEmpty()) return@forEach
                    
                    PartyUtils.addMember(cleanName, memberName)
                    
                    // 如果 ● 是红色(§c)，标记为离线
                    if (bulletColor == "§c") {
                        PartyUtils.markOffline(cleanName)
                    } else {
                        PartyUtils.markOnline(cleanName)
                    }
                }
            }
        }
    }
    
    /**
     * 格式化成员名字（只保留颜色，不显示 rank 和 ●）
     */
    private fun formatMember(text: String): String {
        // 先去掉 ● 符号
        val cleaned = text.replace("●", "").trim()
        // 匹配 [RANK] Name 格式
        val rankPattern = Regex("^(\\[.+?])?\\s*(.+?)$")
        val match = rankPattern.find(cleaned) ?: return "§7$cleaned"
        
        val rank = match.groupValues[1]
        val name = match.groupValues[2]
        
        // 根据 rank 返回对应颜色，只显示名字
        val nameColor = when {
            rank.contains("YOUTUBE") || rank.contains("ADMIN") -> "§c"
            rank.contains("MVP++") -> "§6"
            rank.contains("MVP+") || rank.contains("MVP") -> "§b"
            rank.contains("VIP+") || rank.contains("VIP") -> "§a"
            else -> "§7"
        }
        
        return "$nameColor$name"
    }
    
    /**
     * 显示解析结果
     */
    private fun displayResult(leader: String?, members: List<String>, count: Int) {
        val mc = net.minecraft.client.Minecraft.getInstance()
        val myName = mc.player?.name?.string ?: ""
        val isLeader = leader?.noControlCodes == myName
        
        // 第一行：标题（带颜色）
        modMessage("§b§lParty Status")
        
        // 第二行：队长（检查是否掉线）
        if (leader != null) {
            val leaderClean = leader.noControlCodes
            val isLeaderOffline = PartyUtils.isOffline(leaderClean)
            val displayLeader = if (isLeaderOffline) "$leader §c(Offline)" else leader
            rawMessage("§e§lLeader:")
            rawMessage(" §7- $displayLeader")
        } else {
            rawMessage("§e§lLeader: §7Unknown")
        }
        
        // 第三行：成员（过滤掉队长和自己）
        val otherMembers = members.filter { 
            val memberClean = it.noControlCodes
            // 过滤掉队长
            val isLeader = leader != null && memberClean.equals(leader.noControlCodes, ignoreCase = true)
            // 过滤掉自己
            val isSelf = memberClean.equals(myName, ignoreCase = true)
            !isLeader && !isSelf
        }.toMutableList()
        
        // 如果不是队长，把自己加入成员列表（使用粉色，其他 rank 没用过）
        if (!isLeader && myName.isNotEmpty()) {
            otherMembers.add(0, "§dYou")
        }
        
        // 统计在线/离线成员数
        val totalMembers = otherMembers.size
        val onlineMembers = otherMembers.count { member ->
            val memberClean = member.noControlCodes
            memberClean == myName || !PartyUtils.isOffline(memberClean)
        }
        val offlineMembers = totalMembers - onlineMembers
        
        // 如果有人离线，显示 (在线/总数)，否则只显示总数
        val membersCountStr = if (offlineMembers > 0) {
            "§7(§a$onlineMembers§7/§f$totalMembers§7)"
        } else {
            "§7(§f$totalMembers§7)"
        }
        
        rawMessage("§e§lMembers $membersCountStr:")
        
        if (otherMembers.isEmpty()) {
            rawMessage(" §7- §cNone")
        } else {
            for (member in otherMembers) {
                // 提取纯净名字检查是否掉线
                val memberClean = member.noControlCodes
                val isOffline = PartyUtils.isOffline(memberClean) && memberClean != myName
                val displayMember = if (isOffline) "$member §c(Offline)" else member
                rawMessage(" §7- $displayMember")
            }
        }
    }
    
    /**
     * 检查是否为纯分隔线消息
     * 只拦截由 - = ─ ▬ ═ 和空格组成的消息
     */
    private fun isSeparatorLine(text: String): Boolean {
        return text.matches(Regex("^[\\-▬─══=\\s]+$"))
    }
}
