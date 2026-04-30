package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.config.ModConfig;
import com.bred.crystaloptimizer.render.FakeSpawnerItemUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ItemStack.class, priority = 900)
public class ItemTooltipMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"), require = 0)
    private void exordium$appendSpawnerTooltip(Item.TooltipContext context, PlayerEntity player,
                                               TooltipType type,
                                               CallbackInfoReturnable<List<Text>> cir) {
        if (!ModConfig.showTooltips) return;

        ItemStack self = (ItemStack) (Object) this;
        if (!FakeSpawnerItemUtil.shouldRenderAsSpawner(self)) return;

        List<Text> tooltip = cir.getReturnValue();
        if (tooltip == null) return;

        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("Interact with Spawn Egg:")
            .styled(style -> style.withColor(0xD8D8D8).withItalic(false)));
        tooltip.add(Text.literal(" Sets Mob Type")
            .styled(style -> style.withColor(0x5555FF).withItalic(false)));
        tooltip.add(Text.literal(FakeSpawnerItemUtil.displayMobName())
            .styled(style -> style.withColor(0xFFFF55).withItalic(false)));
        tooltip.add(Text.literal("Worth: ")
            .styled(style -> style.withColor(0xD8D8D8).withItalic(false))
            .append(Text.literal("$0").styled(style -> style.withColor(0x00FF00).withItalic(false))));
    }
}
