package kang.malilib.ui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;

/**
 * 拉开功能名与右侧控件之间的距离。
 * 不写死档位：按「标签列之外剩余空间的占比」动态给间距，
 * 所以面板越宽间距越大，窄面板自动收紧，且永远不超过剩余空间的一半。
 */
@Mixin(WidgetConfigOption.class)
public class WidgetConfigOptionMixin {
    private static final float GAP_RATIO = 0.055f;

    @ModifyVariable(method = "addConfigOption", at = @At("HEAD"), argsOnly = true, index = 3, require = 0)
    private int malilibui$padLabelColumn(int labelWidth) {
        WidgetConfigOption self = (WidgetConfigOption) (Object) this;
        int rowWidth = self.getWidth();
        int leftover = Math.max(0, rowWidth - labelWidth);
        int gap = Math.round(rowWidth * GAP_RATIO);
        gap = Math.max(6, Math.min(gap, leftover / 2));
        return labelWidth + gap;
    }
}
