package kang.malilib.ui.mixin.accessor;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.render.GuiContext;

@Mixin(GuiBase.class)
public interface GuiBaseAccessor {
    @Accessor("buttons")
    List<ButtonBase> malilibui$getButtons();

    @Accessor("widgets")
    List<WidgetBase> malilibui$getWidgets();

    @Invoker("drawHoveredWidget")
    void malilibui$drawHoveredWidget(GuiContext ctx, int mouseX, int mouseY);

    @Invoker("drawButtonHoverTexts")
    void malilibui$drawButtonHoverTexts(GuiContext ctx, int mouseX, int mouseY, float partialTicks);
}
