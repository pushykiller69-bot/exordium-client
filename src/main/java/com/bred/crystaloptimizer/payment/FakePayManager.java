package com.bred.crystaloptimizer.payment;

import com.bred.crystaloptimizer.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FakePayManager {

    public static final long MAX_AMOUNT = 1_000_000_000_000L;
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)([KMBT]?)$");

    private FakePayManager() {}

    public static boolean tryHandleCommand(String rawCommand, Runnable cancelCommand) {
        if (!ModConfig.fakePayEnabled || ModConfig.panicMode) return false;

        String command = normalizeCommand(rawCommand);
        if (!command.toLowerCase(Locale.ROOT).startsWith("pay ")) return false;

        cancelCommand.run();

        String[] parts = command.split("\\s+");
        if (parts.length < 3) {
            sendError("Usage: /pay <player> <amount>");
            return true;
        }

        String recipient = parts[1];
        long amount = parseAmount(parts[2]);
        if (amount <= 0) {
            sendError("Invalid fake pay amount.");
            return true;
        }

        if (amount > MAX_AMOUNT) {
            sendError("Fake pay max is 1T.");
            return true;
        }

        if (!hasEnoughFakeBalance(amount)) {
            sendInsufficientFunds();
            return true;
        }

        sendFakePayment(recipient, amount);
        deductFakeScoreboard(amount);
        ModConfig.save();
        return true;
    }

    public static boolean sendManualFakePay(String recipient, String rawAmount) {
        if (ModConfig.panicMode) return false;
        if (recipient == null || recipient.isBlank() || rawAmount == null || rawAmount.isBlank()) return false;

        long amount = parseAmount(rawAmount);
        if (amount <= 0 || amount > MAX_AMOUNT) return false;
        if (!hasEnoughFakeBalance(amount)) {
            sendInsufficientFunds();
            return false;
        }

        sendFakePayment(recipient.trim(), amount);
        deductFakeScoreboard(amount);
        ModConfig.save();
        return true;
    }

    public static long parseAmount(String rawAmount) {
        if (rawAmount == null) return 0;

        String normalized = rawAmount.trim()
            .replace(",", "")
            .replace("$", "")
            .toUpperCase(Locale.ROOT);
        Matcher matcher = AMOUNT_PATTERN.matcher(normalized);
        if (!matcher.matches()) return 0;

        double value;
        try {
            value = Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return 0;
        }

        double multiplier = switch (matcher.group(2)) {
            case "K" -> 1_000D;
            case "M" -> 1_000_000D;
            case "B" -> 1_000_000_000D;
            case "T" -> 1_000_000_000_000D;
            default -> 1D;
        };

        double result = value * multiplier;
        if (!Double.isFinite(result) || result > Long.MAX_VALUE) return 0;
        return (long) result;
    }

    public static String formatAmount(long amount) {
        if (amount >= 1_000_000_000_000L) return compact(amount, 1_000_000_000_000D, "T");
        if (amount >= 1_000_000_000L) return compact(amount, 1_000_000_000D, "B");
        if (amount >= 1_000_000L) return compact(amount, 1_000_000D, "M");
        if (amount >= 1_000L) return compact(amount, 1_000D, "K");
        return String.valueOf(amount);
    }

    private static String normalizeCommand(String rawCommand) {
        if (rawCommand == null) return "";
        String command = rawCommand.trim();
        while (command.startsWith("/")) command = command.substring(1).trim();
        return command;
    }

    private static void sendFakePayment(String recipient, long amount) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        client.player.sendMessage(
            Text.literal("You paid ").styled(s -> s.withColor(0xAAAAAA))
                .append(Text.literal(recipient + " ").styled(s -> s.withColor(0x00AAFF)))
                .append(Text.literal("$" + formatAmount(amount)).styled(s -> s.withColor(0x55FF55)))
                .append(Text.literal(".").styled(s -> s.withColor(0xAAAAAA))),
            false
        );

        if (client.world != null) {
            client.world.playSound(
                client.player.getX(), client.player.getY(), client.player.getZ(),
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f, false
            );
        }
    }

    private static void deductFakeScoreboard(long amount) {
        ModConfig.fakeScoreboardBalance = Math.max(0, currentFakeBalance() - amount);
        ModConfig.fakeScoreboardBalanceInitialized = true;
        ModConfig.fakeScoreboardMoney = formatScoreboardBalance(ModConfig.fakeScoreboardBalance);
    }

    private static boolean hasEnoughFakeBalance(long amount) {
        return currentFakeBalance() >= amount;
    }

    private static long currentFakeBalance() {
        if (ModConfig.fakeScoreboardBalanceInitialized) return Math.max(0, ModConfig.fakeScoreboardBalance);
        return Math.max(0, parseAmount(ModConfig.fakeScoreboardMoney));
    }

    private static void sendError(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        client.player.sendMessage(
            Text.literal("[Fake Pay] ").styled(s -> s.withColor(0xFF5555))
                .append(Text.literal(message).styled(s -> s.withColor(0xFFAAAA))),
            false
        );
    }

    private static void sendInsufficientFunds() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        client.player.sendMessage(
            Text.literal("You don't have enough funds to do this.").styled(s -> s.withColor(0xFF5555)),
            false
        );
    }

    private static String formatScoreboardBalance(long amount) {
        if (amount >= 1_000_000_000_000L) return compact(amount, 1_000_000_000_000D, "T");
        if (amount >= 1_000_000_000L) return compact(amount, 1_000_000_000D, "B");
        if (amount >= 1_000_000L) return compact(amount, 1_000_000D, "M");
        if (amount >= 1_000L) return compact(amount, 1_000D, "K");
        return String.valueOf(amount);
    }

    private static String compact(long amount, double divisor, String suffix) {
        DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
        return format.format(amount / divisor) + suffix;
    }
}
