package kang.malilib.ui.gui;

import kang.malilib.ui.config.UiConfigs;

/**
 * 渲染作用域开关：
 * 只有我们的主界面在显示时才为 true，所有“接管 malilib 控件样式”的 mixin 都先看这个标记，
 * 保证完全不碰其它界面 / 其它模组。
 */
public final class RowScope {
    private static boolean active;
    public static String currentModId = "";
    private static boolean inRow;

    private RowScope() {
    }

    public static boolean active() {
        return active;
    }

    public static void setActive(boolean value) {
        active = value;
        if (!value) {
            inRow = false;
        }
    }

    public static boolean inRow() {
        return inRow;
    }

    /**
     * 是否允许接管控件样式：
     * - 在本界面：跟随「完整接管」开关（关掉就完全退回 malilib 原版，控件也不再改）
     * - 在其它界面：只看「接管其它界面控件」
     */
    public static boolean styleAllowed() {
        return active
                ? UiConfigs.General.ENABLED.getBooleanValue()
                : UiConfigs.General.STYLE_OTHER.getBooleanValue();
    }

    /** 是否允许“完整接管”本界面（行外观、列表、搜索栏等） */
    public static boolean full() {
        return active && UiConfigs.General.ENABLED.getBooleanValue();
    }

    public static void setInRow(boolean value) {
        inRow = value;
    }
}
