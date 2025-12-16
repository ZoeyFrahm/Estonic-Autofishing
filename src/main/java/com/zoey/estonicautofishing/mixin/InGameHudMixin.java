package com.zoey.estonicautofishing.mixin;

import com.zoey.estonicautofishing.EstonicAutofishing;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    
    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void onOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (message != null) {
            String messageText = message.getString();
            EstonicAutofishing.onOverlayMessage(messageText);
        }
    }
    
    @Inject(method = "setTitleTicks", at = @At("HEAD"))
    private void onTitleTicks(int fadeInTicks, int remainTicks, int fadeOutTicks, CallbackInfo ci) {
        // This can be used to detect title changes if needed
    }
}
