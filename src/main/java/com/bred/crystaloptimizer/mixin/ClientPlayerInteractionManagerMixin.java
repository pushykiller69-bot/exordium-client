package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.config.ModConfig;
import com.bred.crystaloptimizer.render.FakeSpawnerItemUtil;
import com.bred.crystaloptimizer.render.FakeSpawnerRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Unique private BlockPos exordium$pendingFakeSpawnerPos;

    @Inject(method = "interactBlock", at = @At("HEAD"), require = 0)
    private void exordium$captureFakeSpawnerPlacement(ClientPlayerEntity player, Hand hand,
                                                      BlockHitResult hitResult,
                                                      CallbackInfoReturnable<ActionResult> cir) {
        exordium$pendingFakeSpawnerPos = null;
        if (player == null || hitResult == null) return;
        if (ModConfig.panicMode || !ModConfig.showPlacedAsSpawner) return;

        ItemStack stack = player.getStackInHand(hand);
        if (!FakeSpawnerItemUtil.shouldRenderAsSpawner(stack)) return;

        exordium$pendingFakeSpawnerPos = hitResult.getBlockPos().offset(hitResult.getSide()).toImmutable();
    }

    @Inject(method = "interactBlock", at = @At("RETURN"), require = 0)
    private void exordium$applyFakeSpawnerPlacement(ClientPlayerEntity player, Hand hand,
                                                    BlockHitResult hitResult,
                                                    CallbackInfoReturnable<ActionResult> cir) {
        BlockPos pos = exordium$pendingFakeSpawnerPos;
        exordium$pendingFakeSpawnerPos = null;
        if (pos == null) return;
        if (cir.getReturnValue() == ActionResult.FAIL || cir.getReturnValue() == ActionResult.PASS) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        BlockState currentState = client.world.getBlockState(pos);
        if (currentState.isAir() || currentState.getBlock() == Blocks.SPAWNER) return;

        FakeSpawnerRenderer.addTrackedPos(pos);
        FakeSpawnerRenderer.storeFakeOriginal(pos, currentState);
        FakeSpawnerRenderer.applyFake(client, pos, currentState);
    }
}
