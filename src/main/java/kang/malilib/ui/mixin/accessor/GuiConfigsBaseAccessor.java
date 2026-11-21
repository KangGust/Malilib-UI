package kang.malilib.ui.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;

@Mixin(GuiConfigsBase.class)
public interface GuiConfigsBaseAccessor {
    @Invoker("createListWidget")
    WidgetListConfigOptions malilibui$createListWidget(int x, int y);

    @Invoker("getBrowserWidth")
    int malilibui$getBrowserWidth();

    @Invoker("getBrowserHeight")
    int malilibui$getBrowserHeight();

    @Invoker("getConfigWidth")
    int malilibui$getConfigWidth();

    @Accessor("configWidth")
    void malilibui$setConfigWidth(int width);

    @Invoker("onSettingsChanged")
    void malilibui$onSettingsChanged();
}
