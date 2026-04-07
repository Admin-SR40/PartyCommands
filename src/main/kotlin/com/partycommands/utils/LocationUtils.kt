package com.partycommands.utils

/**
 * 位置工具
 * 由于 Hypixel 编码，无法检测具体位置
 * !loc 和 !coords 都显示坐标
 */
object LocationUtils {
    
    /**
     * 获取位置字符串（仅坐标）
     * 格式: "x: xxx, y: xxx, z: xxx"
     */
    fun getLocationString(): String {
        return getPositionString()
    }
}
