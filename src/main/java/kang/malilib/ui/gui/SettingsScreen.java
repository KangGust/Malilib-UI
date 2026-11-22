package kang.malilib.ui.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import kang.malilib.ui.config.UiConfigs;
import kang.malilib.ui.config.UiStore;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** 界面设置：分区排版，所有控件在绘制时就登记点击区，点哪就是哪 */
public class SettingsScreen extends Screen {
    private static final int[] ACCENTS = {
            0xFF7FB4FF, 0xFF9C8CFF, 0xFFB39DDB, 0xFF7FD8A0, 0xFF6FD3E8,
            0xFFFFC46B, 0xFFFF9E9E, 0xFFF48FB1, 0xFFE6E6E6, 0xFF9AA3B2
    };
    private static final int LABEL_W = 118;

    private record Hit(int x, int y, int w, int h, Runnable action) {
    }

    private final Screen parent;
    private final List<Hit> hits = new ArrayList<>();
    private final List<Hit> fixedHits = new ArrayList<>();
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int cursorY;
    private int closeX;
    private int closeY;

    private IntConsumer activeSlider;
    private int activeX;
    private int activeW;
    private int activeMin;
    private int activeMax;
    private float scroll;
    private int contentH;
    private boolean dragging;
    private boolean dragMoved;
    private double lastDragY;
    private double pressY;
    private int viewTop;
    private int viewH;

    public SettingsScreen(Screen parent) {
        super(Component.literal("Malilib UI"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.panelW = Math.min(440, this.width - 24);
        this.panelH = Math.min(424, this.height - 24);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
    }

    private int left() {
        return this.panelX + 16;
    }

    private int contentW() {
        return this.panelW - 32;
    }

    /* ── 绘制 ─────────────────────────────────────────────── */

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
        this.hits.clear();
        this.fixedHits.clear();
        Draw.rect(g, 0, 0, this.width, this.height, Theme.scrim());
        Draw.shadow(g, this.panelX, this.panelY, this.panelW, this.panelH, Theme.radius());
        Draw.roundRect(g, this.panelX, this.panelY, this.panelW, this.panelH, Theme.radius(), Theme.panel());

        Draw.textShadow(g, Text.of("malilib-ui.settings.title"), this.left(), this.panelY + 13, Theme.text());
        this.closeX = this.panelX + this.panelW - 26;
        this.closeY = this.panelY + 9;
        Draw.roundRect(g, this.closeX, this.closeY, 16, 16, 4,
                this.hover(mouseX, mouseY, this.closeX, this.closeY, 16, 16) ? Theme.surfaceHigh() : Theme.surface());
        Icons.iconCentered(g, "x", this.closeX, this.closeY, 16, 16, 9, Theme.textDim());

        this.cursorY = this.panelY + 34;
        this.viewTop = this.panelY + 32;
        int viewBottom = this.panelY + this.panelH - 36;
        this.viewH = Math.max(40, viewBottom - this.viewTop);
        this.clampScroll();
        this.cursorY = this.viewTop - (int) this.scroll;
        Draw.scissor(g, this.panelX + 2, this.viewTop, this.panelW - 4, this.viewH);
        this.section(g, "malilib-ui.settings.appearance");
        this.swatches(g, mouseX, mouseY);
        this.slider(g, "malilib-ui.settings.opacity", UiConfigs.Colors.OPACITY.getIntegerValue(), 30, 100,
                value -> UiConfigs.Colors.OPACITY.setIntegerValue(value));
        this.resetButton(g, this.cursorY - 24, mouseX, mouseY, UiConfigs.Colors.OPACITY);
        this.slider(g, "malilib-ui.settings.controlOpacity", UiConfigs.Colors.CONTROL_OPACITY.getIntegerValue(), 20, 100,
                value -> UiConfigs.Colors.CONTROL_OPACITY.setIntegerValue(value));
        this.resetButton(g, this.cursorY - 24, mouseX, mouseY, UiConfigs.Colors.CONTROL_OPACITY);
        this.slider(g, "malilib-ui.settings.scrim", UiConfigs.Colors.SCRIM.getIntegerValue(), 0, 100,
                value -> UiConfigs.Colors.SCRIM.setIntegerValue(value));
        this.resetButton(g, this.cursorY - 24, mouseX, mouseY, UiConfigs.Colors.SCRIM);
        this.slider(g, "malilib-ui.settings.radius", UiConfigs.General.CORNER_RADIUS.getIntegerValue(), 0, 16,
                value -> UiConfigs.General.CORNER_RADIUS.setIntegerValue(value));
        this.resetButton(g, this.cursorY - 24, mouseX, mouseY, UiConfigs.General.CORNER_RADIUS);
        this.slider(g, "malilib-ui.settings.sidebar", UiConfigs.General.SIDEBAR_WIDTH.getIntegerValue(), 140, 320,
                value -> UiConfigs.General.SIDEBAR_WIDTH.setIntegerValue(value));
        this.resetButton(g, this.cursorY - 24, mouseX, mouseY, UiConfigs.General.SIDEBAR_WIDTH);

        this.section(g, "malilib-ui.settings.display");
        this.toggle(g, "malilib-ui.settings.icons", UiConfigs.General.SHOW_ICONS.getBooleanValue(),
                () -> UiConfigs.General.SHOW_ICONS.setBooleanValue(!UiConfigs.General.SHOW_ICONS.getBooleanValue()));
        this.resetButton(g, this.cursorY - 22, mouseX, mouseY, UiConfigs.General.SHOW_ICONS);
        this.toggle(g, "malilib-ui.settings.blur", UiConfigs.General.BLUR.getBooleanValue(),
                () -> UiConfigs.General.BLUR.setBooleanValue(!UiConfigs.General.BLUR.getBooleanValue()));
        this.resetButton(g, this.cursorY - 22, mouseX, mouseY, UiConfigs.General.BLUR);
        this.toggle(g, "malilib-ui.settings.compact", UiConfigs.General.COMPACT.getBooleanValue(),
                () -> UiConfigs.General.COMPACT.setBooleanValue(!UiConfigs.General.COMPACT.getBooleanValue()));
        this.resetButton(g, this.cursorY - 22, mouseX, mouseY, UiConfigs.General.COMPACT);
        this.toggle(g, "malilib-ui.settings.styleOther", UiConfigs.General.STYLE_OTHER.getBooleanValue(),
                () -> UiConfigs.General.STYLE_OTHER.setBooleanValue(!UiConfigs.General.STYLE_OTHER.getBooleanValue()));
        this.resetButton(g, this.cursorY - 22, mouseX, mouseY, UiConfigs.General.STYLE_OTHER);

        this.section(g, "malilib-ui.settings.sorting");
        this.toggle(g, "malilib-ui.settings.pinStrip", UiConfigs.General.PIN_STRIP.getBooleanValue(),
                () -> UiConfigs.General.PIN_STRIP.setBooleanValue(!UiConfigs.General.PIN_STRIP.getBooleanValue()));
        this.resetButton(g, this.cursorY - 22, mouseX, mouseY, UiConfigs.General.PIN_STRIP);
        this.chip(g, "malilib-ui.settings.modSort",
                Text.of("malilib-ui.settings.modSort." + UiConfigs.General.MOD_SORT.getOptionListValue().getStringValue()),
                mouseX, mouseY, () -> this.cycle(UiConfigs.General.MOD_SORT));
        this.resetButton(g, this.cursorY - 22, mouseX, mouseY, UiConfigs.General.MOD_SORT);
        this.chip(g, "malilib-ui.settings.pinMode",
                Text.of("malilib-ui.pinMode." + UiConfigs.General.PIN_MODE.getIntegerValue()), mouseX, mouseY,
                () -> UiConfigs.General.PIN_MODE.setIntegerValue((UiConfigs.General.PIN_MODE.getIntegerValue() + 1) % 3));
        this.resetButton(g, this.cursorY - 22, mouseX, mouseY, UiConfigs.General.PIN_MODE);
        this.chip(g, "malilib-ui.settings.tabSort",
                Text.of("malilib-ui.settings.tabSort." + UiConfigs.General.TAB_SORT.getOptionListValue().getStringValue()),
                mouseX, mouseY, () -> this.cycle(UiConfigs.General.TAB_SORT));
        this.resetButton(g, this.cursorY - 22, mouseX, mouseY, UiConfigs.General.TAB_SORT);

        this.contentH = this.cursorY + (int) this.scroll - this.viewTop;
        g.disableScissor();
        this.drawScrollbar(g, mouseX, mouseY);

        int btnY = this.panelY + this.panelH - 30;
        int doneW = 84;
        this.button(g, this.panelX + this.panelW - 16 - doneW, btnY, doneW, Text.of("malilib-ui.settings.done"), mouseX, mouseY,
                this::onClose);
        this.button(g, this.panelX + this.panelW - 24 - doneW * 2, btnY, doneW, Text.of("malilib-ui.settings.resetAll"),
                mouseX, mouseY, this::resetAll);
        this.button(g, this.panelX + this.panelW - 32 - doneW * 3, btnY, doneW, Text.of("malilib-ui.settings.resetOrder"),
                mouseX, mouseY, () -> UiStore.get().resetOrder());
    }

    private void clampScroll() {
        float max = Math.max(0, this.contentH - this.viewH);
        this.scroll = Math.max(0, Math.min(this.scroll, max));
    }

    private void drawScrollbar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int max = Math.max(0, this.contentH - this.viewH);
        if (max <= 0) {
            return;
        }
        int x = this.panelX + this.panelW - 7;
        Draw.roundRect(g, x, this.viewTop, 4, this.viewH, 2, Theme.alpha(0xFFFFFFFF, 0.07f));
        float ratio = (float) this.viewH / (this.viewH + max);
        int barH = Math.max(20, (int) (this.viewH * ratio));
        int barY = this.viewTop + (int) ((this.viewH - barH) * (this.scroll / max));
        boolean hovered = this.dragging || this.hover(mouseX, mouseY, x - 4, this.viewTop, 12, this.viewH);
        Draw.roundRect(g, x, barY, 4, barH, 2, hovered ? Theme.accent() : Theme.alpha(0xFFFFFFFF, 0.22f));
    }

    private boolean inView(double mouseY) {
        return mouseY >= this.viewTop && mouseY < this.viewTop + this.viewH;
    }

    private void section(GuiGraphicsExtractor g, String key) {
        Draw.text(g, Text.of(key), this.left(), this.cursorY, Theme.accentSoft(0xB0));
        Draw.rect(g, this.left(), this.cursorY + 12, this.contentW(), 1, Theme.outline());
        this.cursorY += 21;
    }

    private void swatches(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int size = 18;
        int gap = Math.max(4, (this.contentW() - size * ACCENTS.length) / (ACCENTS.length - 1));
        for (int i = 0; i < ACCENTS.length; i++) {
            int x = this.left() + i * (size + gap);
            int y = this.cursorY;
            boolean active = (UiConfigs.Colors.ACCENT.getIntegerValue() | 0xFF000000) == ACCENTS[i];
            if (active) {
                Draw.roundRect(g, x - 3, y - 3, size + 6, size + 6, 7, Theme.accentSoft(0x50));
            }
            Draw.roundRect(g, x, y, size, size, 5, ACCENTS[i]);
            int color = ACCENTS[i];
            this.hits.add(new Hit(x - 2, y - 2, size + 4, size + 4, () -> {
                UiConfigs.Colors.ACCENT.setIntegerValue(color);
                UiConfigs.saveToFile();
            }));
        }
        this.cursorY += size + 10;
    }

    private void slider(GuiGraphicsExtractor g, String key, int value, int min, int max, IntConsumer setter) {
        int y = this.cursorY;
        int x = this.left() + LABEL_W;
        int w = this.contentW() - LABEL_W - 42;
        Draw.text(g, Text.of(key), this.left(), y + 6, Theme.textDim());
        float ratio = (float) (value - min) / (max - min);
        int trackY = y + 11;
        Draw.roundRect(g, x, trackY, w, 4, 2, Theme.surfaceHigh());
        Draw.roundRect(g, x, trackY, Math.max(2, (int) (w * ratio)), 4, 2, Theme.accent());
        Draw.dot(g, x + (int) (w * ratio), trackY + 2, 12, 0xFFFFFFFF);
        Draw.text(g, String.valueOf(value), x + w + 8, y + 6, Theme.textOff());
        this.hits.add(new Hit(x - 6, y, w + 12, 20, () -> {
            this.activeSlider = setter;
            this.activeX = x;
            this.activeW = w;
            this.activeMin = min;
            this.activeMax = max;
            this.applySliderTo(x + (int) (w * ratio));
        }));
        this.cursorY += 26;
    }

    private void toggle(GuiGraphicsExtractor g, String key, boolean on, Runnable flip) {
        int y = this.cursorY;
        Draw.text(g, Text.of(key), this.left(), y + 6, Theme.textDim());
        int x = this.left() + LABEL_W;
        Draw.roundRect(g, x, y + 2, 28, 14, 7, on ? Theme.accent() : Theme.surfaceHigh());
        Draw.dot(g, on ? x + 21 : x + 7, y + 9, 10, on ? 0xFFFFFFFF : Theme.textOff());
        this.hits.add(new Hit(x - 6, y, 44, 20, () -> {
            flip.run();
            UiConfigs.saveToFile();
        }));
        this.cursorY += 24;
    }

    private void chip(GuiGraphicsExtractor g, String key, String value, int mouseX, int mouseY, Runnable cycle) {
        int y = this.cursorY;
        Draw.text(g, Text.of(key), this.left(), y + 6, Theme.textDim());
        int x = this.left() + LABEL_W;
        int w = this.contentW() - LABEL_W;
        boolean hovered = this.hover(mouseX, mouseY, x, y, w, 20);
        Draw.roundRect(g, x, y, w, 20, 5, hovered ? Theme.surfaceHigh() : Theme.surface());
        Draw.text(g, Draw.trim(value, w - 16), x + 7, y + 6, Theme.textDim());
        Icons.icon(g, "chevron-down", x + w - 14, y + 5, 10, Theme.textOff());
        this.hits.add(new Hit(x, y, w, 20, () -> {
            cycle.run();
            UiConfigs.saveToFile();
        }));
        this.cursorY += 26;
    }

    private void button(GuiGraphicsExtractor g, int x, int y, int w, String label, int mouseX, int mouseY, Runnable action) {
        boolean hovered = this.hover(mouseX, mouseY, x, y, w, 20);
        Draw.roundRect(g, x, y, w, 20, 5, hovered ? Theme.surfaceHigh() : Theme.surface());
        Draw.textCentered(g, Draw.trim(label, w - 10), x + w / 2, y + 6, Theme.textDim());
        this.fixedHits.add(new Hit(x, y, w, 20, action));
    }

    /** 行尾的重置按钮 */
    private void resetButton(GuiGraphicsExtractor g, int y, int mouseX, int mouseY, fi.dy.masa.malilib.config.IConfigResettable config) {
        int x = this.left() + this.contentW() - 14;
        boolean hovered = this.hover(mouseX, mouseY, x - 3, y, 18, 18);
        Icons.icon(g, "refresh", x, y + 4, 11, hovered ? Theme.accent() : Theme.textOff());
        this.hits.add(new Hit(x - 3, y, 18, 20, () -> {
            config.resetToDefault();
            UiConfigs.saveToFile();
        }));
    }

    private void resetAll() {
        for (fi.dy.masa.malilib.config.IConfigBase config : UiConfigs.General.OPTIONS) {
            if (config instanceof fi.dy.masa.malilib.config.IConfigResettable resettable) {
                resettable.resetToDefault();
            }
        }
        for (fi.dy.masa.malilib.config.IConfigBase config : UiConfigs.Colors.OPTIONS) {
            if (config instanceof fi.dy.masa.malilib.config.IConfigResettable resettable) {
                resettable.resetToDefault();
            }
        }
        UiConfigs.saveToFile();
    }

    private boolean hover(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private void cycle(fi.dy.masa.malilib.config.options.ConfigOptionList option) {
        IConfigOptionListEntry next = option.getOptionListValue().cycle(true);
        option.setOptionListValue(next);
    }

    private void applySliderTo(int mouseX) {
        if (this.activeSlider == null) {
            return;
        }
        float ratio = Math.max(0f, Math.min(1f, (float) (mouseX - this.activeX) / this.activeW));
        this.activeSlider.accept(this.activeMin + Math.round(ratio * (this.activeMax - this.activeMin)));
    }

    /* ── 交互 ─────────────────────────────────────────────── */

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        int mx = (int) click.x();
        int my = (int) click.y();
        if (this.hover(mx, my, this.closeX, this.closeY, 16, 16)) {
            this.onClose();
            return true;
        }
        for (Hit hit : this.fixedHits) {
            if (this.hover(mx, my, hit.x(), hit.y(), hit.w(), hit.h())) {
                hit.action().run();
                return true;
            }
        }
        if (my >= this.viewTop && my < this.viewTop + this.viewH) {
            for (Hit hit : this.hits) {
                if (this.hover(mx, my, hit.x(), hit.y(), hit.w(), hit.h())) {
                    hit.action().run();
                    return true;
                }
            }
            this.dragging = true;
            this.dragMoved = false;
            this.lastDragY = my;
            this.pressY = my;
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        this.scroll -= (float) (vertical * 26);
        this.clampScroll();
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dragX, double dragY) {
        if (this.activeSlider != null) {
            this.applySliderTo((int) click.x());
            return true;
        }
        if (this.dragging) {
            double my = click.y();
            if (!this.dragMoved && Math.abs(my - this.pressY) <= 4) {
                return true;
            }
            this.dragMoved = true;
            this.scroll -= (float) (my - this.lastDragY);
            this.lastDragY = my;
            this.clampScroll();
            return true;
        }
        return super.mouseDragged(click, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (this.activeSlider != null) {
            this.activeSlider = null;
            UiConfigs.saveToFile();
            return true;
        }
        if (this.dragging) {
            this.dragging = false;
            this.dragMoved = false;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        return super.keyPressed(input);
    }

    @Override
    public void onClose() {
        UiConfigs.saveToFile();
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
