package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.config.ModConfig;
import com.bred.crystaloptimizer.scoreboard.FakeScoreboardManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();

    @Inject(method = "onMouseButton(JIII)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action != GLFW.GLFW_PRESS) return;
        if (ModConfig.panicMode || !ModConfig.fakeScoreboardActive) return;
        if (client.currentScreen != null) return;

        int scaledX = scaledX();
        int scaledY = scaledY();

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (FakeScoreboardManager.handleRightClick(scaledX, scaledY, client)) {
                ci.cancel();
            }
            return;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (FakeScoreboardManager.handleContextClick(scaledX, scaledY)) {
                ci.cancel();
            }
        }
    }

    private int scaledX() {
        return (int) Math.floor(getX() * client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth());
    }

    private int scaledY() {
        return (int) Math.floor(getY() * client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight());
    }
}
