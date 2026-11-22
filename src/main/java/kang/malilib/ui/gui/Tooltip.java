package kang.malilib.ui.gui;

import java.util.ArrayList;
import java.util.List;

import fi.dy.masa.malilib.render.GuiContext;

/** 统一的功能提示框：圆角半透明卡片、首行高亮、自动换行、贴近屏幕边缘自动翻转 */
public final class Tooltip {
    private static final int MAX_WIDTH = 210;
    private static final int PAD = 6;
    private static final int LINE = 10;

    private Tooltip() {
    }

    public static void draw(GuiContext ctx, int mouseX, int mouseY, List<String> rawLines) {
        List<String> lines = new ArrayList<>();
        for (String line : rawLines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            lines.addAll(wrap(strip(line)));
        }
        if (lines.isEmpty()) {
            return;
        }
        int w = 0;
        for (String line : lines) {
            w = Math.max(w, Draw.width(line));
        }
        w += PAD * 2 + 3;
        int h = lines.size() * LINE + PAD * 2 - 2;

        int x = mouseX + 10;
        int y = mouseY - 12;
        if (x + w > ctx.guiWidth() - 4) {
            x = ctx.guiWidth() - w - 4;
        }
        if (y + h > ctx.guiHeight() - 4) {
            y = ctx.guiHeight() - h - 4;
        }
        if (y < 4) {
            y = 4;
        }

        Draw.shadow(ctx, x, y, w, h, 6);
        Draw.roundRect(ctx, x, y, w, h, 6, Theme.withAlpha(Theme.baseRgb(), 238));
        Draw.roundRect(ctx, x + 3, y + PAD, 2, h - PAD * 2, 1, Theme.accent());

        int textY = y + PAD - 1;
        for (int i = 0; i < lines.size(); i++) {
            Draw.text(ctx, lines.get(i), x + PAD + 4, textY, i == 0 ? Theme.text() : Theme.textDim());
            textY += LINE;
        }
    }

    private static List<String> wrap(String line) {
        List<String> out = new ArrayList<>();
        if (Draw.width(line) <= MAX_WIDTH) {
            out.add(line);
            return out;
        }
        StringBuilder current = new StringBuilder();
        for (String word : line.split(" ")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (Draw.width(candidate) > MAX_WIDTH && !current.isEmpty()) {
                out.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (!current.isEmpty()) {
            out.add(current.toString());
        }
        return out;
    }

    private static String strip(String text) {
        return text.replaceAll("§.", "");
    }
}
