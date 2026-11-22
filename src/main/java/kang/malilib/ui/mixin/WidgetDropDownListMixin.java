package kang.malilib.ui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.widgets.WidgetDropDownList;
import fi.dy.masa.malilib.render.GuiContext;
import kang.malilib.ui.gui.Draw;
import kang.malilib.ui.gui.Icons;
import kang.malilib.ui.gui.Theme;

/** 接管下拉框（收起状态）：圆角底 + 文字 + 箭头 */
@Mixin(WidgetDropDownList.class)
public abstract class WidgetDropDownListMixin {
    @Accessor("isOpen")
    protected abstract boolean malilibui$isOpen();

    @Invoker("getDisplayString")
    protected abstract String malilibui$displayString(Object entry);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void malilibui$render(GuiContext ctx, int mouseX, int mouseY, boolean selected, CallbackInfo ci) {
        if (!kang.malilib.ui.gui.RowScope.styleAllowed() || this.malilibui$isOpen()) {
            return;
        }
        ci.cancel();
        WidgetDropDownList<?> self = (WidgetDropDownList<?>) (Object) this;
        int x = self.getX();
        int y = self.getY();
        int w = self.getWidth();
        int h = self.getHeight();
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        Draw.roundRect(ctx, x, y, w, h, Math.min(Theme.radius(), h / 2),
                hovered ? Theme.surfaceHigh() : Theme.surface());
        Object entry = self.getSelectedEntry();
        String text = entry == null ? "" : this.malilibui$displayString(entry);
        ctx.drawString(ctx.fontRenderer(), Draw.trim(text, w - 18), x + 6, y + (h - 8) / 2, Theme.textDim());
        Icons.icon(ctx, "chevron-down", x + w - 13, y + (h - 10) / 2, 10, Theme.textOff());
    }
}
