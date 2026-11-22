package kang.malilib.ui.mixin.accessor;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetContainer;

@Mixin(WidgetContainer.class)
public interface WidgetContainerAccessor {
    @Accessor("subWidgets")
    List<WidgetBase> malilibui$getSubWidgets();
}
