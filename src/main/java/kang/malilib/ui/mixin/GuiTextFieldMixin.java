package kang.malilib.ui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import kang.malilib.ui.gui.Draw;
import kang.malilib.ui.gui.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;

/** 接管文本输入框绘制（圆角底 + 光标）；无可绘制内容时交还原版，避免图标按钮等变成空块 */
@Mixin(EditBox.class)
public class GuiTextFieldMixin {
    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"), cancellable = true, require = 0)
    private void malilibui$render(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        EditBox self = (EditBox) (Object) this;
        if (!(self instanceof GuiTextFieldGeneric) && !(self instanceof kang.malilib.ui.gui.UiEditBox)) {
            return;
        }
        int x = self.getX();
        int y = self.getY();
        int w = self.getWidth();
        int h = self.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        ci.cancel();
        boolean focused = self.isFocused();
        Draw.roundRect(ctx, x, y, w, h, Math.min(Theme.radius(), h / 2),
                focused ? Theme.surfaceHigh() : Theme.surface());
        String value = self.getValue();
        String shown = Draw.trim(value, w - 10);
        int color = focused ? Theme.text() : Theme.textDim();
        if (value.isEmpty()) {
            shown = "";
        }
        Draw.text(ctx, shown, x + 5, y + (h - 8) / 2, color);
        if (focused) {
            Draw.rect(ctx, x + 5 + Draw.width(shown) + 1, y + (h - 9) / 2, 1, 10, Theme.accent());
        }
    }
}
