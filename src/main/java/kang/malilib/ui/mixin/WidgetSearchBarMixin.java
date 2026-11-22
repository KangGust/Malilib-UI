package kang.malilib.ui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.widgets.WidgetSearchBar;
import fi.dy.masa.malilib.render.GuiContext;

/** 原列表自带的搜索框只保留过滤能力，不再绘制（改由顶部搜索栏驱动） */
@Mixin(WidgetSearchBar.class)
public class WidgetSearchBarMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void malilibui$render(GuiContext ctx, int mouseX, int mouseY, boolean selected, CallbackInfo ci) {
        if (!kang.malilib.ui.gui.RowScope.full()) { return; }
        ci.cancel();
    }
}
