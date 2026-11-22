package kang.malilib.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import kang.malilib.ui.config.UiConfigs;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import kang.malilib.ui.core.ConfigIndex;
import kang.malilib.ui.gui.ModsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import kang.malilib.ui.gui.UiConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class MalilibUI implements ClientModInitializer {
    public static final String MOD_ID = "malilib-ui";
    public static final String MOD_NAME = "Malilib UI";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final String VERSION = FabricLoader.getInstance().getModContainer(MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("");

    @Override
    public void onInitializeClient() {
        ConfigManager.getInstance().registerConfigHandler(MOD_ID, new UiConfigs());
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                new ModInfo(MOD_ID, MOD_NAME, UiConfigScreen::new));

        // 切换「启用 Malilib 重绘」后立即重建列表，无需退出重进
        UiConfigs.General.ENABLED.setValueChangeCallback(config -> {
            if (config.getBooleanValue()) {
                // 开启：如果当前正停在原生配置界面，直接换成我们的界面；否则只重建列表
                Screen current = Minecraft.getInstance().gui.screen();
                if (current instanceof GuiConfigsBase configs) {
                    Minecraft.getInstance().gui.setScreen(new ModsScreen(configs.getParent(), configs.getModId()));
                } else {
                    ConfigIndex.requestRebuild();
                }
                return;
            }
            Screen current = Minecraft.getInstance().gui.screen();
            if (current instanceof ModsScreen mods) {
                mods.revertToVanilla();
            }
        });
        LOGGER.info("[{}] {}", MOD_NAME, VERSION);
    }
}
