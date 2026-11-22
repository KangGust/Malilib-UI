package kang.malilib.ui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.render.GuiContext;
import kang.malilib.ui.gui.Draw;
import kang.malilib.ui.gui.RowScope;
import kang.malilib.ui.gui.Theme;
import kang.malilib.ui.mixin.accessor.WidgetConfigOptionAccessor;

/**
 * 配置行渲染接管：只替换“画什么”，不碰“摆在哪”。
 * 控件位置完全沿用 malilib 自己的布局（labelWidth + 10 + configWidth …），
 * 避免二次偏移导致错位/重叠。
 */
@Mixin(WidgetConfigOption.class)
public class WidgetConfigOptionRenderMixin {
    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void malilibui$rowBefore(GuiContext ctx, int mouseX, int mouseY, boolean selected, CallbackInfo ci) {
        if (!RowScope.full()) {
            return;
        }
        WidgetConfigOption self = (WidgetConfigOption) (Object) this;
        GuiConfigsBase.ConfigOptionWrapper wrapper = ((WidgetConfigOptionAccessor) this).malilibui$getWrapper();
        int x = self.getX();
        int y = self.getY();
        int w = self.getWidth();
        int h = self.getHeight();
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;

        if (hovered) {
            Draw.roundRect(ctx, x - 1, y, w + 2, h - 2, 4, Theme.surface());
        }

        // 分组标题（LABEL 行）：画成胶囊小标题，并隐藏原标签
        if (wrapper.getType() == GuiConfigsBase.ConfigOptionWrapper.Type.LABEL) {
            RowScope.setInRow(true);
            String text = wrapper.getLabel();
            if (text != null && !text.isEmpty()) {
                int textW = Draw.width(text);
                Draw.roundRect(ctx, x - 1, y + 2, Math.min(w, textW + 16), 14, 7, Theme.surfaceHigh());
                Draw.roundRect(ctx, x - 1, y + 2, 3, 14, 1, Theme.accent());
                Draw.text(ctx, Draw.trim(text, w - 20), x + 7, y + 5, Theme.text());
            }
            return;
        }
        if (wrapper.getType() != GuiConfigsBase.ConfigOptionWrapper.Type.CONFIG) {
            return;
        }
        IConfigBase config = wrapper.getConfig();
        if (config == null) {
            return;
        }

        RowScope.setInRow(true);

        // 标签列宽度：与 malilib 布局使用同一个基准（maxLabelWidth 已由列表传入，
        // 这里用行宽的一半作为显示上限，仅影响文字截断，不影响控件位置）
        int labelLimit = Math.max(60, (int) (w * 0.5f));
        String zh = config.getConfigGuiDisplayName();
        String en = config.getName();
        Draw.text(ctx, Draw.trim(zh, labelLimit), x, y + 4, hovered ? Theme.text() : Theme.textDim());
        if (!en.equalsIgnoreCase(zh)) {
            String shown = Draw.trim(en, (int) (labelLimit / 0.7f));
            var pose = ctx.pose();
            pose.pushMatrix();
            pose.translate(x, y + 14f);
            pose.scale(0.7f, 0.7f);
            ctx.drawString(ctx.fontRenderer(), shown, 0, 0, hovered ? Theme.textDim() : Theme.textOff());
            pose.popMatrix();
        }
        // 收藏星标：紧跟功能名右侧，不侵入控件区
        boolean pinned = kang.malilib.ui.config.UiStore.get().isPinned(RowScope.currentModId, config.getName());
        int starSize = 12;
        int nameW = Math.min(labelLimit, Draw.width(zh));
        int starX = x + nameW + 6;
        int starY = y + 3;
        if (starX + starSize > x + labelLimit) {
            starX = x + labelLimit - starSize;
        }
        Draw.roundRect(ctx, starX - 2, starY - 1, starSize + 4, starSize + 2, 4,
                pinned ? Theme.accentSoft(0x2A) : (hovered ? Theme.surfaceHigh() : 0));
        kang.malilib.ui.gui.Icons.icon(ctx, pinned ? "star-filled" : "star", starX, starY, starSize,
                pinned ? Theme.accent() : Theme.textOff());
        kang.malilib.ui.gui.PinHits.add(starX - 2, starY - 1, starSize + 4, starSize + 2, config.getName());

        if (config instanceof IConfigResettable resettable && resettable.isModified()) {
            Draw.dot(ctx, x + Math.min(labelLimit, Draw.width(zh)) + 5, y + 8, 4, Theme.accent());
        }
    }
}
