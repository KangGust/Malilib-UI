package kang.malilib.ui.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigOptionList;
import fi.dy.masa.malilib.config.IConfigValue;
import kang.malilib.ui.config.UiStore;

/** 对任意 masa 配置项的读写与功能组开关 */
public final class Actions {
    private static final Map<String, Map<String, String>> SNAPSHOTS = new HashMap<>();

    private Actions() {
    }

    public static boolean wouldToggle(IConfigBase config) {
        return config instanceof IConfigBoolean || config instanceof IConfigOptionList;
    }

    public static boolean isOn(IConfigBase config) {
        if (config instanceof IConfigBoolean bool) {
            return bool.getBooleanValue();
        }
        if (config instanceof IConfigOptionList list) {
            return !list.getOptionListValue().getStringValue()
                    .equals(list.getDefaultOptionListValue().getStringValue());
        }
        return false;
    }

    public static void toggle(IConfigBase config) {
        if (config instanceof IConfigBoolean bool) {
            bool.setBooleanValue(!bool.getBooleanValue());
        } else if (config instanceof IConfigOptionList list) {
            list.setOptionListValue(list.getOptionListValue().cycle(true));
        }
        touch(config);
    }

    public static String value(IConfigBase config) {
        return config instanceof IConfigValue value ? value.getStringValue() : "";
    }

    public static String displayValue(IConfigBase config) {
        if (config instanceof IConfigBoolean bool) {
            return String.valueOf(bool.getBooleanValue());
        }
        if (config instanceof IConfigOptionList list) {
            return list.getOptionListValue().getDisplayName();
        }
        return value(config);
    }

    public static void setValue(IConfigBase config, String value) {
        if (config instanceof IConfigValue configValue) {
            configValue.setValueFromString(value);
            touch(config);
        }
    }

    private static void touch(IConfigBase config) {
        config.markDirty();
        config.checkIfClean();
    }

    public static void save(String modId) {
        if (modId != null && !modId.isEmpty()) {
            ConfigManager.getInstance().onConfigsChanged(modId);
        }
    }

    public static void saveAll(java.util.Collection<String> modIds) {
        java.util.Set<String> unique = new java.util.LinkedHashSet<>(modIds);
        for (String modId : unique) {
            save(modId);
        }
    }

    public static boolean isGroupActive(UiStore.Group group) {
        return SNAPSHOTS.containsKey(group.name);
    }

    /** 开：把组里每个功能写回它记录的“开”值，同时记下当前值用于还原 */
    public static void enableGroup(UiStore.Group group) {
        Map<String, String> snapshot = SNAPSHOTS.computeIfAbsent(group.name, key -> new LinkedHashMap<>());
        snapshot.clear();
        for (UiStore.Item item : group.items) {
            IConfigBase config = ConfigIndex.get().find(item.mod, item.config);
            if (config == null) {
                continue;
            }
            snapshot.put(item.key(), value(config));
            if (!item.value.isEmpty()) {
                setValue(config, item.value);
            }
        }
        saveAll(group.items.stream().map(item -> item.mod).toList());
    }

    /** 关：还原开之前记下的值 */
    public static void disableGroup(UiStore.Group group) {
        Map<String, String> snapshot = SNAPSHOTS.remove(group.name);
        if (snapshot == null) {
            return;
        }
        for (UiStore.Item item : group.items) {
            IConfigBase config = ConfigIndex.get().find(item.mod, item.config);
            String previous = snapshot.get(item.key());
            if (config != null && previous != null) {
                setValue(config, previous);
            }
        }
        saveAll(group.items.stream().map(item -> item.mod).toList());
    }
}
