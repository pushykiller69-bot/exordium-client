package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.util.NameSpoofUtil;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void exordium$addMessage(Text message, CallbackInfo ci) {
        Text spoofed = NameSpoofUtil.replaceOwnName(message);
        if (spoofed == message) return;

        ci.cancel();
        ((ChatHud) (Object) this).addMessage(spoofed);
    }

    @Inject(
        method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void exordium$addSignedMessage(Text message, MessageSignatureData signatureData,
                                           MessageIndicator indicator, CallbackInfo ci) {
        Text spoofed = NameSpoofUtil.replaceOwnName(message);
        if (spoofed == message) return;

        ci.cancel();
        ((ChatHud) (Object) this).addMessage(spoofed, signatureData, indicator);
    }
}
