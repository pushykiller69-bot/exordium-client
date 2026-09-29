package com.bred.crystaloptimizer.gui;

import com.bred.crystaloptimizer.config.ModConfig;
import com.bred.crystaloptimizer.payment.FakePayManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ModScreen extends Screen {

    private static final int W = 480, H = 270, SIDEBAR_W = 100, HEADER_H = 24, ACCENT_H = 2;
    private static final int PADDING = 8, ELEMENT_HEIGHT = 14, SPACING = 18;

    private static final int C_BG = 0xF0101318, C_SIDEBAR = 0xF0181C22, C_HEADER = 0xF0141820;
    private static final int C_ACCENT = 0xFF00E5FF, C_ACCENT2 = 0xFF00FFF0, C_BORDER = 0xFF1E2530;
    private static final int C_TEXT = 0xFFCCCCCC, C_TEXT_DIM = 0xFF666C77;
    private static final int C_ON = 0xFF00E5FF, C_OFF = 0xFF445566;
    private static final int C_WARN = 0xFFFFAA00, C_ERR = 0xFFFF4455, C_OK = 0xFF44FF88;
    private static final int C_MOD_HL = 0xFF1A2233, C_ORANGE = 0xFFFF8800, C_V2 = 0xFF88AAFF;

    private static final String WATERMARK = "@adminlafemei on dis";
    private static final String[] TAB_LABELS = {"Spawners", "Fake Pay", "Scoreboard", "Misc"};
    private static final String[] TAB_ICONS  = {"[S]", "[$]", "[#]", "[M]"};

    private int activeTab = 0, px, py;
    private boolean closing = false;

    private TextFieldWidget blockIdField, fakeNameField, scanRangeField;
    private TextFieldWidget recipientField, amountField, fakePayKeyField;
    private TextFieldWidget sbTitleField, sbMoneyField, sbShardsField, sbKillsField;
    private TextFieldWidget sbDeathsField, sbPlaytimeField, sbTeamField, sbFooterField, sbKeyField;
    private TextFieldWidget nameSpooferField;

    public ModScreen() { super(Text.literal("Exordium Client")); }

    @Override
    protected void init() {
        px = (this.width - W) / 2;
        py = (this.height - H) / 2;
        rebuild();
    }

    private void rebuild() {
        this.clearChildren();
        buildSidebar();
        buildContent();
    }

    private void buildSidebar() {
        int tabH = 36;
        for (int i = 0; i < TAB_LABELS.length; i++) {
            final int idx = i;
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal(TAB_ICONS[i] + " " + TAB_LABELS[i]),
                btn -> { saveCurrentTab(); activeTab = idx; rebuild(); }
            ).dimensions(px, py + HEADER_H + ACCENT_H + i * tabH, SIDEBAR_W, tabH - 1).build());
        }
    }

    private void buildContent() {
        switch (activeTab) {
            case 0 -> buildSpawnersTab();
            case 1 -> buildFakePayTab();
            case 2 -> buildScoreboardTab();
            default -> buildMiscTab();
        }
    }

    private int cx() { return px + SIDEBAR_W + PADDING; }
    private int cw() { return W - SIDEBAR_W - (PADDING * 2); }

    private TextFieldWidget field(int x, int y, int w, int maxLen, String val, String ph) {
        TextFieldWidget f = new TextFieldWidget(this.textRenderer, x, y, w, ELEMENT_HEIGHT - 1, Text.empty());
        f.setMaxLength(maxLen);
        f.setText(val != null ? val : "");
        f.setPlaceholder(Text.literal(ph).styled(s -> s.withColor(C_TEXT_DIM)));
        this.addDrawableChild(f);
        return f;
    }

    private void label(int x, int y, String text) {
        int maxW = (px + W) - x - 4;
        this.addDrawableChild(new TextWidget(
            x, y, Math.max(10, maxW), 8,
            Text.literal(text).styled(s -> s.withColor(C_TEXT_DIM)), this.textRenderer));
    }

    private void toggle(int x, int y, int w, String lbl, boolean val, Consumer<ButtonWidget> fn) {
        this.addDrawableChild(ButtonWidget.builder(toggleTxt(lbl, val), fn::accept)
            .dimensions(x, y, w, ELEMENT_HEIGHT).build());
    }

    private Text toggleTxt(String lbl, boolean on) {
        return Text.literal(lbl + "  ").styled(s -> s.withColor(C_TEXT))
            .append(Text.literal(on ? "ON" : "OFF").styled(s -> s.withColor(on ? C_ON : C_OFF)));
    }

    private void saveBtn() {
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Save & Close").styled(s -> s.withColor(C_ACCENT)),
            btn -> saveAndClose()
        ).dimensions(px + SIDEBAR_W + (cw() / 2) - 44, py + H - 20, 88, ELEMENT_HEIGHT).build());
    }

    private void buildSpawnersTab() {
        int x = cx(), w = cw(), half = w / 2 - 3;
        int y = py + HEADER_H + ACCENT_H + PADDING;

        label(x, y, "Block to replace visually"); y += 9;
        blockIdField = field(x, y, w - 60, 200, ModConfig.configuredBlockId, "e.g. minecraft:ice");
        label(x + w - 55, y + 2, "Range");
        scanRangeField = field(x + w - 25, y, 25, 2, String.valueOf(ModConfig.scanRange), "10");
        y += SPACING;

        label(x, y, "Visual Settings"); y += 9;
        toggle(x, y, half, "Tooltips", ModConfig.showTooltips, btn -> {
            ModConfig.showTooltips = !ModConfig.showTooltips; btn.setMessage(toggleTxt("Tooltips", ModConfig.showTooltips)); });
        toggle(x + half + 6, y, half, "Custom Name", ModConfig.showCustomName, btn -> {
            ModConfig.showCustomName = !ModConfig.showCustomName; btn.setMessage(toggleTxt("Custom Name", ModConfig.showCustomName)); });
        y += SPACING;

        toggle(x, y, half, "Show Others", ModConfig.showWhenPlacedByOthers, btn -> {
            ModConfig.showWhenPlacedByOthers = !ModConfig.showWhenPlacedByOthers; btn.setMessage(toggleTxt("Show Others", ModConfig.showWhenPlacedByOthers)); });
        toggle(x + half + 6, y, half, "Panic Mode", ModConfig.panicMode, btn -> {
            ModConfig.panicMode = !ModConfig.panicMode; btn.setMessage(toggleTxt("Panic Mode", ModConfig.panicMode)); });
        y += SPACING;

        label(x, y, "Fake Mob Tooltip Name"); y += 9;
        toggle(x, y, half, "Enabled", ModConfig.fakeNameEnabled, btn -> {
            ModConfig.fakeNameEnabled = !ModConfig.fakeNameEnabled; btn.setMessage(toggleTxt("Enabled", ModConfig.fakeNameEnabled)); });
        fakeNameField = field(x + half + 6, y, half, 50, ModConfig.fakeMobName, "e.g. Zombie");
        y += SPACING;

        label(x, y, "Replacement Logic"); y += 9;
        toggle(x, y, half, "Block->Spawner", ModConfig.showPlacedAsSpawner, btn -> {
            ModConfig.showPlacedAsSpawner = !ModConfig.showPlacedAsSpawner; btn.setMessage(toggleTxt("Block->Spawner", ModConfig.showPlacedAsSpawner)); });

        saveBtn();
    }

    private void buildFakePayTab() {
        int x = cx(), w = cw(), half = w / 2 - 3;
        int y = py + HEADER_H + ACCENT_H + PADDING;

        label(x, y, "Intercept /pay command"); y += 9;
        int keyW = 40;
        toggle(x, y, w - keyW - 4, "Fake Pay Intercept", ModConfig.fakePayEnabled, btn -> {
            ModConfig.fakePayEnabled = !ModConfig.fakePayEnabled; btn.setMessage(toggleTxt("Fake Pay Intercept", ModConfig.fakePayEnabled)); });
        fakePayKeyField = field(x + w - keyW, y, keyW, 20, ModConfig.fakePayToggleKey.equals("NONE") ? "" : ModConfig.fakePayToggleKey, "NONE");
        y += SPACING;

        label(x, y, "Recipient & Amount"); y += 9;
        recipientField = field(x, y, half, 64, ModConfig.fakePayRecipient, "Steve");
        amountField = field(x + half + 6, y, half, 20, ModConfig.fakePayAmount, "500k");
        y += SPACING;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Send Manual Fake Pay").styled(s -> s.withColor(C_OK)), btn -> sendFakePay())
            .dimensions(x, y, w, ELEMENT_HEIGHT).build());

        y += SPACING + 10;
        label(x, y, "Info: Fake Pay deducts from your Fake Scoreboard balance.");
        y += 10;
        label(x, y, "Supports suffixes: K (kilo), M (million), B (billion), T (trillion)");

        saveBtn();
    }

    private void buildScoreboardTab() {
        int x = cx(), w = cw(), half = w / 2 - 3;
        int y = py + HEADER_H + ACCENT_H + PADDING;

        label(x, y, "Scoreboard Status & Toggle Key"); y += 9;
        int keyW = 40;
        toggle(x, y, w - keyW - 4, "Fake Scoreboard", ModConfig.fakeScoreboardActive, btn -> {
            ModConfig.fakeScoreboardActive = !ModConfig.fakeScoreboardActive; btn.setMessage(toggleTxt("Fake Scoreboard", ModConfig.fakeScoreboardActive)); });
        sbKeyField = field(x + w - keyW, y, keyW, 20, ModConfig.sbToggleKey.equals("NONE") ? "" : ModConfig.sbToggleKey, "NONE");
        y += SPACING;

        label(x, y, "Main Display"); y += 9;
        sbTitleField = field(x, y, half, 32, ModConfig.fakeScoreboardTitle, "Title: Donut SMP");
        sbMoneyField = field(x + half + 6, y, half, 20, ModConfig.fakeScoreboardMoney, "Money: 1.5M");
        y += SPACING;

        label(x, y, "Values"); y += 9;
        int q = (w - 4) / 3;
        sbShardsField = field(x, y, q, 20, ModConfig.fakeScoreboardShards, "Shards");
        sbKillsField  = field(x + q + 2, y, q, 20, ModConfig.fakeScoreboardKills, "Kills");
        sbDeathsField = field(x + q * 2 + 4, y, q, 20, ModConfig.fakeScoreboardDeaths, "Deaths");
        y += SPACING;

        sbPlaytimeField = field(x, y, q, 20, ModConfig.fakeScoreboardPlaytime, "Playtime");
        sbTeamField     = field(x + q + 2, y, q, 32, ModConfig.fakeScoreboardTeam, "Team");
        sbFooterField   = field(x + q * 2 + 4, y, q, 64, ModConfig.fakeScoreboardFooter, "Region");
        y += SPACING;

        label(x, y, "Display Toggles (2-column grid)"); y += 9;
        toggle(x, y, half, "Show Money", ModConfig.sbShowMoney, btn -> { ModConfig.sbShowMoney = !ModConfig.sbShowMoney; btn.setMessage(toggleTxt("Show Money", ModConfig.sbShowMoney)); });
        toggle(x+half+6, y, half, "Show Shards", ModConfig.sbShowShards, btn -> { ModConfig.sbShowShards = !ModConfig.sbShowShards; btn.setMessage(toggleTxt("Show Shards", ModConfig.sbShowShards)); });
        y += SPACING - 4;
        toggle(x, y, half, "Show Kills", ModConfig.sbShowKills, btn -> { ModConfig.sbShowKills = !ModConfig.sbShowKills; btn.setMessage(toggleTxt("Show Kills", ModConfig.sbShowKills)); });
        toggle(x+half+6, y, half, "Show Deaths", ModConfig.sbShowDeaths, btn -> { ModConfig.sbShowDeaths = !ModConfig.sbShowDeaths; btn.setMessage(toggleTxt("Show Deaths", ModConfig.sbShowDeaths)); });
        y += SPACING - 4;
        toggle(x, y, half, "Show Keyall", ModConfig.sbShowKeyall, btn -> { ModConfig.sbShowKeyall = !ModConfig.sbShowKeyall; btn.setMessage(toggleTxt("Show Keyall", ModConfig.sbShowKeyall)); });
        toggle(x+half+6, y, half, "Show Playtime", ModConfig.sbShowPlaytime, btn -> { ModConfig.sbShowPlaytime = !ModConfig.sbShowPlaytime; btn.setMessage(toggleTxt("Show Playtime", ModConfig.sbShowPlaytime)); });
        y += SPACING - 4;
        toggle(x, y, half, "Show Team", ModConfig.sbShowTeam, btn -> { ModConfig.sbShowTeam = !ModConfig.sbShowTeam; btn.setMessage(toggleTxt("Show Team", ModConfig.sbShowTeam)); });
        toggle(x+half+6, y, half, "Show Region", ModConfig.sbShowRegion, btn -> { ModConfig.sbShowRegion = !ModConfig.sbShowRegion; btn.setMessage(toggleTxt("Show Region", ModConfig.sbShowRegion)); });

        saveBtn();
    }

    private void buildMiscTab() {
        int x = cx(), w = cw();
        int y = py + HEADER_H + ACCENT_H + PADDING;

        label(x, y, "Name Spoofer (Client-side Only)"); y += 9;
        toggle(x, y, w, "Enabled", ModConfig.nameSpooferEnabled, btn -> {
            ModConfig.nameSpooferEnabled = !ModConfig.nameSpooferEnabled;
            btn.setMessage(toggleTxt("Enabled", ModConfig.nameSpooferEnabled)); rebuild(); });
        y += SPACING;

        label(x, y, "Spoofed Name"); y += 9;
        nameSpooferField = field(x, y, w - 60, 16, ModConfig.spoofedName, "e.g. DrDonutt");
        nameSpooferField.setEditable(ModConfig.nameSpooferEnabled);
        if (ModConfig.nameSpooferEnabled) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Apply").styled(s -> s.withColor(C_ACCENT)), btn -> {
                ModConfig.spoofedName = nameSpooferField.getText().trim();
                nameSpooferField.setEditable(false); rebuild();
            }).dimensions(x + w - 56, y, 56, ELEMENT_HEIGHT - 1).build());
        }
        y += SPACING + 10;
        this.addDrawableChild(new TextWidget(x, y, w, 9,
            Text.literal("! Server still sees your real name !").styled(s -> s.withColor(C_ERR)), this.textRenderer));
        y += 11;
        this.addDrawableChild(new TextWidget(x, y, w, 9,
            Text.literal("Covers: nametag | chat | F5 | tab list").styled(s -> s.withColor(C_TEXT_DIM)), this.textRenderer));

        saveBtn();
    }

    private void sendFakePay() {
        if (this.client == null || this.client.player == null) return;
        saveCurrentTab();
        String rec = ModConfig.fakePayRecipient != null ? ModConfig.fakePayRecipient.trim() : "";
        String amt = ModConfig.fakePayAmount != null ? ModConfig.fakePayAmount.trim() : "";
        FakePayManager.sendManualFakePay(rec, amt);
    }

    private void saveCurrentTab() {
        switch (activeTab) {
            case 0 -> {
                if (blockIdField != null) ModConfig.setConfiguredBlockId(blockIdField.getText());
                if (fakeNameField != null) ModConfig.fakeMobName = fakeNameField.getText().trim();
                if (scanRangeField != null) {
                    try {
                        int r = Integer.parseInt(scanRangeField.getText().trim());
                        if (r > 0 && r <= 64) ModConfig.scanRange = r;
                    } catch (NumberFormatException ignored) {}
                }
            }
            case 1 -> {
                if (recipientField != null) ModConfig.fakePayRecipient = recipientField.getText().trim();
                if (amountField != null) ModConfig.fakePayAmount = amountField.getText().trim();
                if (fakePayKeyField != null) { String k = fakePayKeyField.getText().trim().toUpperCase(); ModConfig.fakePayToggleKey = k.isEmpty() ? "NONE" : k; }
            }
            case 2 -> {
                if (sbKeyField != null) { String k = sbKeyField.getText().trim().toUpperCase(); ModConfig.sbToggleKey = k.isEmpty() ? "NONE" : k; }
                if (sbTitleField != null) ModConfig.fakeScoreboardTitle = sbTitleField.getText().trim();
                if (sbMoneyField != null) {
                    String newMoney = sbMoneyField.getText().trim();
                    if (!newMoney.equals(ModConfig.fakeScoreboardMoney)) {
                        ModConfig.fakeScoreboardMoney = newMoney;
                        ModConfig.fakeScoreboardBalance = 0;
                        ModConfig.fakeScoreboardBalanceInitialized = false;
                    }
                }
                if (sbShardsField != null) ModConfig.fakeScoreboardShards = sbShardsField.getText().trim();
                if (sbKillsField != null) ModConfig.fakeScoreboardKills = sbKillsField.getText().trim();
                if (sbDeathsField != null) ModConfig.fakeScoreboardDeaths = sbDeathsField.getText().trim();
                if (sbPlaytimeField != null) ModConfig.fakeScoreboardPlaytime = sbPlaytimeField.getText().trim();
                if (sbTeamField != null) ModConfig.fakeScoreboardTeam = sbTeamField.getText().trim();
                if (sbFooterField != null) ModConfig.fakeScoreboardFooter = sbFooterField.getText().trim();
            }
            case 3 -> { if (nameSpooferField != null) ModConfig.spoofedName = nameSpooferField.getText().trim(); }
        }
        ModConfig.save();
    }

    private void saveAndClose() {
        if (closing) return;
        closing = true;
        saveCurrentTab();
        if (this.client != null) this.client.setScreen(null);
    }

    @Override public void removed() { if (!closing) saveCurrentTab(); }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(px, py, px+W, py+H, C_BG);
        ctx.fill(px, py, px+W, py+1, C_BORDER);
        ctx.fill(px, py+H-1, px+W, py+H, C_BORDER);
        ctx.fill(px, py, px+1, py+H, C_BORDER);
        ctx.fill(px+W-1, py, px+W, py+H, C_BORDER);
        ctx.fill(px, py, px+W, py+HEADER_H, C_HEADER);
        drawGradH(ctx, px, py+HEADER_H, W, C_ACCENT, C_ACCENT2);
        ctx.fill(px, py+HEADER_H+ACCENT_H, px+SIDEBAR_W, py+H, C_SIDEBAR);
        ctx.fill(px+SIDEBAR_W, py+HEADER_H+ACCENT_H, px+SIDEBAR_W+1, py+H, C_BORDER);

        int tabH = 36;
        for (int i = 0; i < TAB_LABELS.length; i++) {
            int ty = py + HEADER_H + ACCENT_H + i * tabH;
            if (i == activeTab) {
                ctx.fill(px, ty, px + 2, ty + tabH - 1, C_ACCENT);
                ctx.fill(px + 2, ty, px + SIDEBAR_W, ty + tabH - 1, C_MOD_HL);
            }
        }

        int tx = px + 8, ty2 = py + 7;
        tx = drawGradTxt(ctx, "Exordium Client", tx, ty2, C_ACCENT, C_ACCENT2);
        ctx.drawText(this.textRenderer, Text.literal(" beta").styled(s -> s.withColor(C_ORANGE)), tx, ty2+5, C_ORANGE, false);
        tx += this.textRenderer.getWidth(" beta");
        ctx.drawText(this.textRenderer, Text.literal(" v2").styled(s -> s.withColor(C_V2)), tx, ty2+5, C_V2, false);

        boolean panic = ModConfig.panicMode;
        int sc = panic ? C_ERR : C_OK;
        ctx.fill(px+W-54, py+4, px+W-4, py+HEADER_H-4, panic ? 0x33FF0000 : 0x1100FF88);
        ctx.drawText(this.textRenderer, panic ? "PANIC" : "ACTIVE",
            px+W-51+(46-this.textRenderer.getWidth(panic?"PANIC":"ACTIVE"))/2, py+9, sc, false);

        ctx.drawText(this.textRenderer,
            Text.literal(WATERMARK).styled(s -> s.withColor(0xFF4488FF)),
            px + W - 4 - this.textRenderer.getWidth(WATERMARK), py + H - 10, 0xFF4488FF, false);

        if (activeTab == 3 && ModConfig.nameSpooferEnabled && ModConfig.spoofedName != null && !ModConfig.spoofedName.isEmpty()) {
            ctx.drawText(this.textRenderer, "Playing as: " + ModConfig.spoofedName, px + 4, py + H - 10, 0xFFFFCC44, false);
        }

        super.render(ctx, mx, my, delta);
    }

    private void drawGradH(DrawContext ctx, int x, int y, int w, int c1, int c2) {
        for (int i = 0; i < w; i++) {
            float t = (float)i / Math.max(w-1, 1);
            ctx.fill(x+i, y, x+i+1, y+ACCENT_H,
                0xFF000000
                | (lerp((c1>>16)&0xFF, (c2>>16)&0xFF, t) << 16)
                | (lerp((c1>>8)&0xFF,  (c2>>8)&0xFF,  t) << 8)
                |  lerp(c1&0xFF,       c2&0xFF,        t));
        }
    }

    private int drawGradTxt(DrawContext ctx, String text, int x, int y, int c1, int c2) {
        for (int i = 0; i < text.length(); i++) {
            float t = (float)i / Math.max(text.length()-1, 1);
            int col = 0xFF000000
                | (lerp((c1>>16)&0xFF, (c2>>16)&0xFF, t) << 16)
                | (lerp((c1>>8)&0xFF,  (c2>>8)&0xFF,  t) << 8)
                |  lerp(c1&0xFF,       c2&0xFF,        t);
            String ch = String.valueOf(text.charAt(i));
            ctx.drawText(this.textRenderer, ch, x, y, col, false);
            x += this.textRenderer.getWidth(ch);
        }
        return x;
    }

    private static int lerp(int a, int b, float t) { return Math.round(a + (b - a) * t); }

    @Override public boolean shouldPause() { return false; }
}
