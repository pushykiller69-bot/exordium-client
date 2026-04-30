package com.bred.crystaloptimizer.util;

import com.mojang.authlib.GameProfile;
import com.bred.crystaloptimizer.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Optional;

public final class NameSpoofUtil {

    private NameSpoofUtil() {}

    public static boolean shouldSpoof() {
        if (ModConfig.panicMode) return false;
        if (!ModConfig.nameSpooferEnabled) return false;
        return ModConfig.spoofedName != null && !ModConfig.spoofedName.isBlank();
    }

    public static boolean isLocalPlayerEntity(Object value) {
        if (!(value instanceof AbstractClientPlayerEntity player)) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && player.getUuid().equals(client.player.getUuid());
    }

    public static boolean isLocalProfile(GameProfile profile) {
        if (profile == null || profile.getId() == null) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && profile.getId().equals(client.player.getUuid());
    }

    public static GameProfile spoofedProfile(GameProfile original) {
        if (!shouldSpoof() || original == null) return original;
        GameProfile spoofed = new GameProfile(original.getId(), spoofedName());
        spoofed.getProperties().putAll(original.getProperties());
        return spoofed;
    }

    public static Text spoofedText() {
        return Text.literal(spoofedName());
    }

    public static String spoofedName() {
        return ModConfig.spoofedName.trim();
    }

    public static String realName() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return "";
        if (client.player != null && client.player.getGameProfile() != null) {
            String profileName = client.player.getGameProfile().getName();
            if (profileName != null && !profileName.isBlank() && !profileName.equals(spoofedName())) {
                return profileName;
            }
        }
        String sessionName = client.getSession() != null ? client.getSession().getUsername() : "";
        return sessionName != null ? sessionName : "";
    }

    public static String replaceOwnName(String original) {
        if (!shouldSpoof() || original == null || original.isEmpty()) return original;
        String realName = realName();
        String spoofed = spoofedName();
        if (realName.isBlank() || realName.equals(spoofed) || !original.contains(realName)) return original;
        return original.replace(realName, spoofed);
    }

    public static Text replaceOwnName(Text original) {
        if (!shouldSpoof() || original == null) return original;
        String replacedPlain = replaceOwnName(original.getString());
        if (replacedPlain.equals(original.getString())) return original;
        MutableText result = Text.empty().setStyle(original.getStyle());
        original.visit((style, string) -> {
            String replaced = replaceOwnName(string);
            if (!replaced.isEmpty()) result.append(Text.literal(replaced).setStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return result;
    }

    public static boolean isLocalPlayerScoreHolder(Entity entity) {
        return shouldSpoof() && isLocalPlayerEntity(entity);
    }
}
