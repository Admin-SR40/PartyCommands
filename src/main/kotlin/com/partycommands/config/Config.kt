package com.partycommands.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object Config {
    private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    private val CONFIG_FILE = File(
        FabricLoader.getInstance().configDir.toFile(),
        "partycommands.json"
    )
    
    // 默认配置
    var settings = ConfigData()
    
    data class ConfigData(
        // 基础命令开关
        var enabled: Boolean = true,
        var prefix: String = "!",
        
        // 信息类命令
        var ping: Boolean = true,
        var tps: Boolean = true,
        var fps: Boolean = true,
        var time: Boolean = true,
        var location: Boolean = true,
        var coords: Boolean = true,
        var holding: Boolean = true,
        var status: Boolean = true,
        
        // 队伍管理命令（需要队长权限）
        var warp: Boolean = true,
        var allinvite: Boolean = true,
        var kick: Boolean = true,
        var kickoffline: Boolean = true,
        var kickall: Boolean = true,
        var promote: Boolean = true,
        var demote: Boolean = true,
        var transfer: Boolean = true,
        var disband: Boolean = true,
        
        // 队伍命令（不需要队长权限）
        var leave: Boolean = true,
        
        // 娱乐命令
        var coinflip: Boolean = true,
        var eightball: Boolean = true,
        var dice: Boolean = true,
        
        // 地牢命令
        var queueInstance: Boolean = true,
        
        // 其他
        var boop: Boolean = true,
        var invite: Boolean = true,
        var countdown: Boolean = true,
        var mod: Boolean = true,
        
        // 响应设置
        var respondInPartyChat: Boolean = true,
        var showResponseLocally: Boolean = true,
        var removeSeparator: Boolean = true,
        
        // Note 功能
        var note: String = "",
        var countdownSound: Boolean = true
    )
    
    fun load() {
        if (!CONFIG_FILE.exists()) {
            save()
            return
        }
        
        try {
            FileReader(CONFIG_FILE).use { reader ->
                val data = GSON.fromJson(reader, ConfigData::class.java)
                if (data != null) {
                    settings = data
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果加载失败，使用默认配置并保存
            save()
        }
    }
    
    fun save() {
        try {
            FileWriter(CONFIG_FILE).use { writer ->
                GSON.toJson(settings, writer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
