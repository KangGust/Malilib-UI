package kang.malilib.ui.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.render.GuiContext;
import kang.malilib.ui.gui.Draw;
import kang.malilib.ui.gui.RowScope;
import kang.malilib.ui.gui.Theme;
import kang.malilib.ui.mixin.accessor.WidgetListBaseAccessor;

/** 列表容器装饰：行分隔线（只读 malilib 的几何数据，只画不改） */
@Mixin(WidgetListBase.class)
public class WidgetListBaseMixin {
    @Inject(method = "drawContents", at = @At("TAIL"), require = 0)
    private void malilibui$rowSeparators(GuiContext ctx, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!RowScope.full()) {
            return;
        }
        List<Object> rows = ((WidgetListBaseAccessor) this).malilibui$getRowWidgets();
        if (rows == null) {
            return;
        }
        for (Object entry : rows) {
            if (!(entry instanceof WidgetBase widget)) {
                continue;
            }
            int lineY = widget.getY() + widget.getHeight() - 2;
            int lineW = Math.max(0, widget.getWidth() - 6);
            if (lineW > 0) {
                Draw.rect(ctx, widget.getX(), lineY, lineW, 1, Theme.alpha(0xFFFFFFFF, 0.05f));
            }
        }
    }
}
