package com.taun.mixin;

import com.taun.TaunCore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts command sending to block specific commands from ALL sources
 * (manual typing, other mods, macros, etc.)
 */
@Mixin(ClientPlayNetworkHandler.class)
public class CommandBlockerMixin {
    
    /**
     * Intercept sendChatCommand which is called for ALL /commands
     */
    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    private void blockCommand(String command, CallbackInfo ci) {
        TaunCore.LOGGER.info("DEBUG: sendChatCommand intercepted: '{}'", command);
        if (TaunCore.isCommandBlocked(command)) {
            TaunCore.LOGGER.info("DEBUG: Blocking command '{}'", command);
            // Cancel the command from being sent
            ci.cancel();
            
            // Show message to player
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                long remainingSeconds = TaunCore.getCommandBlockTimeRemaining(command);
                client.player.sendMessage(
                    Text.literal("§c[BLOCKED] §7Command blocked for " + remainingSeconds + " more seconds"), 
                    false
                );
            }
        }
    }
    
    /**
     * Also intercept sendChatMessage in case mods use this instead
     * This catches commands sent as chat messages starting with /
     */
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void blockChatCommand(String message, CallbackInfo ci) {
        TaunCore.LOGGER.info("DEBUG: sendChatMessage intercepted: '{}'", message);
        // Check if it's a command (starts with /)
        if (message != null && message.startsWith("/")) {
            String command = message.substring(1); // Remove the /
            if (TaunCore.isCommandBlocked(command)) {
                TaunCore.LOGGER.info("DEBUG: Blocking chat command '{}'", command);
                // Cancel the message from being sent
                ci.cancel();
                
                // Show message to player
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    long remainingSeconds = TaunCore.getCommandBlockTimeRemaining(command);
                    client.player.sendMessage(
                        Text.literal("§c[BLOCKED] §7Command blocked for " + remainingSeconds + " more seconds"), 
                        false
                    );
                }
            }
        }
    }
}
