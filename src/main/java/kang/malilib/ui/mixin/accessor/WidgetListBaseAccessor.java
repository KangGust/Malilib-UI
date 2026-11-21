package kang.malilib.ui.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import fi.dy.masa.malilib.gui.widgets.WidgetListBase;

@Mixin(WidgetListBase.class)
public interface WidgetListBaseAccessor {
    @Accessor("browserEntryHeight")
    void malilibui$setEntryHeight(int height);
}
