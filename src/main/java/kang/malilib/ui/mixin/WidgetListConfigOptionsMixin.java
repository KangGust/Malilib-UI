package kang.malilib.ui.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import kang.malilib.ui.config.UiConfigs;
import kang.malilib.ui.config.UiStore;
import kang.malilib.ui.gui.RowScope;

/** 收藏置顶：把收藏的配置项排到列表最前（pinMode 0 仅置顶 / 2 同时启用） */
@Mixin(WidgetListConfigOptions.class)
public class WidgetListConfigOptionsMixin {
    @Inject(method = "getAllEntries", at = @At("RETURN"), cancellable = true, require = 0)
    private void malilibui$pinFirst(CallbackInfoReturnable<Collection<GuiConfigsBase.ConfigOptionWrapper>> cir) {
        if (!RowScope.full()) {
            return;
        }
        int mode = UiConfigs.General.PIN_MODE.getIntegerValue();
        if (mode == 1) {
            return;
        }
        Collection<GuiConfigsBase.ConfigOptionWrapper> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) {
            return;
        }
        Map<String, Integer> order = new LinkedHashMap<>();
        for (UiStore.Ref ref : UiStore.get().pins) {
            if (ref.mod.equals(RowScope.currentModId) && !order.containsKey(ref.config)) {
                order.put(ref.config, order.size());
            }
        }
        if (order.isEmpty()) {
            return;
        }
        List<GuiConfigsBase.ConfigOptionWrapper> pinned = new ArrayList<>();
        List<GuiConfigsBase.ConfigOptionWrapper> rest = new ArrayList<>();
        for (GuiConfigsBase.ConfigOptionWrapper wrapper : original) {
            if (wrapper.getConfig() != null && order.containsKey(wrapper.getConfig().getName())) {
                pinned.add(wrapper);
            } else {
                rest.add(wrapper);
            }
        }
        pinned.sort((a, b) -> Integer.compare(
                order.getOrDefault(a.getConfig().getName(), 0),
                order.getOrDefault(b.getConfig().getName(), 0)));
        pinned.addAll(rest);
        cir.setReturnValue(pinned);
    }
}
