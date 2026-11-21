package kang.malilib.ui.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import fi.dy.masa.malilib.util.FileUtils;
import kang.malilib.ui.MalilibUI;

/**
 * 界面自身的状态：模组/功能排序、置顶快捷开关、自定义功能组。
 * 与 UiConfigs（malilib 配置项）分开存，避免污染配置界面的选项列表。
 */
public final class UiStore {
    private static final String FILE_NAME = "layout.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class Ref {
        public String mod;
        public String config;

        public Ref() {
        }

        public Ref(String mod, String config) {
            this.mod = mod;
            this.config = config;
        }

        public String key() {
            return mod + "/" + config;
        }
    }

    /** 功能组：一组配置 + 每个配置“开启”时的取值，可一键开/关 */
    public static class Group {
        public String name = "";
        public List<Item> items = new ArrayList<>();

        public Group() {
        }

        public Group(String name) {
            this.name = name;
        }
    }

    public static class Item extends Ref {
        public String value = "";

        public Item() {
        }

        public Item(String mod, String config, String value) {
            super(mod, config);
            this.value = value;
        }
    }

    public List<String> modOrder = new ArrayList<>();
    public Map<String, List<String>> tabOrder = new LinkedHashMap<>();
    public List<Ref> pins = new ArrayList<>();
    public List<Group> groups = new ArrayList<>();
    public String lastMod = "";
    public Map<String, String> lastTab = new LinkedHashMap<>();

    private static UiStore instance;

    public static UiStore get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public boolean isPinned(String mod, String config) {
        String key = mod + "/" + config;
        for (Ref ref : pins) {
            if (ref.key().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public void togglePin(String mod, String config) {
        String key = mod + "/" + config;
        if (!pins.removeIf(ref -> ref.key().equals(key))) {
            pins.add(new Ref(mod, config));
        }
        save();
    }

    /** 自定义排序：把 order 里缺失的 mod 追加到末尾 */
    public void syncModOrder(List<String> modIds) {
        modOrder.removeIf(id -> !modIds.contains(id));
        for (String id : modIds) {
            if (!modOrder.contains(id)) {
                modOrder.add(id);
            }
        }
    }

    public void moveMod(String modId, int delta) {
        int from = modOrder.indexOf(modId);
        int to = from + delta;
        if (from < 0 || to < 0 || to >= modOrder.size()) {
            return;
        }
        modOrder.add(to, modOrder.remove(from));
        save();
    }

    public List<String> tabOrderOf(String modId) {
        return tabOrder.computeIfAbsent(modId, key -> new ArrayList<>());
    }

    public String lastTabOf(String modId) {
        return lastTab.getOrDefault(modId, "");
    }

    public void setLastTab(String modId, String label) {
        lastTab.put(modId, label);
        save();
    }

    public void moveTab(String modId, String tabId, int delta) {
        List<String> order = tabOrderOf(modId);
        int from = order.indexOf(tabId);
        int to = from + delta;
        if (from < 0 || to < 0 || to >= order.size()) {
            return;
        }
        order.add(to, order.remove(from));
        save();
    }

    public void addGroup(Group group) {
        groups.add(group);
        save();
    }

    public void removeGroup(Group group) {
        groups.remove(group);
        save();
    }

    /** 恢复全部自定义排序 */
    public void resetOrder() {
        modOrder.clear();
        tabOrder.clear();
        save();
    }

    private static UiStore load() {
        Path file = UiConfigs.dir().resolve(FILE_NAME);
        if (!Files.exists(file)) {
            Path legacy = FileUtils.getConfigDirectory().resolve("malilib-ui-layout.json");
            if (Files.exists(legacy)) {
                try {
                    Files.copy(legacy, file);
                } catch (Exception ignored) {
                }
            }
        }
        if (Files.exists(file) && Files.isReadable(file)) {
            try {
                UiStore store = GSON.fromJson(Files.readString(file), new TypeToken<UiStore>() {
                }.getType());
                if (store != null) {
                    return store;
                }
            } catch (Exception e) {
                MalilibUI.LOGGER.warn("Failed to read {}", FILE_NAME, e);
            }
        }
        return new UiStore();
    }

    public void save() {
        try {
            Path dir = UiConfigs.dir();
            if (!Files.isDirectory(dir)) {
                FileUtils.createDirectoriesIfMissing(dir);
            }
            Files.writeString(dir.resolve(FILE_NAME), GSON.toJson(this));
        } catch (Exception e) {
            MalilibUI.LOGGER.warn("Failed to save {}", FILE_NAME, e);
        }
    }
}
