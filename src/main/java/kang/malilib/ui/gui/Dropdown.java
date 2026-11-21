package kang.malilib.ui.gui;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** 自绘下拉框：圆角、悬停高亮、超出高度自动滚动的弹出列表，点外部关闭 */
public final class Dropdown {
    private static final int ITEM_H = 18;

    public final List<String> options;
    public int selected;
    public boolean open;
    private float scroll;
    private int popupX;
    private int popupY;
    private int popupW;
    private int popupH;
    private int boxX;
    private int boxY;
    private int boxW;
    private int boxH;

    public Dropdown(List<String> options, int selected) {
        this.options = options;
        this.selected = Math.max(0, Math.min(selected, options.size() - 1));
    }

    public String label() {
        return this.options.isEmpty() ? "" : this.options.get(this.selected);
    }

    /** 画主框（弹出列表在 drawPopup 里画，保证盖在其他控件之上） */
    public void drawBox(GuiGraphicsExtractor g, int x, int y, int w, int h, int mouseX, int mouseY) {
        this.boxX = x;
        this.boxY = y;
        this.boxW = w;
        this.boxH = h;
        boolean hovered = in(mouseX, mouseY, x, y, w, h);
        Draw.roundRect(g, x, y, w, h, Math.min(Theme.radius(), h / 2),
                hovered || this.open ? Theme.surfaceHigh() : Theme.surface());
        Draw.text(g, Draw.trim(label(), w - 22), x + 6, y + (h - 8) / 2, Theme.textDim());
        Icons.icon(g, this.open ? "chevron-up" : "chevron-down", x + w - 13, y + (h - 10) / 2, 10, Theme.textOff());
    }

    public void drawPopup(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (!this.open) {
            return;
        }
        int visible = Math.min(this.options.size(), 9);
        this.popupX = this.boxX;
        this.popupY = this.boxY + this.boxH + 2;
        this.popupW = this.boxW;
        this.popupH = visible * ITEM_H + 6;
        float max = Math.max(0, this.options.size() * ITEM_H - visible * ITEM_H);
        this.scroll = Math.max(0, Math.min(this.scroll, max));

        Draw.shadow(g, this.popupX, this.popupY, this.popupW, this.popupH, 6);
        Draw.roundRect(g, this.popupX, this.popupY, this.popupW, this.popupH, 6, Theme.popup());

        Draw.scissor(g, this.popupX + 2, this.popupY + 3, this.popupW - 4, this.popupH - 6);
        int first = (int) (this.scroll / ITEM_H);
        for (int i = first; i < this.options.size() && i < first + visible + 1; i++) {
            int y = this.popupY + 3 + i * ITEM_H - (int) this.scroll;
            boolean hovered = in(mouseX, mouseY, this.popupX + 3, y, this.popupW - 6, ITEM_H);
            if (hovered) {
                Draw.roundRect(g, this.popupX + 3, y, this.popupW - 6, ITEM_H, 4, Theme.surfaceHigh());
            }
            if (i == this.selected) {
                Draw.roundRect(g, this.popupX + 3, y + 5, 2, ITEM_H - 10, 1, Theme.accent());
            }
            Draw.text(g, Draw.trim(this.options.get(i), this.popupW - 22), this.popupX + 10, y + 5,
                    i == this.selected ? Theme.text() : Theme.textDim());
        }
        g.disableScissor();
    }

    /** 返回 true 表示这次点击被下拉消费 */
    public boolean mouseClicked(double mouseX, double mouseY) {
        if (this.open) {
            if (in(mouseX, mouseY, this.popupX, this.popupY, this.popupW, this.popupH)) {
                int index = (int) ((mouseY - this.popupY - 3 + this.scroll) / ITEM_H);
                if (index >= 0 && index < this.options.size()) {
                    this.selected = index;
                }
                this.open = false;
                return true;
            }
            this.open = false;
            return false;
        }
        if (in(mouseX, mouseY, this.boxX, this.boxY, this.boxW, this.boxH)) {
            this.open = true;
            this.scroll = Math.max(0, this.selected * ITEM_H - ITEM_H * 3);
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.open && in(mouseX, mouseY, this.popupX, this.popupY, this.popupW, this.popupH)) {
            this.scroll -= (float) (amount * ITEM_H);
            return true;
        }
        return false;
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
