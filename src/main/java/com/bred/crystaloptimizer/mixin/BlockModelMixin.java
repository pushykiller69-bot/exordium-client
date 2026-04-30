package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.CrystalOptimizer;
import com.bred.crystaloptimizer.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModels.class)
public class BlockModelMixin {
    private static final ModelIdentifier SPAWNER_BLOCKSTATE_MODEL =
        new ModelIdentifier(Identifier.of("minecraft", "spawner"), "normal");

    @Inject(method = "getModel", at = @At("RETURN"), cancellable = true, require = 0)
    private void swapToSpawnerModel(BlockState state, CallbackInfoReturnable<BakedModel> cir) {
        if (!ModConfig.isActive()) return;
        if (!ModConfig.showPlacedAsSpawner) return;

        String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
        if (!CrystalOptimizer.SPAWNER_BLOCKS.contains(blockId)) return;

        var mgr = MinecraftClient.getInstance().getBakedModelManager();
        BakedModel spawnerModel = mgr.getModel(SPAWNER_BLOCKSTATE_MODEL);

        if (spawnerModel == null || spawnerModel == mgr.getMissingModel()) {
            spawnerModel = mgr.getModel(new ModelIdentifier(Identifier.of("minecraft", "spawner"), ""));
        }

        if (spawnerModel != null && spawnerModel != mgr.getMissingModel()) {
            cir.setReturnValue(spawnerModel);
        }
    }
}

