package com.bred.crystaloptimizer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir().resolve("crystaloptimizer.json");

    public static boolean showTooltips           = true;
    public static boolean showCustomName         = true;
    public static boolean showWhenPlacedByOthers = false;
    public static boolean panicMode              = false;
    public static boolean fakeNameEnabled        = false;
    public static boolean showPlacedAsSpawner    = true;
    public static String  fakeMobName            = "";
    public static String  configuredBlockId      = "";

    public static boolean fakeScoreboardActive             = false;
    public static long    fakeScoreboardBalance            = 0;
    public static boolean fakeScoreboardBalanceInitialized = false;
    public static String  fakeScoreboardTitle              = "Donut SMP";
    public static String  fakeScoreboardMoney              = "";
    public static String  fakeScoreboardShards             = "";
    public static String  fakeScoreboardKills              = "";
    public static String  fakeScoreboardDeaths             = "";
    public static String  fakeScoreboardPlaytime           = "";
    public static String  fakeScoreboardTeam               = "";
    public static String  fakeScoreboardFooter             = "";
    public static boolean sbShowMoney    = true;
    public static boolean sbShowShards   = true;
    public static boolean sbShowKills    = true;
    public static boolean sbShowDeaths   = true;
    public static boolean sbShowKeyall   = true;
    public static boolean sbShowPlaytime = true;
    public static boolean sbShowTeam     = true;
    public static boolean sbShowRegion   = true;

    public static boolean fakePayEnabled   = false;
    public static String  fakePayRecipient = "";
    public static String  fakePayAmount    = "";
    public static String  fakePayToggleKey = "NONE";
    public static String  sbToggleKey      = "NONE";

    public static boolean nameSpooferEnabled = false;
    public static String  spoofedName        = "";

    public static java.util.Set<String> trackedSpawnerPositions = new java.util.HashSet<>();

    public static boolean isActive() {
        return !panicMode;
    }

    private static class ConfigData {
        boolean showTooltips = true;
        boolean showCustomName = true;
        boolean showWhenPlacedByOthers = false;
        boolean fakeNameEnabled = false;
        boolean showPlacedAsSpawner = true;
        String fakeMobName = "";
        String configuredBlockId = "";
        boolean fakeScoreboardActive = false;
        long fakeScoreboardBalance = 0;
        boolean fakeScoreboardBalanceInitialized = false;
        String fakeScoreboardTitle = "Donut SMP";
        String fakeScoreboardMoney = "";
        String fakeScoreboardShards = "";
        String fakeScoreboardKills = "";
        String fakeScoreboardDeaths = "";
        String fakeScoreboardPlaytime = "";
        String fakeScoreboardTeam = "";
        String fakeScoreboardFooter = "";
        boolean sbShowMoney = true;
        boolean sbShowShards = true;
        boolean sbShowKills = true;
        boolean sbShowDeaths = true;
        boolean sbShowKeyall = true;
        boolean sbShowPlaytime = true;
        boolean sbShowTeam = true;
        boolean sbShowRegion = true;
        boolean fakePayEnabled = false;
        String fakePayRecipient = "";
        String fakePayAmount = "";
        String fakePayToggleKey = "NONE";
        String sbToggleKey = "NONE";
        boolean nameSpooferEnabled = false;
        String spoofedName = "";
        java.util.List<String> trackedSpawnerPositions = new java.util.ArrayList<>();
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data == null) return;

            showTooltips = data.showTooltips;
            showCustomName = data.showCustomName;
            showWhenPlacedByOthers = data.showWhenPlacedByOthers;
            fakeNameEnabled = data.fakeNameEnabled;
            showPlacedAsSpawner = data.showPlacedAsSpawner;
            fakeMobName = data.fakeMobName != null ? data.fakeMobName : "";
            setConfiguredBlockId(data.configuredBlockId);
            fakeScoreboardActive = data.fakeScoreboardActive;
            fakeScoreboardBalance = data.fakeScoreboardBalance;
            fakeScoreboardBalanceInitialized = data.fakeScoreboardBalanceInitialized;
            fakeScoreboardTitle = normalizeScoreboardTitle(data.fakeScoreboardTitle);
            fakeScoreboardMoney = data.fakeScoreboardMoney != null ? data.fakeScoreboardMoney : "";
            fakeScoreboardShards = data.fakeScoreboardShards != null ? data.fakeScoreboardShards : "";
            fakeScoreboardKills = data.fakeScoreboardKills != null ? data.fakeScoreboardKills : "";
            fakeScoreboardDeaths = data.fakeScoreboardDeaths != null ? data.fakeScoreboardDeaths : "";
            fakeScoreboardPlaytime = data.fakeScoreboardPlaytime != null ? data.fakeScoreboardPlaytime : "";
            fakeScoreboardTeam = data.fakeScoreboardTeam != null ? data.fakeScoreboardTeam : "";
            fakeScoreboardFooter = normalizeScoreboardFooter(data.fakeScoreboardFooter);
            sbShowMoney = data.sbShowMoney;
            sbShowShards = data.sbShowShards;
            sbShowKills = data.sbShowKills;
            sbShowDeaths = data.sbShowDeaths;
            sbShowKeyall = data.sbShowKeyall;
            sbShowPlaytime = data.sbShowPlaytime;
            sbShowTeam = data.sbShowTeam;
            sbShowRegion = data.sbShowRegion;
            fakePayEnabled = data.fakePayEnabled;
            fakePayRecipient = data.fakePayRecipient != null ? data.fakePayRecipient : "";
            fakePayAmount = data.fakePayAmount != null ? data.fakePayAmount : "";
            fakePayToggleKey = data.fakePayToggleKey != null ? data.fakePayToggleKey : "NONE";
            sbToggleKey = data.sbToggleKey != null ? data.sbToggleKey : "NONE";
            nameSpooferEnabled = data.nameSpooferEnabled;
            spoofedName = data.spoofedName != null ? data.spoofedName : "";
            trackedSpawnerPositions.clear();
            if (data.trackedSpawnerPositions != null) {
                trackedSpawnerPositions.addAll(data.trackedSpawnerPositions);
            }
        } catch (Exception e) {
            com.bred.crystaloptimizer.CrystalOptimizer.LOGGER.error("Failed to load config", e);
        }
    }

    public static void save() {
        ConfigData data = new ConfigData();
        data.showTooltips = showTooltips;
        data.showCustomName = showCustomName;
        data.showWhenPlacedByOthers = showWhenPlacedByOthers;
        data.fakeNameEnabled = fakeNameEnabled;
        data.showPlacedAsSpawner = showPlacedAsSpawner;
        data.fakeMobName = fakeMobName;
        setConfiguredBlockId(configuredBlockId);
        data.configuredBlockId = configuredBlockId;
        data.fakeScoreboardActive = fakeScoreboardActive;
        data.fakeScoreboardBalance = fakeScoreboardBalance;
        data.fakeScoreboardBalanceInitialized = fakeScoreboardBalanceInitialized;
        data.fakeScoreboardTitle = fakeScoreboardTitle;
        data.fakeScoreboardMoney = fakeScoreboardMoney;
        data.fakeScoreboardShards = fakeScoreboardShards;
        data.fakeScoreboardKills = fakeScoreboardKills;
        data.fakeScoreboardDeaths = fakeScoreboardDeaths;
        data.fakeScoreboardPlaytime = fakeScoreboardPlaytime;
        data.fakeScoreboardTeam = fakeScoreboardTeam;
        data.fakeScoreboardFooter = fakeScoreboardFooter;
        data.sbShowMoney = sbShowMoney;
        data.sbShowShards = sbShowShards;
        data.sbShowKills = sbShowKills;
        data.sbShowDeaths = sbShowDeaths;
        data.sbShowKeyall = sbShowKeyall;
        data.sbShowPlaytime = sbShowPlaytime;
        data.sbShowTeam = sbShowTeam;
        data.sbShowRegion = sbShowRegion;
        data.fakePayEnabled = fakePayEnabled;
        data.fakePayRecipient = fakePayRecipient;
        data.fakePayAmount = fakePayAmount;
        data.fakePayToggleKey = fakePayToggleKey;
        data.sbToggleKey = sbToggleKey;
        data.nameSpooferEnabled = nameSpooferEnabled;
        data.spoofedName = spoofedName;
        data.trackedSpawnerPositions = new java.util.ArrayList<>(trackedSpawnerPositions);
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
            GSON.toJson(data, writer);
        } catch (Exception e) {
            com.bred.crystaloptimizer.CrystalOptimizer.LOGGER.error("Failed to save config", e);
        }
    }

    public static void setConfiguredBlockId(String blockId) {
        configuredBlockId = normalizeBlockIdList(blockId);
        refreshSpawnerBlocks();
    }

    public static boolean matchesConfiguredSpawnerBlock(String blockId) {
        Set<String> configuredIds = parseBlockIds(configuredBlockId);
        if (configuredIds.isEmpty()) return true;
        String normalizedBlockId = normalizeSingleBlockId(blockId);
        String pathOnly = pathOnly(normalizedBlockId);
        for (String configuredId : configuredIds) {
            if (configuredId.equals(normalizedBlockId)) return true;
            if (pathOnly(configuredId).equals(pathOnly)) return true;
        }
        return false;
    }

    private static void refreshSpawnerBlocks() {
        com.bred.crystaloptimizer.CrystalOptimizer.SPAWNER_BLOCKS.clear();
        com.bred.crystaloptimizer.CrystalOptimizer.SPAWNER_BLOCKS.addAll(parseBlockIds(configuredBlockId));
    }

    private static String normalizeBlockIdList(String blockId) {
        Set<String> ids = parseBlockIds(blockId);
        return String.join(",", ids);
    }

    private static Set<String> parseBlockIds(String blockIds) {
        Set<String> ids = new LinkedHashSet<>();
        if (blockIds == null || blockIds.isBlank()) return ids;
        for (String raw : blockIds.split("[,;\\s]+")) {
            String normalized = normalizeSingleBlockId(raw);
            if (!normalized.isEmpty()) ids.add(normalized);
        }
        return ids;
    }

    private static String normalizeSingleBlockId(String blockId) {
        if (blockId == null) return "";
        String normalized = blockId.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) return "";
        if (!normalized.contains(":")) normalized = "minecraft:" + normalized;
        return normalized;
    }

    private static String pathOnly(String blockId) {
        int colon = blockId.indexOf(':');
        return colon >= 0 && colon < blockId.length() - 1 ? blockId.substring(colon + 1) : blockId;
    }

    private static String normalizeScoreboardTitle(String title) {
        if (title == null || title.isBlank()) return "Donut SMP";
        String trimmed = title.trim();
        return trimmed.equalsIgnoreCase("Exordium Client") ? "Donut SMP" : trimmed;
    }

    private static String normalizeScoreboardFooter(String footer) {
        if (footer == null || footer.isBlank()) return "";
        String trimmed = footer.trim();
        if (trimmed.equalsIgnoreCase("donutsmp.net(50ms)") || trimmed.equalsIgnoreCase("Europe")) return "";
        return trimmed;
    }
}
