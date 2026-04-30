package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.config.ModConfig;
import com.bred.crystaloptimizer.render.FakeSpawnerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends Entity {

    @Unique private boolean crystalOptimizer$checkedSpawner = false;
    @Unique private boolean crystalOptimizer$fromFakeSpawner = false;

    protected MobEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick()V", at = @At("HEAD"), require = 0)
    private void onEntityTick(CallbackInfo ci) {
        if (!crystalOptimizer$checkedSpawner && this.age < 10) {
            crystalOptimizer$checkedSpawner = true;
            BlockPos entityPos = this.getBlockPos();
            for (BlockPos pos : BlockPos.iterate(entityPos.add(-2, -2, -2), entityPos.add(2, 2, 2))) {
                if (FakeSpawnerRenderer.isTracked(pos)) {
                    crystalOptimizer$fromFakeSpawner = true;
                    break;
                }
            }
        }

        if (crystalOptimizer$fromFakeSpawner) {
            if (!ModConfig.isActive()) return;
            if (this.age > 100) this.age = 100;
        }
    }

    @Inject(method = "canImmediatelyDespawn(D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void preventDespawn(double distanceSquared, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.isActive()) return;
        if (crystalOptimizer$fromFakeSpawner) cir.setReturnValue(false);
    }

    @Inject(method = "cannotDespawn()Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void forceNoDespawn(CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.isActive()) return;
        if (crystalOptimizer$fromFakeSpawner) cir.setReturnValue(true);
    }
}
