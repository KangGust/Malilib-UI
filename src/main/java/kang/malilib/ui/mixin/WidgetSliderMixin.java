package kang.malilib.ui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.interfaces.ISliderCallback;
import fi.dy.masa.malilib.gui.widgets.WidgetSlider;
import fi.dy.masa.malilib.render.GuiContext;
import kang.malilib.ui.gui.Draw;
import kang.malilib.ui.gui.Theme;

/** 接管滑杆：圆角轨道 + 圆形滑块；保留原实现的“拖动中持续更新数值” */
@Mixin(WidgetSlider.class)
public abstract class WidgetSliderMixin {
    @Accessor("callback")
    protected abstract ISliderCallback malilibui$getCallback();

    @Accessor("dragging")
    protected abstract boolean malilibui$isDragging();

    @Accessor("lastMouseX")
    protected abstract int malilibui$getLastMouseX();

    @Accessor("lastMouseX")
    protected abstract void malilibui$setLastMouseX(int value);

    @Invoker("getRelativePosition")
    protected abstract double malilibui$relativePosition(int mouseX);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void malilibui$render(GuiContext ctx, int mouseX, int mouseY, boolean selected, CallbackInfo ci) {
        ci.cancel();
        if (!kang.malilib.ui.gui.RowScope.styleAllowed()) { return; }
        WidgetSlider self = (WidgetSlider) (Object) this;

        if (this.malilibui$isDragging() && mouseX != this.malilibui$getLastMouseX()) {
            this.malilibui$getCallback().setValueRelative(this.malilibui$relativePosition(mouseX));
            this.malilibui$setLastMouseX(mouseX);
        }

        int x = self.getX();
        int y = self.getY();
        int w = self.getWidth();
        int h = self.getHeight();
        String value = this.malilibui$getCallback().getFormattedDisplayValue();
        int textW = Math.max(24, Draw.width(value) + 6);
        int trackW = Math.max(12, w - textW - 6);
        int trackY = y + h / 2 - 2;

        Draw.roundRect(ctx, x, trackY, trackW, 4, 2, Theme.surfaceHigh());
        int knob = Math.max(8, Math.min(h, 12));
        int knobX = x + (int) (this.malilibui$getCallback().getValueRelative() * (trackW - knob));
        Draw.roundRect(ctx, x, trackY, Math.max(4, knobX + knob / 2 - x), 4, 2, Theme.accent());
        Draw.dot(ctx, knobX + knob / 2, y + h / 2, knob, this.malilibui$isDragging() ? 0xFFFFFFFF : 0xFFEDEFF4);
        ctx.drawString(ctx.fontRenderer(), value, x + w - Draw.width(value), y + (h - 8) / 2, Theme.text());
    }
}
