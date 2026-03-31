package com.partycommands.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestions;
import com.partycommands.commands.Commands;
import com.partycommands.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @Shadow
    private ParseResults<SharedSuggestionProvider> currentParse;

    @Shadow
    @Final
    private EditBox input;

    @Shadow
    private boolean keepSuggestions;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    private CommandSuggestions.SuggestionsList suggestions;

    @Shadow
    private boolean allowSuggestions;

    @Shadow
    private List<net.minecraft.util.FormattedCharSequence> commandUsage;

    @Shadow
    protected abstract void showSuggestions(boolean bl);

    @Shadow
    private void updateUsageInfo() {}

    @Inject(method = "updateCommandInfo", at = @At("HEAD"), cancellable = true)
    private void onUpdateCommandInfo(CallbackInfo ci) {
        String value = this.input.getValue();
        String prefix = Config.INSTANCE.getSettings().getPrefix();
        int length = prefix.length();

        if (value.startsWith(prefix)) {
            // 清除之前的 suggestion（模仿原版逻辑）
            if (!this.keepSuggestions) {
                this.input.setSuggestion(null);
                this.suggestions = null;
            }

            // 重置 parse 如果输入改变
            if (this.currentParse != null && !this.currentParse.getReader().getString().equals(value)) {
                this.currentParse = null;
            }

            StringReader reader = new StringReader(value);
            reader.setCursor(length);

            if (this.currentParse == null) {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    this.currentParse = Commands.DISPATCHER.parse(reader, player.connection.getSuggestionsProvider());
                }
            }

            int cursor = this.input.getCursorPosition();
            if (cursor >= length && (this.suggestions == null || !this.keepSuggestions)) {
                if (this.currentParse != null && !this.currentParse.getExceptions().isEmpty()) {
                    // 未知或不完整命令：不显示命令补全列表，只显示错误信息
                    this.pendingSuggestions = CompletableFuture.completedFuture(
                        new Suggestions(StringRange.at(cursor), Collections.emptyList())
                    );
                } else {
                    this.pendingSuggestions = Commands.DISPATCHER.getCompletionSuggestions(this.currentParse, cursor);
                }

                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone() && this.allowSuggestions) {
                        // 确保没有显示过期的 suggestion
                        if (this.currentParse != null && this.currentParse.getReader().getString().equals(value)) {
                            ((CommandSuggestions) (Object) this).showSuggestions(false);
                        }
                    }
                });
            }

            // 更新命令用法/错误信息
            if (this.currentParse != null && !this.currentParse.getExceptions().isEmpty()) {
                this.updateUsageInfo();
            } else {
                this.commandUsage.clear();
            }

            ci.cancel();
        }
    }
}
