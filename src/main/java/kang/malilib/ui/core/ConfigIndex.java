package kang.malilib.ui.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IConfigGui;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import kang.malilib.ui.config.UiConfigs;
import kang.malilib.ui.config.UiStore;
import kang.malilib.ui.mixin.accessor.ButtonBaseAccessor;
import kang.malilib.ui.mixin.accessor.GuiBaseAccessor;

/**
 * 所有 masa 配置界面的索引：模组列表、每个模组的功能页（原 tab 按钮）、以及全部配置项。
 * 功能页与配置项都是懒加载的：第一次访问时实例化原界面并“模拟”切换功能页来收集。
 */
public final class ConfigIndex {
    public record Tab(String label, int index) {
    }

    private static ConfigIndex instance;
    private static boolean suppressNavigation;

    /** 外部（例如 malilib 的滑杆/输入框切换按钮）改动了配置后，请求重建当前列表 */
    public static Runnable rebuildHandler;

    public static void requestRebuild() {
        if (rebuildHandler != null) {
            rebuildHandler.run();
        }
    }

    private final Map<String, ModEntry> entries = new LinkedHashMap<>();
    private final Map<String, List<Tab>> tabCache = new HashMap<>();
    private final Map<String, Map<String, IConfigBase>> configCache = new HashMap<>();

    public static ConfigIndex get() {
        if (instance == null) {
            instance = new ConfigIndex();
        }
        return instance;
    }

    /** 收集期间禁止真的切换界面（有的功能页会直接 openGui 到另一个界面） */
    public static boolean navigationSuppressed() {
        return suppressNavigation;
    }

    public List<ModEntry> mods() {
        List<ModEntry> list = new ArrayList<>();
        for (ModInfo info : Registry.CONFIG_SCREEN.getAllModsWithConfigScreens()) {
            if (info.configScreenSupplier() == null) {
                continue;
            }
            list.add(this.entries.computeIfAbsent(info.modId(), id -> new ModEntry(info)));
        }
        UiStore store = UiStore.get();
        store.syncModOrder(list.stream().map(mod -> mod.modId).toList());
        IConfigOptionListEntry mode = UiConfigs.General.MOD_SORT.getOptionListValue();
        if (mode == UiConfigs.SortMode.NAME) {
            list.sort(Comparator.comparing(mod -> mod.name, String.CASE_INSENSITIVE_ORDER));
        } else if (mode == UiConfigs.SortMode.CUSTOM) {
            list.sort(Comparator.comparingInt(mod -> store.modOrder.indexOf(mod.modId)));
        }
        return list;
    }

    public ModEntry byId(String modId) {
        for (ModEntry entry : this.mods()) {
            if (entry.modId.equals(modId)) {
                return entry;
            }
        }
        return null;
    }

    public List<Tab> tabs(ModEntry mod) {
        List<Tab> cached = this.tabCache.get(mod.modId);
        if (cached != null) {
            return cached;
        }
        List<Tab> result = new ArrayList<>();
        GuiBase screen = mod.createScreen();
        if (screen != null) {
            suppressNavigation = true;
            try {
                screen.width = 400;
                screen.height = 400;
                screen.initGui();
                List<ButtonBase> buttons = ((GuiBaseAccessor) screen).malilibui$getButtons();
                for (int i = 0; i < buttons.size(); i++) {
                    String label = tabLabel(buttons.get(i));
                    if (label != null) {
                        result.add(new Tab(label, i));
                    }
                }
            } catch (Exception ignored) {
            } finally {
                suppressNavigation = false;
            }
        }
        IConfigOptionListEntry mode = UiConfigs.General.TAB_SORT.getOptionListValue();
        List<String> order = UiStore.get().tabOrderOf(mod.modId);
        if (mode == UiConfigs.SortMode.NAME) {
            result.sort(Comparator.comparing(Tab::label, String.CASE_INSENSITIVE_ORDER));
        } else if (mode == UiConfigs.SortMode.CUSTOM) {
            result.sort(Comparator.comparingInt(tab -> {
                int index = order.indexOf(tab.label());
                return index < 0 ? Integer.MAX_VALUE : index;
            }));
        }
        this.tabCache.put(mod.modId, result);
        return result;
    }

    public int activeTab(GuiBase screen) {
        List<ButtonBase> buttons = ((GuiBaseAccessor) screen).malilibui$getButtons();
        for (int i = 0; i < buttons.size(); i++) {
            if (tabLabel(buttons.get(i)) != null && !((ButtonBaseAccessor) buttons.get(i)).malilibui$isEnabled()) {
                return i;
            }
        }
        return -1;
    }

    /** 触发原界面的功能页切换（直接调原按钮的监听器，避免播放点击音效） */
    public static void selectTab(GuiBase screen, int buttonIndex) {
        List<ButtonBase> buttons = ((GuiBaseAccessor) screen).malilibui$getButtons();
        if (buttonIndex < 0 || buttonIndex >= buttons.size()) {
            return;
        }
        ButtonBase button = buttons.get(buttonIndex);
        IButtonActionListener listener = ((ButtonBaseAccessor) button).malilibui$getListener();
        if (listener != null) {
            listener.actionPerformedWithButton(button, 0);
        }
    }

    public Map<String, IConfigBase> configsOf(ModEntry mod) {
        Map<String, IConfigBase> cached = this.configCache.get(mod.modId);
        if (cached != null) {
            return cached;
        }
        Map<String, IConfigBase> map = new LinkedHashMap<>();
        GuiBase screen = mod.createScreen();
        if (screen instanceof GuiConfigsBase) {
            suppressNavigation = true;
            try {
                screen.width = 400;
                screen.height = 400;
                screen.initGui();
                int original = this.activeTab(screen);
                for (Tab tab : this.tabs(mod)) {
                    selectTab(screen, tab.index());
                    for (GuiConfigsBase.ConfigOptionWrapper wrapper : ((IConfigGui) screen).getConfigs()) {
                        IConfigBase config = wrapper.getConfig();
                        if (config != null) {
                            map.putIfAbsent(config.getName(), config);
                        }
                    }
                }
                if (original >= 0) {
                    selectTab(screen, original);
                }
            } catch (Exception ignored) {
            } finally {
                suppressNavigation = false;
            }
        }
        this.configCache.put(mod.modId, map);
        return map;
    }

    public IConfigBase find(String modId, String configName) {
        ModEntry mod = this.byId(modId);
        return mod == null ? null : this.configsOf(mod).get(configName);
    }

    private static String tabLabel(ButtonBase button) {
        if (button.getY() < 0 || button.getY() > 46 || button.getHeight() <= 0 || button.getHeight() > 24) {
            return null;
        }
        String label = ((ButtonBaseAccessor) button).malilibui$getText();
        return label == null || label.isBlank() ? null : label;
    }
}
