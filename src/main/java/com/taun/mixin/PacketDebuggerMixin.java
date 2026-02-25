package com.taun.mixin;

import com.taun.TaunCore;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * DEBUG MIXIN - Logs ALL packets being sent to help identify how the farming mod sends commands
 * This should be REMOVED once we identify the method
 */
@Mixin(ClientConnection.class)
public class PacketDebuggerMixin {
    
    /**
     * Intercept ALL packet sends to see what the farming mod is doing
     */
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"))
    private void logPacketSend(Packet<?> packet, CallbackInfo ci) {
        // Only log command-related packets to avoid spam
        String packetClass = packet.getClass().getSimpleName();
        
        // Log all C2S (Client to Server) packets that might be commands
        if (packetClass.contains("Command") || packetClass.contains("Chat") || packetClass.contains("C2S")) {
            TaunCore.LOGGER.info("====== PACKET SENT ======");
            TaunCore.LOGGER.info("Packet Type: {}", packetClass);
            TaunCore.LOGGER.info("Full Class: {}", packet.getClass().getName());
            TaunCore.LOGGER.info("Packet Data: {}", packet.toString());
            TaunCore.LOGGER.info("========================");
            
            // Try to extract command if it's a command packet
            try {
                if (packetClass.contains("Command")) {
                    // Try to get the command field via reflection
                    var commandField = packet.getClass().getDeclaredField("command");
                    commandField.setAccessible(true);
                    String command = (String) commandField.get(packet);
                    TaunCore.LOGGER.info("!!! COMMAND DETECTED: '{}' !!!", command);
                    TaunCore.LOGGER.info(">>> If this is the farming mod's command, we need to block this packet type!");
                }
            } catch (Exception e) {
                // Field doesn't exist or couldn't access, that's fine
            }
        }
    }
}
