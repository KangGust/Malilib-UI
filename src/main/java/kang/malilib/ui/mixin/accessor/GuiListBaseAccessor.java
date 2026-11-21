package kang.malilib.ui.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;

@Mixin(GuiListBase.class)
public interface GuiListBaseAccessor {
    @Accessor("widget")
    WidgetListBase malilibui$getList();

    @Accessor("widget")
    void malilibui$setList(WidgetListBase list);
}
