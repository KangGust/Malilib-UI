package kang.malilib.ui.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import kang.malilib.ui.config.UiStore;
import kang.malilib.ui.core.Actions;
import kang.malilib.ui.core.ConfigIndex;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** 功能组编辑：改名、删成员、加成员、应用/还原、删除整组 */
public class GroupEditorScreen extends Screen {
    private static final int ROW_H = 20;

    private final Screen parent;
    private final UiStore.Group group;
    private final Input nameField = new Input();
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    public GroupEditorScreen(Screen parent, UiStore.Group group) {
        super(Component.literal("Malilib UI"));
        this.parent = parent;
        this.group = group;
        this.nameField.text = group.name;
    }

    @Override
    protected void init() {
        this.panelW = Math.min(420, this.width - 24);
        this.panelH = Math.min(420, this.height - 24);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
    }

    private int listTop() {
        return this.panelY + 62;
    }

    private int listBottom() {
        return this.panelY + this.panelH - 34;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
        Draw.rect(g, 0, 0, this.width, this.height, Theme.scrim());
        Draw.shadow(g, this.panelX, this.panelY, this.panelW, this.panelH, Theme.radius());
        Draw.roundRect(g, this.panelX, this.panelY, this.panelW, this.panelH, Theme.radius(), Theme.panel());
        Draw.textShadow(g, Text.of("malilib-ui.group.title"), this.panelX + 14, this.panelY + 12, Theme.text());
        this.nameField.draw(g, this.panelX + 14, this.panelY + 30, this.panelW - 28, 16,
                Text.of("malilib-ui.group.name"));
        Draw.text(g, Text.of("malilib-ui.group.count", this.group.items.size()), this.panelX + 14,
                this.listTop() - 14, Theme.textOff());

        Draw.scissor(g, this.panelX + 8, this.listTop(), this.panelW - 16, this.listBottom() - this.listTop());
        int y = this.listTop();
        for (int i = 0; i < this.group.items.size(); i++) {
            UiStore.Item item = this.group.items.get(i);
            IConfigBase config = ConfigIndex.get().find(item.mod, item.config);
            String label = config == null ? item.config : config.getConfigGuiDisplayName();
            String value = item.value.isEmpty() ? "" : item.value;
            boolean hovered = mouseX >= this.panelX + 14 && mouseX < this.panelX + this.panelW - 20
                    && mouseY >= y && mouseY < y + ROW_H - 2;
            if (hovered) {
                Draw.roundRect(g, this.panelX + 10, y, this.panelW - 28, ROW_H - 2, 5, Theme.surface());
            }
            Draw.text(g, Draw.trim(label, this.panelW - 140), this.panelX + 18, y + 6, Theme.textDim());
            Draw.text(g, Draw.trim(value, 50), this.panelX + this.panelW - 96, y + 6, Theme.accent());
            Draw.text(g, "×", this.panelX + this.panelW - 34, y + 6, Theme.textOff());
            y += ROW_H;
        }
        g.disableScissor();

        int by = this.panelY + this.panelH - 28;
        this.chip(g, this.panelX + 14, by, 88, Text.of("malilib-ui.group.add"), mouseX, mouseY);
        this.chip(g, this.panelX + 108, by, 88, Text.of("malilib-ui.button.on"), mouseX, mouseY);
        this.chip(g, this.panelX + 202, by, 88, Text.of("malilib-ui.button.off"), mouseX, mouseY);
        this.chip(g, this.panelX + this.panelW - 80, by, 68, Text.of("malilib-ui.group.delete"), mouseX, mouseY);
    }

    private void chip(GuiGraphicsExtractor g, int x, int y, int w, String label, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 18;
        Draw.roundRect(g, x, y, w, 18, 5, hovered ? Theme.surfaceHigh() : Theme.surface());
        Draw.textCentered(g, Draw.trim(label, w - 8), x + w / 2, y + 5, Theme.textDim());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        int mx = (int) click.x();
        int my = (int) click.y();
        this.nameField.focused = my >= this.panelY + 30 && my < this.panelY + 46;

        if (my >= this.listTop() && my < this.listBottom()) {
            int index = (my - this.listTop()) / ROW_H;
            if (index >= 0 && index < this.group.items.size()
                    && mx >= this.panelX + this.panelW - 44) {
                this.group.items.remove(index);
                UiStore.get().save();
                return true;
            }
        }
        int by = this.panelY + this.panelH - 28;
        if (my >= by && my < by + 18) {
            if (mx >= this.panelX + 14 && mx < this.panelX + 102) {
                this.minecraft.gui.setScreen(new PickerScreen(this, PickerScreen.Mode.GROUP, this.group, null));
                return true;
            }
            if (mx >= this.panelX + 108 && mx < this.panelX + 196) {
                Actions.enableGroup(this.group);
                return true;
            }
            if (mx >= this.panelX + 202 && mx < this.panelX + 290) {
                Actions.disableGroup(this.group);
                return true;
            }
            if (mx >= this.panelX + this.panelW - 80 && mx < this.panelX + this.panelW - 12) {
                UiStore.get().removeGroup(this.group);
                this.onClose();
                return true;
            }
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent input) {
        if (this.nameField.keyPressed(input)) {
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent input) {
        if (this.nameField.charTyped(input)) {
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public void onClose() {
        String trimmed = this.nameField.text.trim();
        if (!trimmed.isEmpty()) {
            this.group.name = trimmed;
        }
        UiStore.get().save();
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
