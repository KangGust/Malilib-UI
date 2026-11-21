package kang.malilib.ui.gui;

import fi.dy.masa.malilib.util.StringUtils;

/** 语言串（assets/malilib-ui/lang/*.json），缺失时回退成 key 本身 */
public final class Text {
    private Text() {
    }

    public static String of(String key, Object... args) {
        return StringUtils.translate(key, args);
    }
}
