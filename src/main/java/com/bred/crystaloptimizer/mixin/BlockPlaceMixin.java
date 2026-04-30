package com.bred.crystaloptimizer.mixin;

import com.bred.crystaloptimizer.config.ModConfig;
import com.bred.crystaloptimizer.render.FakeSpawnerRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class BlockPlaceMixin {

    @Inject(method = "onBlockUpdate", at = @At("HEAD"), cancellable = true, require = 0)
    private void exordium$onBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        if (!shouldShowFakePlacedSpawners()) return;

        BlockPos pos = packet.getPos();
        BlockState incoming = packet.getState();
        if (pos == null || incoming == null) return;
        if (!FakeSpawnerRenderer.isTracked(pos)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        ci.cancel();
        applyTrackedServerState(client, pos, incoming);
    }

    @Inject(method = "onChunkDeltaUpdate", at = @At("HEAD"), cancellable = true, require = 0)
    private void exordium$onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet, CallbackInfo ci) {
        if (!shouldShowFakePlacedSpawners()) return;

        final boolean[] hasTrackedUpdate = {false};
        packet.visitUpdates((pos, state) -> {
            if (FakeSpawnerRenderer.isTracked(pos)) hasTrackedUpdate[0] = true;
        });
        if (!hasTrackedUpdate[0]) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        ci.cancel();
        packet.visitUpdates((pos, state) -> {
            if (FakeSpawnerRenderer.isTracked(pos)) applyTrackedServerState(client, pos, state);
            else client.world.setBlockState(pos, state);
        });
    }

    private static void applyTrackedServerState(MinecraftClient client, BlockPos pos, BlockState incoming) {
        if (incoming.isAir() || incoming.getBlock() == Blocks.SPAWNER) {
            FakeSpawnerRenderer.removeFake(pos);
            client.world.setBlockState(pos, incoming);
            return;
        }
        FakeSpawnerRenderer.storeFakeOriginal(pos, incoming);
        FakeSpawnerRenderer.applyFake(client, pos, incoming);
    }

    private static boolean shouldShowFakePlacedSpawners() {
        if (ModConfig.panicMode) return false;
        return ModConfig.showPlacedAsSpawner;
    }
}
