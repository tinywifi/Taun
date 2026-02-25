package com.taun.mixin;

import com.taun.TaunCore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blocks inputs by immediately unsetting them after they're set.
 * This hooks into setPressed which is called whenever a key state changes.
 */
@Mixin(KeyBinding.class)
public class SafeInputBlocker {
    
    @Shadow
    private boolean pressed;
    
    /**
     * Hook into setPressed AFTER it executes.
     * If blocking is enabled and this is a key we want to block,
     * immediately unset it.
     */
    @Inject(method = "setPressed", at = @At("TAIL"))
    private void unsetIfBlocking(boolean pressed, CallbackInfo ci) {
        // Only process if the key was just set to pressed
        if (!pressed) {
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
            return;
        }
        
        KeyBinding thisBinding = (KeyBinding)(Object)this;
        
        // LOG ALL KEY PRESSES when blocking is active (to see what's getting through)
        if (TaunCore.blockingInputs) {
            String keyName = "UNKNOWN KEY";
            boolean shouldBlock = false;
            
            if (thisBinding == client.options.attackKey) {
                keyName = "M1 (attack)";
                shouldBlock = true;
            } else if (thisBinding == client.options.useKey) {
                keyName = "M2 (use)";
                shouldBlock = true;
            } else if (thisBinding == client.options.leftKey) {
                keyName = "A (left)";
                shouldBlock = true;
            } else if (thisBinding == client.options.rightKey) {
                keyName = "D (right)";
                shouldBlock = true;
            } else if (thisBinding == client.options.forwardKey) {
                keyName = "W (forward)";
                shouldBlock = true;
            } else if (thisBinding == client.options.backKey) {
                keyName = "S (back)";
                shouldBlock = true;
            } else {
                // This is some OTHER key that we're not blocking - log it!
                keyName = thisBinding.getBoundKeyTranslationKey() + " (NOT BLOCKED)";
                System.out.println("╔═══════════════════════════════════════════");
                System.out.println("║ [TAUN] UNBLOCKED KEY PRESSED!");
                System.out.println("║ Key: " + keyName);
                System.out.println("║ Translation Key: " + thisBinding.getBoundKeyTranslationKey());
                System.out.println("║ >>> This key is being pressed but NOT blocked!");
                System.out.println("╚═══════════════════════════════════════════");
            }
            
            if (shouldBlock) {
                // Immediately unset the key
                this.pressed = false;
                System.out.println("[Taun Input Blocker] Blocked: " + keyName);
            }
        }
    }
}
