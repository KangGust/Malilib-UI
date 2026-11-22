package kang.malilib.ui.mixin.accessor;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import fi.dy.masa.malilib.gui.widgets.WidgetListBase;

@Mixin(WidgetListBase.class)
public interface WidgetListBaseAccessor {
    @Accessor("browserEntryHeight")
    void malilibui$setEntryHeight(int height);

    @Accessor("listWidgets")
    List<Object> malilibui$getRowWidgets();
}
