package kang.malilib.ui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import kang.malilib.ui.core.ConfigIndex;

/** malilib 的「滑杆 / 输入框」切换按钮只认它自己的界面，这里补一刀：切换后重建我们的列表 */
@Mixin(WidgetConfigOption.ListenerSliderToggle.class)
public class SliderToggleMixin {
    @Inject(method = "actionPerformedWithButton", at = @At("TAIL"), require = 0)
    private void malilibui$afterToggle(ButtonBase button, int mouseButton, CallbackInfo ci) {
        ConfigIndex.requestRebuild();
    }
}
