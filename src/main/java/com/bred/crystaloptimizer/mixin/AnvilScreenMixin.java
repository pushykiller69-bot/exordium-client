package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.CrystalOptimizer;
import com.bred.crystaloptimizer.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends ForgingScreen<AnvilScreenHandler> {

    public AnvilScreenMixin(AnvilScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title, null);
    }

    @Inject(method = "drawForeground", at = @At("TAIL"), require = 0)
    private void onDrawForeground(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (!ModConfig.isActive() || ModConfig.panicMode) return;

        ItemStack output = this.handler.getSlot(2).getStack();
        if (output.isEmpty()) return;
        if (!(output.getItem() instanceof BlockItem bi)) return;
        String blockId = Registries.BLOCK.getId(bi.getBlock()).toString();
        if (!CrystalOptimizer.SPAWNER_BLOCKS.contains(blockId)) return;

        Text name = output.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null || !name.getString().trim().equalsIgnoreCase("Spawner")) return;
    }
}
