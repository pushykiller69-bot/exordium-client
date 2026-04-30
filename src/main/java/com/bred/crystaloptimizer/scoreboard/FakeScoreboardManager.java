package com.bred.crystaloptimizer.scoreboard;

import com.bred.crystaloptimizer.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FakeScoreboardManager {

    private static final String OBJECTIVE_NAME = "exordium_fake_sidebar";
    private static final String TEAM_PREFIX = "exordium_sb_";
    private static final long KEYALL_SECONDS = 59 * 60 + 59;
    private static final int MAP_SIZE = 9;
    private static final double REGION_SIZE = 50_000.0;
    private static final double MAP_OFFSET = 225_000.0;
    private static final String FALLBACK_REGION = "donutsmp.net";
    private static final String[] REGION_TYPE_NAMES = {
        "EU Central", "EU West", "NA East", "NA West", "Asia", "Oceania"
    };
    private static final int[][] REGION_LAYOUT = {
        {82, 5}, {100, 3}, {101, 3}, {102, 3}, {103, 2}, {104, 2}, {105, 2}, {106, 2}, {91, 2},
        {83, 5}, {44, 3}, {75, 3}, {42, 3}, {41, 2}, {40, 2}, {39, 2}, {38, 2}, {92, 2},
        {84, 5}, {45, 3}, {14, 3}, {13, 3}, {12, 2}, {11, 2}, {10, 2}, {37, 2}, {93, 2},
        {85, 5}, {46, 5}, {74, 5}, {3, 3}, {2, 2}, {1, 2}, {25, 2}, {36, 2}, {94, 2},
        {86, 4}, {47, 4}, {72, 4}, {71, 4}, {5, 2}, {4, 2}, {24, 2}, {35, 2}, {95, 2},
        {87, 4}, {51, 1}, {17, 1}, {9, 0}, {8, 0}, {7, 0}, {23, 0}, {34, 0}, {96, 2},
        {88, 4}, {54, 1}, {18, 1}, {61, 0}, {62, 0}, {21, 0}, {22, 0}, {33, 0}, {97, 0},
        {89, 0}, {26, 1}, {27, 0}, {28, 0}, {29, 0}, {30, 0}, {59, 0}, {32, 0}, {98, 0},
        {90, 0}, {107, 1}, {108, 1}, {109, 1}, {110, 1}, {111, 1}, {112, 1}, {113, 1}, {99, 0}
    };

    private static ScoreboardObjective fakeObjective;
    private static ScoreboardObjective savedObjective;
    private static Scoreboard lastScoreboard;
    private static final List<String> teamNames = new ArrayList<>();

    private static long keyallStartTime;
    private static boolean keyallInitialized;
    private static long lastUpdateMs;
    private static String lastFingerprint = "";

    public static void render(DrawContext ctx, MinecraftClient client) {
        tick(client);
    }

    public static void tick(MinecraftClient client) {
        if (client == null || client.world == null || client.player == null) {
            reset();
            return;
        }

        if (!ModConfig.fakeScoreboardActive || ModConfig.panicMode) {
            restoreOriginal(client.world.getScoreboard());
            return;
        }

        if (!keyallInitialized) {
            keyallInitialized = true;
            keyallStartTime = System.currentTimeMillis();
        }

        Scoreboard scoreboard = client.world.getScoreboard();
        String fingerprint = buildFingerprint(client);
        long now = System.currentTimeMillis();

        if (scoreboard == lastScoreboard
            && fakeObjective != null
            && fingerprint.equals(lastFingerprint)
            && now - lastUpdateMs < 500) {
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, fakeObjective);
            return;
        }

        updateScoreboard(client, scoreboard, fingerprint);
        lastUpdateMs = now;
    }

    public static boolean isFakeObjective(ScoreboardObjective objective) {
        return objective != null && OBJECTIVE_NAME.equals(objective.getName());
    }

    public static boolean handleRightClick(int mouseX, int mouseY, MinecraftClient client) {
        return false;
    }

    public static boolean handleContextClick(int mouseX, int mouseY) {
        return false;
    }

    public static void reset() {
        if (lastScoreboard != null) {
            restoreOriginal(lastScoreboard);
        }

        fakeObjective = null;
        savedObjective = null;
        lastScoreboard = null;
        teamNames.clear();
        keyallInitialized = false;
        lastUpdateMs = 0;
        lastFingerprint = "";
    }

    private static void updateScoreboard(MinecraftClient client, Scoreboard scoreboard, String fingerprint) {
        if (scoreboard != lastScoreboard) {
            reset();
            lastScoreboard = scoreboard;
        }

        ScoreboardObjective current = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (savedObjective == null && current != null && !isFakeObjective(current)) {
            savedObjective = current;
        }

        cleanup(scoreboard);

        if (fakeObjective != null) {
            try {
                scoreboard.removeObjective(fakeObjective);
            } catch (Exception ignored) {
            }
        }

        fakeObjective = scoreboard.addObjective(
            OBJECTIVE_NAME,
            ScoreboardCriterion.DUMMY,
            gradientTitle(scoreboardTitle()),
            ScoreboardCriterion.RenderType.INTEGER,
            false,
            (NumberFormat) BlankNumberFormat.INSTANCE
        );

        scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, fakeObjective);

        List<MutableText> entries = entries(client);
        for (int i = 0; i < entries.size(); i++) {
            String teamName = TEAM_PREFIX + i;
            teamNames.add(teamName);

            Team existingTeam = scoreboard.getTeam(teamName);
            if (existingTeam != null) {
                scoreboard.removeTeam(existingTeam);
            }

            Team team = scoreboard.addTeam(teamName);
            team.setPrefix(entries.get(i));

            String holderName = uniqueHolderName(i);
            ScoreHolder holder = ScoreHolder.fromName(holderName);
            scoreboard.removeScore(holder, fakeObjective);

            ScoreAccess score = scoreboard.getOrCreateScore(holder, fakeObjective);
            score.setScore(entries.size() - i);
            scoreboard.addScoreHolderToTeam(holderName, team);
        }

        lastFingerprint = fingerprint;
    }

    private static void restoreOriginal(Scoreboard scoreboard) {
        if (scoreboard == null) return;

        cleanup(scoreboard);

        if (fakeObjective != null) {
            try {
                scoreboard.removeObjective(fakeObjective);
            } catch (Exception ignored) {
            }
        }

        if (scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) == null
            || isFakeObjective(scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR))) {
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, savedObjective);
        }

        fakeObjective = null;
        savedObjective = null;
        lastFingerprint = "";
    }

    private static void cleanup(Scoreboard scoreboard) {
        for (String teamName : new ArrayList<>(teamNames)) {
            Team team = scoreboard.getTeam(teamName);
            if (team != null) {
                try {
                    scoreboard.removeTeam(team);
                } catch (Exception ignored) {
                }
            }
        }
        teamNames.clear();
    }

    private static List<MutableText> entries(MinecraftClient client) {
        List<MutableText> lines = new ArrayList<>();
        lines.add(Text.literal(" "));

        if (ModConfig.sbShowMoney) {
            lines.add(row("💲", "Money", displayMoney(), 0x00FF00));
        }
        if (ModConfig.sbShowShards) {
            lines.add(row("⭐", "Shards", emptyToDash(ModConfig.fakeScoreboardShards), 0xA503FC));
        }
        if (ModConfig.sbShowKills) {
            lines.add(row("🗡", "Kills", emptyToDash(ModConfig.fakeScoreboardKills), 0xFF0000));
        }
        if (ModConfig.sbShowDeaths) {
            lines.add(row("☠", "Deaths", emptyToDash(ModConfig.fakeScoreboardDeaths), 0xFC7703));
        }
        if (ModConfig.sbShowKeyall) {
            lines.add(row("⌛", "Keyall", keyallTimer(), 0x00A2FF));
        }
        if (ModConfig.sbShowPlaytime) {
            lines.add(row("\u25A3", "Playtime", emptyToDash(ModConfig.fakeScoreboardPlaytime), 0xFFE600));
        }
        if (ModConfig.sbShowTeam) {
            lines.add(row("\u27A4", "Team", emptyToDash(ModConfig.fakeScoreboardTeam), 0x00A2FF));
        }

        if (ModConfig.sbShowRegion) {
            lines.add(Text.literal("  "));
            lines.add(footer(client));
        }

        return lines;
    }

    private static MutableText row(String icon, String label, String value, int accent) {
        return colored(icon + " ", accent)
            .append(colored(label + " ", 0xE0E0E0))
            .append(colored(value, accent));
    }

    private static MutableText footer(MinecraftClient client) {
        String footer = configuredFooter(client);
        int start = footer.lastIndexOf('(');
        int end = footer.lastIndexOf(')');

        if (start <= 0 || end <= start) {
            return colored(footer, 0xA0A0A0);
        }

        String region = footer.substring(0, start).trim();
        String ping = footer.substring(start + 1, end).trim();
        return colored(region + " ", 0xA0A0A0)
            .append(colored("(", 0xA0A0A0))
            .append(colored(ping, 0x00A2FF))
            .append(colored(")", 0xA0A0A0));
    }

    private static String buildFingerprint(MinecraftClient client) {
        return scoreboardTitle() + "|"
            + displayMoney() + "|"
            + ModConfig.fakeScoreboardShards + "|"
            + ModConfig.fakeScoreboardKills + "|"
            + ModConfig.fakeScoreboardDeaths + "|"
            + keyallTimer() + "|"
            + ModConfig.fakeScoreboardPlaytime + "|"
            + ModConfig.fakeScoreboardTeam + "|"
            + configuredFooter(client) + "|"
            + ModConfig.sbShowMoney + ModConfig.sbShowShards + ModConfig.sbShowKills
            + ModConfig.sbShowDeaths + ModConfig.sbShowKeyall + ModConfig.sbShowPlaytime
            + ModConfig.sbShowTeam + ModConfig.sbShowRegion;
    }

    private static String scoreboardTitle() {
        if (ModConfig.fakeScoreboardTitle == null || ModConfig.fakeScoreboardTitle.isBlank()) {
            return "Donut SMP";
        }
        return ModConfig.fakeScoreboardTitle.trim();
    }

    private static String displayMoney() {
        if (ModConfig.fakeScoreboardBalanceInitialized) {
            return formatBalance(ModConfig.fakeScoreboardBalance);
        }
        return emptyToDash(ModConfig.fakeScoreboardMoney);
    }

    private static String emptyToDash(String value) {
        return value == null || value.isBlank() ? "---" : value.trim();
    }

    private static String formatBalance(long value) {
        if (value >= 1_000_000_000_000L) return compact(value, 1_000_000_000_000D, "T");
        if (value >= 1_000_000_000L) return compact(value, 1_000_000_000D, "B");
        if (value >= 1_000_000L) return compact(value, 1_000_000D, "M");
        if (value >= 1_000L) return compact(value, 1_000D, "K");
        return String.valueOf(value);
    }

    private static String compact(long value, double divisor, String suffix) {
        DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
        return format.format(value / divisor) + suffix;
    }

    private static String keyallTimer() {
        long elapsed = System.currentTimeMillis() - keyallStartTime;
        long remaining = Math.max(0, KEYALL_SECONDS - elapsed / 1000);
        return String.format(Locale.ROOT, "%dm %ds", remaining / 60, remaining % 60);
    }

    private static String configuredFooter(MinecraftClient client) {
        return currentRegion(client) + " (" + actualPing(client) + "ms)";
    }

    private static int actualPing(MinecraftClient client) {
        try {
            if (client.getNetworkHandler() == null || client.player == null) return 0;
            var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            return entry != null ? entry.getLatency() : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String serverAddress(MinecraftClient client) {
        ServerInfo info = client.getCurrentServerEntry();
        if (info == null || info.address == null || info.address.isBlank()) return "Local";

        String address = info.address.trim();
        int colon = address.lastIndexOf(':');
        if (colon > 0 && colon < address.length() - 1) {
            try {
                Integer.parseInt(address.substring(colon + 1));
                address = address.substring(0, colon);
            } catch (NumberFormatException ignored) {
            }
        }
        return address;
    }

    private static String currentRegion(MinecraftClient client) {
        if (client == null || client.player == null) {
            return FALLBACK_REGION;
        }

        String region = regionTypeName(client.player.getX(), client.player.getZ());
        if (!region.equals("Unknown")) {
            return region;
        }

        String configured = ModConfig.fakeScoreboardFooter == null ? "" : ModConfig.fakeScoreboardFooter.trim();
        if (!configured.isEmpty() && !configured.equalsIgnoreCase("Europe")) {
            int paren = configured.indexOf('(');
            return paren > 0 ? configured.substring(0, paren).trim() : configured;
        }

        String server = serverAddress(client);
        return server == null || server.isBlank() || server.equalsIgnoreCase("local") ? FALLBACK_REGION : server;
    }

    private static String regionTypeName(double worldX, double worldZ) {
        int gridX = (int) ((worldX + MAP_OFFSET) / REGION_SIZE);
        int gridZ = (int) ((worldZ + MAP_OFFSET) / REGION_SIZE);
        if (gridX < 0 || gridX >= MAP_SIZE || gridZ < 0 || gridZ >= MAP_SIZE) {
            return "Unknown";
        }

        int index = gridZ * MAP_SIZE + gridX;
        if (index < 0 || index >= REGION_LAYOUT.length || REGION_LAYOUT[index].length < 2) {
            return "Unknown";
        }

        int regionType = REGION_LAYOUT[index][1];
        if (regionType < 0 || regionType >= REGION_TYPE_NAMES.length) {
            return "Unknown";
        }
        return REGION_TYPE_NAMES[regionType];
    }

    private static MutableText gradientTitle(String text) {
        return gradient(text, 0x007CF9, 0x00C6F9);
    }

    private static MutableText gradient(String text, int startColor, int endColor) {
        MutableText result = Text.empty();
        int len = Math.max(1, text.length());

        int startR = (startColor >> 16) & 0xFF;
        int startG = (startColor >> 8) & 0xFF;
        int startB = startColor & 0xFF;
        int endR = (endColor >> 16) & 0xFF;
        int endG = (endColor >> 8) & 0xFF;
        int endB = endColor & 0xFF;

        for (int i = 0; i < text.length(); i++) {
            float t = len <= 1 ? 0 : (float) i / (len - 1);
            int r = Math.round(startR + (endR - startR) * t);
            int g = Math.round(startG + (endG - startG) * t);
            int b = Math.round(startB + (endB - startB) * t);
            result.append(Text.literal(String.valueOf(text.charAt(i)))
                .setStyle(Style.EMPTY
                    .withColor(TextColor.fromRgb((r << 16) | (g << 8) | b))
                    .withBold(true)));
        }

        return result;
    }

    private static MutableText colored(String text, int rgb) {
        return Text.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
    }

    private static String uniqueHolderName(int index) {
        return "\u00A7" + Integer.toHexString(index);
    }
}
