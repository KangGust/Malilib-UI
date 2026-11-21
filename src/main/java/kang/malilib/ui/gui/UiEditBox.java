package kang.malilib.ui.gui;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** 本模组自用的输入框（用类型标记，避免误伤原版/其它模组的输入框样式） */
public class UiEditBox extends EditBox {
    public UiEditBox(int x, int y, int width, int height, Component message) {
        super(MinecraftHolder.font(), x, y, width, height, message);
    }

    private static final class MinecraftHolder {
        private static net.minecraft.client.gui.Font font() {
            return net.minecraft.client.Minecraft.getInstance().font;
        }
    }
}
