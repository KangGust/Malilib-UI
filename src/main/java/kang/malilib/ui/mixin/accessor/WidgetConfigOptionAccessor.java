package kang.malilib.ui.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;

@Mixin(WidgetConfigOption.class)
public interface WidgetConfigOptionAccessor {
    @Accessor("wrapper")
    GuiConfigsBase.ConfigOptionWrapper malilibui$getWrapper();
}
