package kang.malilib.ui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.render.GuiContext;
import kang.malilib.ui.gui.Draw;
import kang.malilib.ui.gui.Icons;
import kang.malilib.ui.gui.Theme;
import kang.malilib.ui.mixin.accessor.ButtonBaseAccessor;

/** 接管 malilib 按钮渲染：有文字画文字，滑杆/输入框切换这类纯图标按钮换成 Tabler 图标 */
@Mixin(ButtonGeneric.class)
public abstract class ButtonGenericMixin {
    @Accessor("icon")
    protected abstract IGuiIcon malilibui$getIcon();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void malilibui$render(GuiContext ctx, int mouseX, int mouseY, boolean selected, CallbackInfo ci) {
        if (!kang.malilib.ui.gui.RowScope.styleAllowed()) { return; }
        WidgetBase self = (WidgetBase) (Object) this;
        ButtonBaseAccessor access = (ButtonBaseAccessor) this;
        String text = access.malilibui$getText();
        IGuiIcon icon = this.malilibui$getIcon();
        boolean toggle = icon == MaLiLibIcons.BTN_SLIDER || icon == MaLiLibIcons.BTN_TXTFIELD;

        if ((text == null || text.isEmpty()) && !toggle) {
            return;
        }
        ci.cancel();

        int x = self.getX();
        int y = self.getY();
        int w = self.getWidth();
        int h = self.getHeight();
        boolean enabled = access.malilibui$isEnabled();
        boolean hovered = enabled && mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int radius = Math.min(Theme.radius(), h / 2);
        int bg = !enabled ? Theme.surface()
                : (hovered ? Theme.mix(Theme.accent(), Theme.surface(), 0.62f) : Theme.surfaceHigh());
        Draw.roundRect(ctx, x, y, w, h, radius, bg);

        if (toggle) {
            String name = icon == MaLiLibIcons.BTN_SLIDER ? "forms" : "keyboard";
            int color = hovered ? Theme.accent() : Theme.textDim();
            Icons.iconCentered(ctx, name, x, y, w, h, Math.min(12, h - 4), color);
            return;
        }

        int color = !enabled ? Theme.accent() : (hovered ? Theme.text() : Theme.textDim());
        String trimmed = Draw.trim(text, w - 6);
        ctx.drawString(ctx.fontRenderer(), trimmed, x + (w - Draw.width(trimmed)) / 2, y + (h - 8) / 2, color);
    }
}
