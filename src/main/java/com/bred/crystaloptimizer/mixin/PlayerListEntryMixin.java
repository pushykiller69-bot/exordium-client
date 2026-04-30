package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.util.NameSpoofUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {

    @Inject(method = "getProfile()Lcom/mojang/authlib/GameProfile;", at = @At("RETURN"), cancellable = true, require = 0)
    private void exordium$spoofListEntryProfile(CallbackInfoReturnable<GameProfile> cir) {
        GameProfile profile = cir.getReturnValue();
        if (!NameSpoofUtil.isLocalProfile(profile)) return;
        GameProfile spoofed = NameSpoofUtil.spoofedProfile(profile);
        if (spoofed != profile) {
            cir.setReturnValue(spoofed);
        }
    }
}
