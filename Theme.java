package com.kercher;

import java.awt.Color;

public enum Theme {
    KERCHER(0xB44CFF, 0xFF4C9A),
    OCEAN(0x2EC4FF, 0x3A5BFF),
    TOXIC(0x7CFF4C, 0x18C47A),
    SUNSET(0xFF9A3C, 0xFF3C6E),
    RAINBOW(0, 0);

    private final int a, b;

    Theme(int a, int b) { this.a = a; this.b = b; }

    public Theme next() { return values()[(ordinal() + 1) % values().length]; }

    /** phase: любое число, анимация идёт по синусоиде (или по hue для RAINBOW). Возвращает ARGB. */
    public int color(double phase) {
        if (this == RAINBOW) {
            float h = (float) (((phase % 1.0) + 1.0) % 1.0);
            return 0xFF000000 | (Color.HSBtoRGB(h, 0.65f, 1f) & 0xFFFFFF);
        }
        double t = (Math.sin(phase * Math.PI * 2) + 1) / 2;
        return 0xFF000000 | lerp(a, b, t);
    }

    private static int lerp(int c1, int c2, double t) {
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int bl = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }

    public static int withAlpha(int rgb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }
}
