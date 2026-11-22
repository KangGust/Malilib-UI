package kang.malilib.ui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.widgets.WidgetSearchBarConfigs;
import fi.dy.masa.malilib.render.GuiContext;
import kang.malilib.ui.gui.RowScope;

/** 隐藏按键过滤搜索控件（它自己重写了 render，父类 mixin 拦不到） */
@Mixin(WidgetSearchBarConfigs.class)
public class WidgetSearchBarConfigsMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void malilibui$hide(GuiContext ctx, int mouseX, int mouseY, boolean selected, CallbackInfo ci) {
        if (RowScope.full()) {
            ci.cancel();
        }
    }
}
