package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.util.NameSpoofUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TextRenderer.class)
public class TextRendererMixin {

    @ModifyVariable(
        method = {
            "draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
            "draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I"
        },
        at = @At("HEAD"),
        argsOnly = true,
        index = 1,
        require = 0
    )
    private String exordium$spoofDrawnString(String text) {
        return NameSpoofUtil.replaceOwnName(text);
    }

    @ModifyVariable(
        method = "draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
        at = @At("HEAD"),
        argsOnly = true,
        index = 1,
        require = 0
    )
    private Text exordium$spoofDrawnText(Text text) {
        return NameSpoofUtil.replaceOwnName(text);
    }
}
