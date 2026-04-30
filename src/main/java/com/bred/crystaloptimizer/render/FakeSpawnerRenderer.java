package com.bred.crystaloptimizer.render;

import com.bred.crystaloptimizer.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FakeSpawnerRenderer {

    private static final java.util.concurrent.ConcurrentHashMap<BlockPos, BlockState> originalBlocks
        = new java.util.concurrent.ConcurrentHashMap<>();

    private static final Set<BlockPos> trackedSpawnerPositions = ConcurrentHashMap.newKeySet();
    private static final Set<BlockPos> bypassPositions = ConcurrentHashMap.newKeySet();
    private static boolean loaded = false;

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        trackedSpawnerPositions.clear();
        for (String posStr : ModConfig.trackedSpawnerPositions) {
            try {
                if (posStr == null || posStr.trim().isEmpty()) continue;
                String[] parts = posStr.split(",");
                if (parts.length == 3) {
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    int z = Integer.parseInt(parts[2].trim());
                    trackedSpawnerPositions.add(new BlockPos(x, y, z));
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    private static void saveTrackedPositions() {
        ModConfig.trackedSpawnerPositions.clear();
        for (BlockPos pos : trackedSpawnerPositions) {
            ModConfig.trackedSpawnerPositions.add(pos.getX() + "," + pos.getY() + "," + pos.getZ());
        }
        ModConfig.save();
    }

    public static boolean isBypassed(BlockPos pos) {
        return pos != null && bypassPositions.contains(pos);
    }

    public static void applyFake(MinecraftClient client, BlockPos pos, BlockState realState) {
        if (client == null || client.world == null || pos == null || realState == null) return;
        if (!originalBlocks.containsKey(pos)) {
            originalBlocks.put(pos.toImmutable(), realState);
        }
        bypassPositions.add(pos);
        try {
            client.world.setBlockState(pos, Blocks.SPAWNER.getDefaultState());
            BlockEntity be = client.world.getBlockEntity(pos);
            if (be instanceof MobSpawnerBlockEntity spawner) {
                spawner.getLogic().setEntityId(EntityType.SKELETON, client.world, client.world.random, pos);
            }
            client.world.scheduleBlockRerenderIfNeeded(pos, realState, Blocks.SPAWNER.getDefaultState());
        } finally {
            bypassPositions.remove(pos);
        }
    }

    public static void revertOne(MinecraftClient client, BlockPos pos) {
        if (client == null || client.world == null || pos == null) return;
        BlockState original = originalBlocks.remove(pos);
        if (original == null) return;
        bypassPositions.add(pos);
        try {
            client.world.setBlockState(pos, original);
            client.world.scheduleBlockRerenderIfNeeded(pos, Blocks.SPAWNER.getDefaultState(), original);
        } finally {
            bypassPositions.remove(pos);
        }
    }

    public static void revertAll(MinecraftClient client) {
        if (client == null || client.world == null) return;
        for (BlockPos pos : new HashSet<>(originalBlocks.keySet())) {
            revertOne(client, pos);
        }
        originalBlocks.clear();
    }

    public static void addTrackedPos(BlockPos pos) {
        if (pos == null) return;
        ensureLoaded();
        trackedSpawnerPositions.add(pos.toImmutable());
        saveTrackedPositions();
    }

    public static void removeTrackedPos(BlockPos pos) {
        if (pos == null) return;
        ensureLoaded();
        trackedSpawnerPositions.remove(pos);
        saveTrackedPositions();
    }

    public static boolean isTracked(BlockPos pos) {
        if (pos == null) return false;
        ensureLoaded();
        return trackedSpawnerPositions.contains(pos);
    }

    public static boolean isFakeAt(BlockPos pos) {
        return pos != null && originalBlocks.containsKey(pos);
    }

    public static boolean hasFakes() {
        return !originalBlocks.isEmpty();
    }

    public static Set<BlockPos> getTrackedPositions() {
        ensureLoaded();
        return new HashSet<>(trackedSpawnerPositions);
    }

    public static void storeFakeOriginal(BlockPos pos, BlockState original) {
        if (pos == null || original == null) return;
        originalBlocks.put(pos.toImmutable(), original);
    }

    public static void removeFake(BlockPos pos) {
        if (pos == null) return;
        originalBlocks.remove(pos);
    }

    public static void onWorldUnload() {
        originalBlocks.clear();
        bypassPositions.clear();
        loaded = false;
    }
}
