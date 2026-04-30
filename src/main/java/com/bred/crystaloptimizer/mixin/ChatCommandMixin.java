package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.payment.FakePayManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayNetworkHandler.class)
public class ChatCommandMixin {

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true, require = 0)
    private void exordium$onSendChatCommand(String command, CallbackInfo ci) {
        FakePayManager.tryHandleCommand(command, ci::cancel);
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true, require = 0)
    private void exordium$onSendCommand(String command, CallbackInfoReturnable<Boolean> cir) {
        FakePayManager.tryHandleCommand(command, () -> cir.setReturnValue(true));
    }
}
