package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.util.NameSpoofUtil;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class PlayerNameMixin {

    @Inject(method = "getName()Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true, require = 0)
    private void exordium$getName(CallbackInfoReturnable<Text> cir) {
        Entity self = (Entity) (Object) this;
        if (NameSpoofUtil.isLocalPlayerScoreHolder(self)) {
            cir.setReturnValue(NameSpoofUtil.spoofedText());
        }
    }

    @Inject(method = "getDisplayName()Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true, require = 0)
    private void exordium$getDisplayName(CallbackInfoReturnable<Text> cir) {
        Entity self = (Entity) (Object) this;
        if (NameSpoofUtil.isLocalPlayerScoreHolder(self)) {
            cir.setReturnValue(NameSpoofUtil.spoofedText());
        }
    }

    @Inject(method = "getNameForScoreboard()Ljava/lang/String;", at = @At("RETURN"), cancellable = true, require = 0)
    private void exordium$getNameForScoreboard(CallbackInfoReturnable<String> cir) {
        Entity self = (Entity) (Object) this;
        if (NameSpoofUtil.isLocalPlayerScoreHolder(self)) {
            cir.setReturnValue(NameSpoofUtil.spoofedName());
        }
    }
}
