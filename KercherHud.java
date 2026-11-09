package com.kercher;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class KercherHud {
    private static final List<Long> HITS = new ArrayList<>();
    private static final long HIT_LIFE_MS = 450;

    public static void addHit() {
        HITS.add(Util.getMeasuringTimeMs());
    }

    public static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!KercherVisualClient.enabled || mc.player == null || mc.options.hudHidden) return;

        double time = Util.getMeasuringTimeMs() / 1000.0;
        Theme theme = KercherVisualClient.theme;
        TextRenderer tr = mc.textRenderer;

        drawLowHealthVignette(ctx, mc, theme, time);
        drawWatermark(ctx, mc, tr, theme, time);
        drawKeystrokes(ctx, mc, tr, theme, time);
        drawHitRing(ctx, mc, theme, time);
    }

    // ---------- Watermark ----------
    private static void drawWatermark(DrawContext ctx, MinecraftClient mc, TextRenderer tr, Theme theme, double time) {
        int x = 6, y = 6;
        int fps = mc.getCurrentFps();
        int ping = 0;
        if (mc.getNetworkHandler() != null) {
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (e != null) ping = e.getLatency();
        }
        String info = "  |  " + fps + " fps  |  " + ping + " ms  |  "
                + (int) mc.player.getX() + " " + (int) mc.player.getY() + " " + (int) mc.player.getZ();
        String name = "Kercher";

        int nameW = tr.getWidth(name);
        int w = nameW + tr.getWidth(info) + 10;
        int h = 16;

        ctx.fill(x, y, x + w, y + h, 0xB0101018);

        // Анимированная бегущая линия сверху
        for (int i = 0; i < w; i++) {
            int c = theme.color(time * 0.5 - i * 0.004);
            ctx.fill(x + i, y, x + i + 1, y + 2, c);
        }

        // Название: каждый символ со своей фазой
        int cx = x + 5;
        for (int i = 0; i < name.length(); i++) {
            String s = String.valueOf(name.charAt(i));
            ctx.drawText(tr, s, cx, y + 5, theme.color(time * 0.6 - i * 0.08), true);
            cx += tr.getWidth(s);
        }
        ctx.drawText(tr, info, cx, y + 5, 0xFFD8D8E0, true);
    }

    // ---------- Keystrokes ----------
    private static void drawKeystrokes(DrawContext ctx, MinecraftClient mc, TextRenderer tr, Theme theme, double time) {
        int size = 20, gap = 2;
        int baseX = 6;
        int baseY = ctx.getScaledWindowHeight() - (size * 3 + gap * 2) - 6;

        key(ctx, tr, theme, time, mc.options.forwardKey, "W", baseX + size + gap, baseY, size, size);
        key(ctx, tr, theme, time, mc.options.leftKey, "A", baseX, baseY + size + gap, size, size);
        key(ctx, tr, theme, time, mc.options.backKey, "S", baseX + size + gap, baseY + size + gap, size, size);
        key(ctx, tr, theme, time, mc.options.rightKey, "D", baseX + (size + gap) * 2, baseY + size + gap, size, size);
        key(ctx, tr, theme, time, mc.options.jumpKey, "SPACE", baseX, baseY + (size + gap) * 2,
                size * 3 + gap * 2, 14);
    }

    private static void key(DrawContext ctx, TextRenderer tr, Theme theme, double time,
                            KeyBinding kb, String label, int x, int y, int w, int h) {
        boolean down = kb.isPressed();
        int accent = theme.color(time * 0.5 - x * 0.01);
        int bg = down ? Theme.withAlpha(accent, 170) : 0x90101018;
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.fill(x, y + h - 1, x + w, y + h, accent); // нижняя линия
        int tw = tr.getWidth(label);
        ctx.drawText(tr, label, x + (w - tw) / 2, y + (h - 8) / 2, down ? 0xFF101018 : 0xFFFFFFFF, !down);
    }

    // ---------- Hit kercher ----------
    private static void drawHitRing(DrawContext ctx, MinecraftClient mc, Theme theme, double time) {
        long now = Util.getMeasuringTimeMs();
        HITS.removeIf(t -> now - t > HIT_LIFE_MS);

        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;

        for (long t : HITS) {
            double p = (now - t) / (double) HIT_LIFE_MS;      // 0..1
            double eased = 1 - Math.pow(1 - p, 3);             // ease-out
            double radius = 5 + 22 * eased;
            int alpha = (int) (230 * (1 - p));
            int color = Theme.withAlpha(theme.color(time * 0.8 + p), alpha);

            int segments = 80;
            for (int i = 0; i < segments; i++) {
                double a = i * (Math.PI * 2 / segments);
                int px = cx + (int) Math.round(Math.cos(a) * radius);
                int py = cy + (int) Math.round(Math.sin(a) * radius);
                ctx.fill(px, py, px + 1, py + 1, color);
            }
        }
    }

    // ---------- Low health vignette ----------
    private static void drawLowHealthVignette(DrawContext ctx, MinecraftClient mc, Theme theme, double time) {
        float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        float max = mc.player.getMaxHealth();
        float ratio = hp / max;
        if (ratio > 0.4f) return;

        double beat = (Math.sin(time * (3.0 + (0.4 - ratio) * 8.0)) + 1) / 2; // чем меньше HP — тем чаще ритм
        int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
        int layers = 14;
        for (int i = 0; i < layers; i++) {
            int a = (int) ((1 - i / (double) layers) * (30 + 60 * beat) * (1 - ratio / 0.4f * 0.5));
            int c = Theme.withAlpha(0xFF2030, a);
            ctx.fill(0, i * 3, w, i * 3 + 3, c);                 // верх
            ctx.fill(0, h - i * 3 - 3, w, h - i * 3, c);        // низ
            ctx.fill(i * 3, 0, i * 3 + 3, h, c);                 // лево
            ctx.fill(w - i * 3 - 3, 0, w - i * 3, h, c);        // право
        }
    }
}
