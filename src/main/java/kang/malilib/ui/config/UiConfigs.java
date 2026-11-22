package kang.malilib.ui.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.malilib.util.data.json.JsonUtils;
import kang.malilib.ui.MalilibUI;

public class UiConfigs implements IConfigHandler {
    private static final String PREFIX = "malilib-ui.config";
    private static final String FILE_NAME = "settings.json";

    /** 本模组的配置目录 config/malilib-ui/ */
    public static Path dir() {
        Path dir = FileUtils.getConfigDirectory().resolve("malilib-ui");
        if (!Files.isDirectory(dir)) {
            FileUtils.createDirectoriesIfMissing(dir);
        }
        return dir;
    }

    public enum SortMode implements IConfigOptionListEntry {
        DEFAULT("default"),
        NAME("name"),
        CUSTOM("custom");

        private final String value;

        SortMode(String value) {
            this.value = value;
        }

        @Override
        public String getStringValue() {
            return this.value;
        }

        @Override
        public String getDisplayName() {
            return StringUtils.getTranslatedOrFallback("malilib-ui.sort." + this.value, this.value);
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            SortMode[] all = values();
            int i = this.ordinal() + (forward ? 1 : -1);
            return all[(i + all.length) % all.length];
        }

        @Override
        public IConfigOptionListEntry fromString(String value) {
            for (SortMode mode : values()) {
                if (mode.value.equals(value)) {
                    return mode;
                }
            }
            return DEFAULT;
        }
    }

    public static class General {
        public static final ConfigBoolean ENABLED = new ConfigBoolean("enabled", true).apply(PREFIX);
        public static final ConfigBoolean SHOW_ICONS = new ConfigBoolean("showIcons", true).apply(PREFIX);
        public static final ConfigBoolean BLUR = new ConfigBoolean("blur", true).apply(PREFIX);
        public static final ConfigBoolean COMPACT = new ConfigBoolean("compact", false).apply(PREFIX);
        public static final ConfigBoolean PIN_STRIP = new ConfigBoolean("pinStrip", true).apply(PREFIX);
        public static final ConfigInteger PIN_MODE = new ConfigInteger("pinMode", 2, 0, 2).apply(PREFIX);
        public static final ConfigBoolean STYLE_OTHER = new ConfigBoolean("styleOtherScreens", false).apply(PREFIX);
        public static final ConfigInteger SIDEBAR_WIDTH = new ConfigInteger("sidebarWidth", 196, 140, 320, true).apply(PREFIX);
        public static final ConfigInteger CORNER_RADIUS = new ConfigInteger("cornerRadius", 8, 0, 16, true).apply(PREFIX);
        public static final ConfigOptionList MOD_SORT = new ConfigOptionList("modSort", SortMode.DEFAULT).apply(PREFIX);
        public static final ConfigOptionList TAB_SORT = new ConfigOptionList("tabSort", SortMode.DEFAULT).apply(PREFIX);

        public static final List<IConfigBase> OPTIONS = List.of(
                ENABLED, SHOW_ICONS, BLUR, COMPACT, PIN_STRIP, PIN_MODE, STYLE_OTHER,
                SIDEBAR_WIDTH, CORNER_RADIUS, MOD_SORT, TAB_SORT);
    }

    public static class Colors {
        public static final ConfigColor ACCENT = new ConfigColor("accent", "#7FB4FF").apply(PREFIX);
        public static final ConfigBoolean PANEL_FOLLOW = new ConfigBoolean("panelFollowAccent", true).apply(PREFIX);
        public static final ConfigColor PANEL = new ConfigColor("panel", "#12161F").apply(PREFIX);
        public static final ConfigColor TEXT = new ConfigColor("text", "#EDEFF4").apply(PREFIX);
        public static final ConfigInteger OPACITY = new ConfigInteger("opacity", 84, 30, 100, true).apply(PREFIX);
        public static final ConfigInteger CONTROL_OPACITY = new ConfigInteger("controlOpacity", 100, 20, 100, true).apply(PREFIX);
        public static final ConfigInteger SCRIM = new ConfigInteger("scrim", 54, 0, 100, true).apply(PREFIX);

        public static final List<IConfigBase> OPTIONS = List.of(ACCENT, PANEL_FOLLOW, PANEL, TEXT, OPACITY, CONTROL_OPACITY, SCRIM);
    }

    public static void loadFromFile() {
        Path file = dir().resolve(FILE_NAME);
        if (!Files.exists(file)) {
            Path legacy = FileUtils.getConfigDirectory().resolve("malilib-ui.json");
            if (Files.exists(legacy)) {
                try {
                    Files.copy(legacy, file);
                } catch (Exception ignored) {
                }
            }
        }
        if (Files.exists(file) && Files.isReadable(file)) {
            JsonElement element = JsonUtils.parseJsonFile(file);
            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();
                ConfigUtils.readConfigBase(root, "General", General.OPTIONS);
                ConfigUtils.readConfigBase(root, "Colors", Colors.OPTIONS);
            } else {
                MalilibUI.LOGGER.warn("Failed to parse config file '{}'", file.toAbsolutePath());
            }
        }
    }

    public static void saveToFile() {
        Path dir = dir();
        if (Files.isDirectory(dir)) {
            JsonObject root = new JsonObject();
            ConfigUtils.writeConfigBase(root, "General", General.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Colors", Colors.OPTIONS);
            JsonUtils.writeJsonToFile(root, dir.resolve(FILE_NAME));
        }
    }

    @Override
    public void load() {
        loadFromFile();
    }

    @Override
    public void save() {
        saveToFile();
    }
}
