package kang.malilib.ui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.widgets.WidgetLabel;
import fi.dy.masa.malilib.render.GuiContext;
import kang.malilib.ui.gui.RowScope;

/** 配置行内的原版标签由我们自己画，这里把原标签隐藏（仅在我们界面生效） */
@Mixin(WidgetLabel.class)
public class WidgetLabelMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void malilibui$hideLabel(GuiContext ctx, int mouseX, int mouseY, boolean selected, CallbackInfo ci) {
        if (RowScope.inRow()) {
            ci.cancel();
        }
    }
}
