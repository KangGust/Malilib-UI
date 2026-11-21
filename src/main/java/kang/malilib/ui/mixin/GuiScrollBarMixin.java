package kang.malilib.ui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.GuiScrollBar;
import fi.dy.masa.malilib.render.GuiContext;
import kang.malilib.ui.gui.Draw;
import kang.malilib.ui.gui.Theme;

/** 接管滚动条：细圆角滑块，拖动逻辑仍用原实现 */
@Mixin(GuiScrollBar.class)
public abstract class GuiScrollBarMixin {
    @Accessor("mouseOver")
    protected abstract void malilibui$setMouseOver(boolean value);

    @Accessor("dragging")
    protected abstract boolean malilibui$isDragging();

    @Invoker("handleDrag")
    protected abstract void malilibui$handleDrag(int mouseY, int barTravel);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void malilibui$render(GuiContext ctx, int mouseX, int mouseY, float partialTicks, int x, int y,
            int width, int height, int totalHeight, CallbackInfo ci) {
        ci.cancel();
        GuiScrollBar self = (GuiScrollBar) (Object) this;
        if (totalHeight <= 0) {
            return;
        }
        int slideHeight = height - 2;
        float relative = Math.min(1f, (float) slideHeight / totalHeight);
        int barHeight = Math.max(16, (int) (relative * slideHeight));
        int barTravel = Math.max(1, slideHeight - barHeight);
        int max = self.getMaxValue();
        int barY = y + 1 + (max > 0 ? (int) ((float) self.getValue() / max * barTravel) : 0);
        boolean hovered = mouseX > x - 2 && mouseX < x + width + 2 && mouseY > barY && mouseY < barY + barHeight;
        this.malilibui$setMouseOver(hovered);
        Draw.roundRect(ctx, x + 1, barY, Math.max(2, width - 2), barHeight, Math.max(1, Math.min(3, width / 2 - 1)),
                hovered || this.malilibui$isDragging() ? Theme.accent() : Theme.alpha(0xFFFFFFFF, 0.2f));
        this.malilibui$handleDrag(mouseY, barTravel);
    }
}
