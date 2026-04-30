package com.bred.crystaloptimizer;

import com.bred.crystaloptimizer.config.ModConfig;
import com.bred.crystaloptimizer.gui.ModScreen;
import com.bred.crystaloptimizer.render.FakeSpawnerItemUtil;
import com.bred.crystaloptimizer.render.FakeSpawnerRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class CrystalOptimizerClient implements ClientModInitializer {

    public static KeyBinding openGuiKey;
    public static KeyBinding panicKey;
    public static KeyBinding fakePayKey;
    private static boolean customFakePayHeld;
    private static boolean customScoreboardHeld;

    @Override
    public void onInitializeClient() {
        ModConfig.load();
        CrystalOptimizer.LOGGER.info("Exordium Client visual modules registered client-side.");

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> FakeSpawnerRenderer.onWorldUnload());

        final boolean[] wasInWorld = {false};
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            boolean inWorld = client.world != null && client.player != null;
            if (wasInWorld[0] && !inWorld) {
                FakeSpawnerRenderer.onWorldUnload();
                com.bred.crystaloptimizer.scoreboard.FakeScoreboardManager.reset();
            }
            wasInWorld[0] = inWorld;
        });

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.crystaloptimizer.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_EQUAL,
            "category.crystaloptimizer"
        ));

        panicKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.crystaloptimizer.panic",
            InputUtil.Type.KEYSYM,
            InputUtil.UNKNOWN_KEY.getCode(),
            "category.crystaloptimizer"
        ));

        fakePayKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.crystaloptimizer.fakepay",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "category.crystaloptimizer"
        ));

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient) return net.minecraft.util.ActionResult.PASS;
            if (ModConfig.panicMode) return net.minecraft.util.ActionResult.PASS;
            if (player == null) return net.minecraft.util.ActionResult.PASS;

            net.minecraft.item.ItemStack stack = player.getStackInHand(hand);
            if (stack == null || stack.isEmpty()) return net.minecraft.util.ActionResult.PASS;

            if (FakeSpawnerItemUtil.shouldRenderAsSpawner(stack)) {
                BlockPos hitPos = hitResult.getBlockPos();
                BlockPos placedPos = hitPos.offset(hitResult.getSide());
                FakeSpawnerRenderer.addTrackedPos(placedPos);
            }
            return net.minecraft.util.ActionResult.PASS;
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            if (ModConfig.panicMode) {
                if (FakeSpawnerRenderer.hasFakes()) {
                    FakeSpawnerRenderer.revertAll(client);
                }
                return;
            }

            if (!ModConfig.showPlacedAsSpawner) {
                if (FakeSpawnerRenderer.hasFakes()) {
                    FakeSpawnerRenderer.revertAll(client);
                }
                return;
            }

            BlockPos playerPos = client.player.getBlockPos();
            int range = 10;
            for (int dx = -range; dx <= range; dx++) {
                for (int dy = -range; dy <= range; dy++) {
                    for (int dz = -range; dz <= range; dz++) {
                        BlockPos pos = playerPos.add(dx, dy, dz);
                        BlockState state = client.world.getBlockState(pos);

                        if (state.isAir()) {
                            if (FakeSpawnerRenderer.isFakeAt(pos)) {
                                FakeSpawnerRenderer.removeFake(pos);
                            }
                            continue;
                        }

                        if (!FakeSpawnerRenderer.isTracked(pos)) continue;
                        if (state.getBlock() == Blocks.SPAWNER) continue;

                        FakeSpawnerRenderer.applyFake(client, pos, state);
                    }
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ModScreen());
                }
            }

            while (panicKey.wasPressed()) {
                ModConfig.panicMode = !ModConfig.panicMode;
                ModConfig.save();
                if (ModConfig.panicMode && client.world != null) {
                    FakeSpawnerRenderer.revertAll(client);
                }
            }

            while (fakePayKey.wasPressed()) {
                if (!hasConfiguredKey(ModConfig.fakePayToggleKey)) {
                    ModConfig.fakePayEnabled = !ModConfig.fakePayEnabled;
                    ModConfig.save();
                }
            }

            boolean fakePayOverridePressed = isConfiguredKeyPressed(client, ModConfig.fakePayToggleKey);
            if (fakePayOverridePressed && !customFakePayHeld) {
                ModConfig.fakePayEnabled = !ModConfig.fakePayEnabled;
                ModConfig.save();
            }
            customFakePayHeld = fakePayOverridePressed;

            boolean scoreboardOverridePressed = isConfiguredKeyPressed(client, ModConfig.sbToggleKey);
            if (scoreboardOverridePressed && !customScoreboardHeld) {
                ModConfig.fakeScoreboardActive = !ModConfig.fakeScoreboardActive;
                ModConfig.save();
            }
            customScoreboardHeld = scoreboardOverridePressed;
        });
    }

    private static boolean hasConfiguredKey(String configuredKey) {
        return parseConfiguredKeyCode(configuredKey) != InputUtil.UNKNOWN_KEY.getCode();
    }

    private static boolean isConfiguredKeyPressed(net.minecraft.client.MinecraftClient client, String configuredKey) {
        int code = parseConfiguredKeyCode(configuredKey);
        if (code == InputUtil.UNKNOWN_KEY.getCode()) return false;
        return InputUtil.isKeyPressed(client.getWindow().getHandle(), code);
    }

    private static int parseConfiguredKeyCode(String configuredKey) {
        if (configuredKey == null) return InputUtil.UNKNOWN_KEY.getCode();

        String normalized = configuredKey.trim()
            .replace('-', '_')
            .replace(' ', '_')
            .toUpperCase(Locale.ROOT);
        if (normalized.isEmpty() || normalized.equals("NONE")) return InputUtil.UNKNOWN_KEY.getCode();

        if (normalized.length() == 1) {
            char c = normalized.charAt(0);
            if (c >= 'A' && c <= 'Z') return GLFW.GLFW_KEY_A + (c - 'A');
            if (c >= '0' && c <= '9') return GLFW.GLFW_KEY_0 + (c - '0');
            return switch (c) {
                case '=' -> GLFW.GLFW_KEY_EQUAL;
                case '-' -> GLFW.GLFW_KEY_MINUS;
                case ',' -> GLFW.GLFW_KEY_COMMA;
                case '.' -> GLFW.GLFW_KEY_PERIOD;
                case '/' -> GLFW.GLFW_KEY_SLASH;
                case '\\' -> GLFW.GLFW_KEY_BACKSLASH;
                case ';' -> GLFW.GLFW_KEY_SEMICOLON;
                case '\'' -> GLFW.GLFW_KEY_APOSTROPHE;
                case '`' -> GLFW.GLFW_KEY_GRAVE_ACCENT;
                default -> InputUtil.UNKNOWN_KEY.getCode();
            };
        }

        if (normalized.startsWith("F")) {
            try {
                int fn = Integer.parseInt(normalized.substring(1));
                if (fn >= 1 && fn <= 25) return GLFW.GLFW_KEY_F1 + (fn - 1);
            } catch (NumberFormatException ignored) {}
        }

        return switch (normalized) {
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "ENTER", "RETURN" -> GLFW.GLFW_KEY_ENTER;
            case "BACKSPACE" -> GLFW.GLFW_KEY_BACKSPACE;
            case "ESC", "ESCAPE" -> GLFW.GLFW_KEY_ESCAPE;
            case "LEFT_SHIFT", "LSHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RIGHT_SHIFT", "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LEFT_CONTROL", "LCONTROL", "LCTRL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "RIGHT_CONTROL", "RCONTROL", "RCTRL" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "LEFT_ALT", "LALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "RIGHT_ALT", "RALT" -> GLFW.GLFW_KEY_RIGHT_ALT;
            case "UP" -> GLFW.GLFW_KEY_UP;
            case "DOWN" -> GLFW.GLFW_KEY_DOWN;
            case "LEFT" -> GLFW.GLFW_KEY_LEFT;
            case "RIGHT" -> GLFW.GLFW_KEY_RIGHT;
            case "INSERT" -> GLFW.GLFW_KEY_INSERT;
            case "DELETE", "DEL" -> GLFW.GLFW_KEY_DELETE;
            case "HOME" -> GLFW.GLFW_KEY_HOME;
            case "END" -> GLFW.GLFW_KEY_END;
            case "PAGE_UP", "PGUP" -> GLFW.GLFW_KEY_PAGE_UP;
            case "PAGE_DOWN", "PGDN" -> GLFW.GLFW_KEY_PAGE_DOWN;
            case "MINUS" -> GLFW.GLFW_KEY_MINUS;
            case "EQUALS", "PLUS" -> GLFW.GLFW_KEY_EQUAL;
            case "COMMA" -> GLFW.GLFW_KEY_COMMA;
            case "PERIOD", "DOT" -> GLFW.GLFW_KEY_PERIOD;
            case "SLASH" -> GLFW.GLFW_KEY_SLASH;
            case "BACKSLASH" -> GLFW.GLFW_KEY_BACKSLASH;
            case "SEMICOLON" -> GLFW.GLFW_KEY_SEMICOLON;
            case "APOSTROPHE", "QUOTE" -> GLFW.GLFW_KEY_APOSTROPHE;
            case "GRAVE", "GRAVE_ACCENT", "BACKTICK" -> GLFW.GLFW_KEY_GRAVE_ACCENT;
            default -> InputUtil.UNKNOWN_KEY.getCode();
        };
    }
}
