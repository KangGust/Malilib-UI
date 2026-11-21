package kang.malilib.ui.compat;

import java.util.HashMap;
import java.util.Map;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import kang.malilib.ui.MalilibUI;
import kang.malilib.ui.gui.ModsScreen;

/**
 * Mod Menu 集成：
 * 1. 本模组自己的「配置」按钮直接进主界面；
 * 2. 为每个带 malilib 配置界面的模组「提供」配置界面，于是它们的 Mod Menu 条目也有齿轮按钮。
 */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ModsScreen(parent, MalilibUI.MOD_ID);
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();
        for (ModInfo info : Registry.CONFIG_SCREEN.getAllModsWithConfigScreens()) {
            String modId = info.modId();
            if (!modId.equals(MalilibUI.MOD_ID)) {
                factories.put(modId, parent -> new ModsScreen(parent, modId));
            }
        }
        factories.putIfAbsent("malilib", parent -> new ModsScreen(parent, "malilib"));
        return factories;
    }
}
