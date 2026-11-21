package kang.malilib.ui.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;
import kang.malilib.ui.gui.Tooltip;
import net.minecraft.network.chat.Component;

/** 接管 malilib 的悬停提示框：原版黑框换成圆角卡片 */
@Mixin(RenderUtils.class)
public class RenderUtilsMixin {
    @Inject(method = "drawHoverText(Lfi/dy/masa/malilib/render/GuiContext;IILjava/util/List;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void malilibui$hoverTextList(GuiContext ctx, int x, int y, List<String> lines, CallbackInfo ci) {
        ci.cancel();
        Tooltip.draw(ctx, x, y, lines);
    }

    @Inject(method = "drawHoverText(Lfi/dy/masa/malilib/render/GuiContext;IILnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void malilibui$hoverTextComponent(GuiContext ctx, int x, int y, Component text, CallbackInfo ci) {
        ci.cancel();
        Tooltip.draw(ctx, x, y, List.of(text.getString()));
    }
}
