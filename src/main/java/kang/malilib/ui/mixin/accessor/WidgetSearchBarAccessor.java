package kang.malilib.ui.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetSearchBar;

@Mixin(WidgetSearchBar.class)
public interface WidgetSearchBarAccessor {
    @Accessor("searchBox")
    GuiTextFieldGeneric malilibui$getSearchBox();
}
