package kang.malilib.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import kang.malilib.ui.config.UiConfigs;
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
        LOGGER.info("[{}] {}", MOD_NAME, VERSION);
    }
}
