package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.util.NameSpoofUtil;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Inject(method = "getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Lnet/minecraft/text/Text;",
            at = @At("RETURN"), cancellable = true, require = 0)
    private void exordium$spoofTabName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (!NameSpoofUtil.isLocalProfile(entry.getProfile())) return;
        Text spoofed = NameSpoofUtil.replaceOwnName(cir.getReturnValue());
        if (spoofed != cir.getReturnValue()) {
            cir.setReturnValue(spoofed);
        }
    }
}
