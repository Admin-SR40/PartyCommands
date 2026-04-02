package com.partycommands.utils

/**
 * 根据 Ping 值获取颜色
 * 300ms+ 红色, 250-299 橙色, 200-249 黄色, 150-199 浅绿色, 150- 绿色
 */
fun getPingColor(ping: Int): String {
    return when {
        ping >= 300 -> "§c"      // 红色 - 很高
        ping >= 250 -> "§6"      // 橙色 - 偏高
        ping >= 200 -> "§e"      // 黄色 - 中等
        ping >= 150 -> "§a"      // 绿色 - 良好
        else -> "§a"             // 绿色 - 优秀
    }
}

/**
 * 根据 TPS 值获取颜色
 * 19.5-20 绿色, 18-19.49 黄色, 15-17.99 橙色, 15- 红色
 */
fun getTpsColor(tps: Double): String {
    return when {
        tps >= 19.5 -> "§a"      // 绿色 - 优秀
        tps >= 18.0 -> "§e"      // 黄色 - 良好
        tps >= 15.0 -> "§6"      // 橙色 - 偏低
        else -> "§c"             // 红色 - 很低
    }
}

/**
 * 根据 FPS 值获取颜色
 * 120+ 绿色, 60-119 浅绿色, 30-59 黄色, 15-29 橙色, 15- 红色
 */
fun getFpsColor(fps: Int): String {
    return when {
        fps >= 120 -> "§a"       // 绿色 - 流畅
        fps >= 60 -> "§a"        // 绿色 - 良好
        fps >= 30 -> "§e"        // 黄色 - 可玩
        fps >= 15 -> "§6"        // 橙色 - 卡顿
        else -> "§c"             // 红色 - 很卡
    }
}

/**
 * 响应前缀颜色
 */
const val RESPONSE_PREFIX = "§b"      // 青色 - 标签
const val RESPONSE_LABEL = "§e"       // 黄色 - 标签名
const val RESPONSE_VALUE = "§f"       // 白色 - 普通值
const val RESPONSE_SEPARATOR = "§7"   // 灰色 - 分隔符

/**
 * 格式化响应消息（带颜色）
 * 例如: formatResponse("Ping", "150ms", "§a")
 * 输出: §bPing: §a150ms
 */
fun formatResponse(label: String, value: String, valueColor: String = RESPONSE_VALUE): String {
    return "${RESPONSE_PREFIX}$label${RESPONSE_SEPARATOR}: $valueColor$value"
}
