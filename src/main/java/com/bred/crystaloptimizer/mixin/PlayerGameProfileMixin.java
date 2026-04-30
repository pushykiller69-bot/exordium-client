package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.util.NameSpoofUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class PlayerGameProfileMixin {

    @Inject(method = "getGameProfile()Lcom/mojang/authlib/GameProfile;", at = @At("RETURN"), cancellable = true, require = 0)
    private void exordium$spoofGameProfile(CallbackInfoReturnable<GameProfile> cir) {
        AbstractClientPlayerEntity self = (AbstractClientPlayerEntity) (Object) this;
        if (!NameSpoofUtil.isLocalPlayerEntity(self)) return;
        GameProfile spoofed = NameSpoofUtil.spoofedProfile(cir.getReturnValue());
        if (spoofed != cir.getReturnValue()) {
            cir.setReturnValue(spoofed);
        }
    }
}
