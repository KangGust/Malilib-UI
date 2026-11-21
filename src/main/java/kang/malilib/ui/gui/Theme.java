package kang.malilib.ui.gui;

import kang.malilib.ui.config.UiConfigs;

/** 所有颜色/尺寸都由配置实时算出，改配置立刻生效 */
public final class Theme {
    private Theme() {
    }

    public static int accent() {
        return UiConfigs.Colors.ACCENT.getIntegerValue() | 0xFF000000;
    }

    public static int accentSoft(int alpha) {
        return (accent() & 0xFFFFFF) | (alpha << 24);
    }

    public static int panel() {
        int alpha = UiConfigs.Colors.OPACITY.getIntegerValue() * 255 / 100;
        return (baseRgb() & 0xFFFFFF) | (alpha << 24);
    }

    /** 面板基础色（可跟随强调色推导） */
    public static int baseRgb() {
        return UiConfigs.Colors.PANEL_FOLLOW.getBooleanValue()
                ? mix(accent(), 0xFF07090E, 0.86f)
                : UiConfigs.Colors.PANEL.getIntegerValue();
    }

    private static int controlAlpha() {
        return UiConfigs.Colors.CONTROL_OPACITY.getIntegerValue() * 255 / 100;
    }

    public static int surface() {
        return withAlpha(mix(baseRgb(), 0xFFFFFFFF, 0.06f), controlAlpha());
    }

    public static int surfaceHigh() {
        return withAlpha(mix(baseRgb(), 0xFFFFFFFF, 0.12f), controlAlpha());
    }

    public static int withAlpha(int rgb, int alpha) {
        return (rgb & 0xFFFFFF) | (alpha << 24);
    }

    public static int outline() {
        return 0x22FFFFFF;
    }

    public static int text() {
        return UiConfigs.Colors.TEXT.getIntegerValue() | 0xFF000000;
    }

    public static int textDim() {
        return mix(text(), panel(), 0.42f);
    }

    public static int textOff() {
        return mix(text(), panel(), 0.62f);
    }

    public static int scrim() {
        int alpha = UiConfigs.Colors.SCRIM.getIntegerValue() * 255 / 100;
        return alpha << 24;
    }

    /** 弹层用更实的底色，避免透出下层文字显得糊 */
    public static int popup() {
        int base = mix(panel(), 0xFFFFFFFF, 0.045f);
        int alpha = Math.min(255, Math.max(210, (UiConfigs.Colors.OPACITY.getIntegerValue() * 255 / 100) + 46));
        return (base & 0xFFFFFF) | (alpha << 24);
    }

    public static int radius() {
        return UiConfigs.General.CORNER_RADIUS.getIntegerValue();
    }

    public static int sidebarWidth() {
        return UiConfigs.General.SIDEBAR_WIDTH.getIntegerValue();
    }

    public static boolean compact() {
        return UiConfigs.General.COMPACT.getBooleanValue();
    }

    public static boolean icons() {
        return UiConfigs.General.SHOW_ICONS.getBooleanValue();
    }

    /** 线性混合两个 ARGB 颜色（含 alpha） */
    public static int mix(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >>> 24), ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >>> 24), br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int ra = (int) (aa + (ba - aa) * t);
        int rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t);
        int rb = (int) (ab + (bb - ab) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }

    public static int alpha(int argb, float mul) {
        int a = (int) (((argb >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, mul)));
        return (argb & 0xFFFFFF) | (a << 24);
    }
}
