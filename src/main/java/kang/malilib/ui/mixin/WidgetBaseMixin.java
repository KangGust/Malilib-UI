package kang.malilib.ui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import fi.dy.masa.malilib.render.GuiContext;
import kang.malilib.ui.gui.Draw;
import kang.malilib.ui.gui.Theme;

/** 配置行：中文显示名下补一行英文原名 */
@Mixin(WidgetBase.class)
public class WidgetBaseMixin {
    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void malilibui$renderEnglishName(GuiContext ctx, int mouseX, int mouseY, boolean selected, CallbackInfo ci) {
        if (!((Object) this instanceof WidgetConfigOption option)) {
            return;
        }
        Object entry = ((WidgetListEntryBase) option).getEntry();
        if (!(entry instanceof GuiConfigsBase.ConfigOptionWrapper wrapper)) {
            return;
        }
        IConfigBase config = wrapper.getConfig();
        if (config == null) {
            return;
        }
        String english = config.getName();
        String display = config.getConfigGuiDisplayName();
        if (english.equalsIgnoreCase(display) || english.isEmpty()) {
            return;
        }
        WidgetBase self = (WidgetBase) (Object) this;
        String text = Draw.trim(english, (int) (Math.max(60, self.getWidth() * 0.42f) / 0.68f));
        var pose = ctx.pose();
        pose.pushMatrix();
        pose.translate(self.getX(), self.getY() + 14.5f);
        pose.scale(0.68f, 0.68f);
        ctx.drawString(ctx.fontRenderer(), text, 0, 0, Theme.textOff());
        pose.popMatrix();
    }
}
