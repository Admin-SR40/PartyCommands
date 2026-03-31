package com.partycommands.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.partycommands.commands.Commands;
import com.partycommands.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String message, boolean addToHistory, CallbackInfo ci) {
        String prefix = Config.INSTANCE.getSettings().getPrefix();
        if (message.startsWith(prefix)) {
            try {
                Commands.dispatch(message.substring(prefix.length()));
                if (addToHistory && !message.isEmpty()) {
                    Minecraft.getInstance().gui.getChat().addRecentChat(message);
                }
                ci.cancel();
            } catch (CommandSyntaxException e) {
                // 未知命令或命令语法错误，让消息正常发送到聊天
            }
        }
    }
}
