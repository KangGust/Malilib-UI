package kang.malilib.ui.gui;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import fi.dy.masa.malilib.config.IConfigBase;
import kang.malilib.ui.config.UiStore;
import kang.malilib.ui.core.Actions;
import kang.malilib.ui.core.ConfigIndex;
import kang.malilib.ui.core.ModEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** 选择功能：置顶到左侧常用区，或加入某个功能组 */
public class PickerScreen extends Screen {
    public enum Mode {
        PIN, GROUP
    }

    private static final int ROW_H = 22;

    private final Screen parent;
    private final Mode mode;
    private final UiStore.Group group;
    private final Input search = new Input();
    private final Input nameField = new Input();
    private final Set<String> picked = new LinkedHashSet<>();
    private final List<Row> rows = new ArrayList<>();

    private float scroll;
    private boolean dragging;
    private double lastY;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    private record Row(int kind, ModEntry mod, IConfigBase config, String label) {
    }

    public PickerScreen(Screen parent, Mode mode, ModEntry focusMod) {
        this(parent, mode, null, focusMod);
    }

    public PickerScreen(Screen parent, Mode mode, UiStore.Group group, ModEntry focusMod) {
        super(Component.literal("Malilib UI"));
        this.parent = parent;
        this.mode = mode;
        this.group = group;
        if (group != null) {
            for (UiStore.Item item : group.items) {
                this.picked.add(item.key());
            }
        }
    }

    private boolean newGroup() {
        return this.mode == Mode.GROUP && this.group == null;
    }

    @Override
    protected void init() {
        this.panelW = Math.min(460, this.width - 24);
        this.panelH = Math.min(420, this.height - 24);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
        this.search.focused = true;
        if (this.nameField.text.isEmpty()) {
            this.nameField.text = Text.of("malilib-ui.group.defaultName");
        }
        this.rebuild();
    }

    private int listTop() {
        return this.panelY + (this.newGroup() ? 68 : 46);
    }

    private int listBottom() {
        return this.panelY + this.panelH - 32;
    }

    private void rebuild() {
        this.rows.clear();
        String filter = this.search.text.toLowerCase();
        for (ModEntry mod : ConfigIndex.get().mods()) {
            List<IConfigBase> configs = new ArrayList<>();
            for (IConfigBase config : ConfigIndex.get().configsOf(mod).values()) {
                String label = config.getConfigGuiDisplayName();
                if (filter.isEmpty() || label.toLowerCase().contains(filter)
                        || config.getName().toLowerCase().contains(filter)) {
                    configs.add(config);
                }
            }
            if (configs.isEmpty()) {
                continue;
            }
            this.rows.add(new Row(0, mod, null, mod.name));
            for (IConfigBase config : configs) {
                this.rows.add(new Row(1, mod, config, config.getConfigGuiDisplayName()));
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
        Draw.rect(g, 0, 0, this.width, this.height, Theme.scrim());
        Draw.shadow(g, this.panelX, this.panelY, this.panelW, this.panelH, Theme.radius());
        Draw.roundRect(g, this.panelX, this.panelY, this.panelW, this.panelH, Theme.radius(), Theme.panel());
        Draw.textShadow(g, Text.of(this.mode == Mode.PIN ? "malilib-ui.picker.pin" : "malilib-ui.picker.group"),
                this.panelX + 14, this.panelY + 12, Theme.text());
        this.search.draw(g, this.panelX + 14, this.panelY + 26, this.panelW - 60, 16,
                Text.of("malilib-ui.picker.search"));
        if (this.newGroup()) {
            this.nameField.draw(g, this.panelX + 14, this.panelY + 46, this.panelW - 28, 16,
                    Text.of("malilib-ui.group.name"));
        }

        Draw.scissor(g, this.panelX + 8, this.listTop(), this.panelW - 16, this.listBottom() - this.listTop());
        int y = this.listTop() - (int) this.scroll;
        for (Row row : this.rows) {
            if (y + ROW_H >= this.listTop() - ROW_H && y <= this.listBottom() + ROW_H) {
                this.drawRow(g, row, y, mouseX, mouseY);
            }
            y += ROW_H;
        }
        g.disableScissor();

        String done = Text.of(this.newGroup() ? "malilib-ui.button.create" : "malilib-ui.settings.done");
        int doneW = 76;
        this.popButton(g, this.panelX + this.panelW - doneW - 14, this.panelY + this.panelH - 26, doneW, done, mouseX, mouseY);
    }

    private void drawRow(GuiGraphicsExtractor g, Row row, int y, int mouseX, int mouseY) {
        int x = this.panelX + 14;
        int w = this.panelW - 40;
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + ROW_H - 2;
        if (row.kind() == 0) {
            Draw.text(g, Draw.trim(row.label(), w), x + 2, y + 10, Theme.accent());
            return;
        }
        String key = row.mod().modId + "/" + row.config().getName();
        boolean selected = this.mode == Mode.PIN ? UiStore.get().isPinned(row.mod().modId, row.config().getName())
                : this.picked.contains(key);
        if (hovered) {
            Draw.roundRect(g, x - 4, y, w + 4, ROW_H - 2, 5, Theme.surface());
        }
        String en = row.config().getName();
        boolean dual = !en.equalsIgnoreCase(row.label());
        Draw.text(g, Draw.trim(row.label(), w - 40), x + 18, dual ? y + 3 : y + 7, selected ? Theme.text() : Theme.textDim());
        if (dual) {
            Draw.text(g, Draw.trim(en, w - 40), x + 18, y + 12, Theme.textOff());
        }
        Draw.text(g, Actions.displayValue(row.config()), x + w - 60, y + (dual ? 3 : 7), Theme.textOff());
        Draw.dot(g, x + 6, y + (dual ? 8 : 10), 8, selected ? Theme.accent() : Theme.alpha(0xFFFFFFFF, 0.14f));
    }

    private void popButton(GuiGraphicsExtractor g, int x, int y, int w, String label, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 18;
        Draw.roundRect(g, x, y, w, 18, 5, hovered ? Theme.surfaceHigh() : Theme.surface());
        Draw.textCentered(g, label, x + w / 2, y + 5, Theme.textDim());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        int mx = (int) click.x();
        int my = (int) click.y();
        if (my >= this.listTop() && my <= this.listBottom() && mx >= this.panelX && mx < this.panelX + this.panelW) {
            int index = (int) ((my - this.listTop() + this.scroll) / ROW_H);
            if (index >= 0 && index < this.rows.size()) {
                Row row = this.rows.get(index);
                if (row.kind() == 1) {
                    this.toggle(row);
                }
                return true;
            }
            this.dragging = true;
            this.lastY = my;
            return true;
        }
        this.search.focused = mx >= this.panelX + 14 && my >= this.panelY + 26 && my < this.panelY + 42;
        if (this.newGroup()) {
            this.nameField.focused = my >= this.panelY + 46 && my < this.panelY + 64;
        }
        int doneW = 76;
        if (mx >= this.panelX + this.panelW - doneW - 14 && my >= this.panelY + this.panelH - 26
                && my < this.panelY + this.panelH - 8) {
            this.confirm();
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    private void toggle(Row row) {
        String name = row.config().getName();
        if (this.mode == Mode.PIN) {
            UiStore.get().togglePin(row.mod().modId, name);
            return;
        }
        String key = row.mod().modId + "/" + name;
        if (!this.picked.remove(key)) {
            this.picked.add(key);
        }
        if (!this.newGroup() && this.group != null) {
            this.group.items.removeIf(item -> item.key().equals(key));
            if (this.picked.contains(key)) {
                this.group.items.add(new UiStore.Item(row.mod().modId, name, Actions.value(row.config())));
            }
            UiStore.get().save();
        }
    }

    private void confirm() {
        if (this.newGroup()) {
            UiStore.Group created = new UiStore.Group(this.nameField.text.trim().isEmpty()
                    ? Text.of("malilib-ui.group.defaultName") : this.nameField.text.trim());
            for (String key : this.picked) {
                int split = key.indexOf('/');
                String modId = key.substring(0, split);
                String configName = key.substring(split + 1);
                IConfigBase config = ConfigIndex.get().find(modId, configName);
                created.items.add(new UiStore.Item(modId, configName, config == null ? "" : Actions.value(config)));
            }
            UiStore.get().addGroup(created);
        }
        this.onClose();
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dragX, double dragY) {
        if (this.dragging) {
            this.scroll -= (float) (click.y() - this.lastY);
            this.lastY = click.y();
            this.clamp();
            return true;
        }
        return super.mouseDragged(click, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        this.dragging = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        this.scroll -= (float) (vertical * ROW_H);
        this.clamp();
        return true;
    }

    private void clamp() {
        float max = Math.max(0, this.rows.size() * ROW_H - (this.listBottom() - this.listTop()));
        this.scroll = Math.max(0, Math.min(this.scroll, max));
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (this.search.keyPressed(input) || this.nameField.keyPressed(input)) {
            this.rebuild();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (this.search.charTyped(input) || this.nameField.charTyped(input)) {
            this.rebuild();
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
