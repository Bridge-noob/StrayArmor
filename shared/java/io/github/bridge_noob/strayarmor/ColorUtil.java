package io.github.bridge_noob.strayarmor;

/**
 * Small ARGB helpers. Colors are kept in the same 0xAARRGGBB format Minecraft
 * uses for model vertex colors.
 */
public final class ColorUtil {

    private ColorUtil() {
    }

    public static int parse(String value, int fallback) {
        if (value == null) return fallback;
        String hex = value.trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.startsWith("0x") || hex.startsWith("0X")) hex = hex.substring(2);
        if (hex.length() != 6 && hex.length() != 8) return fallback;
        try {
            long parsed = Long.parseLong(hex, 16);
            if (hex.length() == 6) parsed |= 0xFF000000L;
            return (int) parsed;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Blends a color toward white. A strength of 1 keeps the full color, while
     * lower values produce a lighter wash that still lets the armor texture
     * shading show through.
     */
    public static int wash(int color, float strength) {
        float s = Math.max(0.0F, Math.min(1.0F, strength));
        int a = (color >>> 24) & 0xFF;
        int r = (color >>> 16) & 0xFF;
        int g = (color >>> 8) & 0xFF;
        int b = color & 0xFF;
        int wr = Math.round(255.0F * (1.0F - s) + r * s);
        int wg = Math.round(255.0F * (1.0F - s) + g * s);
        int wb = Math.round(255.0F * (1.0F - s) + b * s);
        return (a << 24) | (wr << 16) | (wg << 8) | wb;
    }

    /**
     * Forces the color to full saturation and full brightness while keeping
     * its hue. This turns washed-out colors such as {@code #FF3B3B} into a
     * pure red, so the armor reads as that color instead of a pale tint.
     */
    public static int vivid(int color) {
        int a = (color >>> 24) & 0xFF;
        int r = (color >>> 16) & 0xFF;
        int g = (color >>> 8) & 0xFF;
        int b = color & 0xFF;

        float rf = r / 255.0F;
        float gf = g / 255.0F;
        float bf = b / 255.0F;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        if (max == 0.0F) return color; // keep black as-is

        float hue;
        if (max == min) {
            hue = 0.0F;
        } else if (max == rf) {
            hue = (gf - bf) / (max - min);
        } else if (max == gf) {
            hue = 2.0F + (bf - rf) / (max - min);
        } else {
            hue = 4.0F + (rf - gf) / (max - min);
        }
        hue = (hue % 6.0F + 6.0F) % 6.0F;

        int sector = (int) Math.floor(hue);
        float frac = hue - sector;
        float p = 0.0F;          // v * (1 - s), s = 1
        float q = 1.0F - frac;   // v * (1 - s * f)
        float t = frac;          // v * (1 - s * (1 - f))

        float nr;
        float ng;
        float nb;
        switch (sector % 6) {
            case 0 -> { nr = 1.0F; ng = t; nb = p; }
            case 1 -> { nr = q; ng = 1.0F; nb = p; }
            case 2 -> { nr = p; ng = 1.0F; nb = t; }
            case 3 -> { nr = p; ng = q; nb = 1.0F; }
            case 4 -> { nr = t; ng = p; nb = 1.0F; }
            default -> { nr = 1.0F; ng = p; nb = q; }
        }

        int outR = Math.round(nr * 255.0F);
        int outG = Math.round(ng * 255.0F);
        int outB = Math.round(nb * 255.0F);
        return (a << 24) | (outR << 16) | (outG << 8) | outB;
    }

    /** Multiplies two ARGB colors channel-by-channel. */
    public static int multiply(int base, int tint) {
        if (base == 0 || tint == 0) return base;
        int a = (base >>> 24) & 0xFF;
        int r = ((base >>> 16) & 0xFF) * ((tint >>> 16) & 0xFF) / 255;
        int g = ((base >>> 8) & 0xFF) * ((tint >>> 8) & 0xFF) / 255;
        int b = (base & 0xFF) * (tint & 0xFF) / 255;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
