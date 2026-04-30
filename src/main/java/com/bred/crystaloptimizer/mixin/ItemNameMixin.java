package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.render.FakeSpawnerItemUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemNameMixin {

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true, require = 0)
    private void exordium$overrideSpawnerName(CallbackInfoReturnable<Text> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (FakeSpawnerItemUtil.shouldRenderAsSpawner(self)) {
            cir.setReturnValue(FakeSpawnerItemUtil.displayName());
        }
    }
}
