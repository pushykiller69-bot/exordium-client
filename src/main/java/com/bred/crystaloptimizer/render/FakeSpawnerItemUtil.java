package com.bred.crystaloptimizer.render;

import com.bred.crystaloptimizer.config.ModConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

public final class FakeSpawnerItemUtil {

    private FakeSpawnerItemUtil() {}

    public static boolean shouldRenderAsSpawner(ItemStack stack) {
        if (ModConfig.panicMode || !ModConfig.isActive()) return false;
        if (stack == null || stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        if (!hasSpawnerName(stack)) return false;
        return ModConfig.matchesConfiguredSpawnerBlock(Registries.BLOCK.getId(blockItem.getBlock()).toString());
    }

    public static boolean hasSpawnerName(ItemStack stack) {
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (isSpawnerName(customName)) return true;
        Text itemName = stack.get(DataComponentTypes.ITEM_NAME);
        return isSpawnerName(itemName);
    }

    private static boolean isSpawnerName(Text text) {
        if (text == null) return false;
        String name = text.getString()
            .trim()
            .replace(" ", "")
            .replace("_", "")
            .replace("-", "")
            .toLowerCase(java.util.Locale.ROOT);
        return name.equals("spawner") || name.equals("skeletonspawner");
    }

    public static Text displayName() {
        return Text.literal("Spawner").styled(style -> style.withColor(0xFF55FF).withItalic(false));
    }

    public static String displayMobName() {
        if (ModConfig.fakeNameEnabled && ModConfig.fakeMobName != null && !ModConfig.fakeMobName.isBlank()) {
            return titleCaseMobName(ModConfig.fakeMobName);
        }
        return "Skeleton";
    }

    private static String titleCaseMobName(String rawName) {
        String normalized = rawName == null ? "" : rawName.trim().replace('_', ' ').replace('-', ' ');
        if (normalized.isBlank()) return "Skeleton";
        StringBuilder result = new StringBuilder();
        for (String part : normalized.split("\\s+")) {
            if (part.isBlank()) continue;
            if (!result.isEmpty()) result.append(' ');
            String lower = part.toLowerCase(java.util.Locale.ROOT);
            result.append(Character.toUpperCase(lower.charAt(0)));
            if (lower.length() > 1) result.append(lower.substring(1));
        }
        return result.isEmpty() ? "Skeleton" : result.toString();
    }
}
