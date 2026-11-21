package kang.malilib.ui.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

/** 极简文本输入：只做单行、退格、回车确认，够搜索和命名用 */
public final class Input {
    public String text = "";
    public boolean focused;

    public boolean keyPressed(KeyEvent event) {
        if (!this.focused) {
            return false;
        }
        if (event.key() == 259) {
            if (!this.text.isEmpty()) {
                this.text = this.text.substring(0, this.text.length() - 1);
            }
            return true;
        }
        if (event.key() == 256) {
            this.focused = false;
            return true;
        }
        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        if (!this.focused) {
            return false;
        }
        char c = (char) event.codepoint();
        if (c >= 32 && this.text.length() < 64) {
            this.text += c;
            return true;
        }
        return false;
    }

    public void draw(GuiGraphicsExtractor g, int x, int y, int w, int h, String placeholder) {
        Draw.roundFrame(g, x, y, w, h, Math.min(6, h / 2), Theme.outline(),
                this.focused ? Theme.surfaceHigh() : Theme.surface());
        String shown = this.text.isEmpty() ? placeholder : this.text;
        int color = this.text.isEmpty() ? Theme.textOff() : Theme.text();
        Draw.text(g, Draw.trim(shown + (this.focused ? "_" : ""), w - 12), x + 6, y + (h - 8) / 2, color);
    }
}
