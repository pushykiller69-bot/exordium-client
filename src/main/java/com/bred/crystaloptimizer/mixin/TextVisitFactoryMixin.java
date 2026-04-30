package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.util.NameSpoofUtil;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TextVisitFactory.class)
public abstract class TextVisitFactoryMixin {

    @ModifyVariable(
        method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
        at = @At("HEAD"),
        argsOnly = true,
        index = 1,
        require = 0
    )
    private static String exordium$spoofVisitedText(String text) {
        return NameSpoofUtil.replaceOwnName(text);
    }
}
