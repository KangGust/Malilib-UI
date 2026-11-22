package kang.malilib.ui.gui;

import java.util.ArrayList;
import java.util.List;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import kang.malilib.ui.MalilibUI;
import kang.malilib.ui.config.UiConfigs;

/** 本模组自己的配置页，同样由上面的主界面渲染 */
public class UiConfigScreen extends GuiConfigsBase {
    public UiConfigScreen() {
        super(10, 50, MalilibUI.MOD_ID, null, "malilib-ui.config.title");
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        List<ConfigOptionWrapper> list = new ArrayList<>();
        list.add(new ConfigOptionWrapper(Text.of("malilib-ui.group.takeover")));
        list.addAll(ConfigOptionWrapper.createFor(List.of(
                UiConfigs.General.ENABLED, UiConfigs.General.STYLE_OTHER)));
        list.add(new ConfigOptionWrapper(Text.of("malilib-ui.group.appearance")));
        list.addAll(ConfigOptionWrapper.createFor(List.of(
                UiConfigs.Colors.ACCENT, UiConfigs.Colors.PANEL_FOLLOW, UiConfigs.Colors.PANEL,
                UiConfigs.Colors.TEXT, UiConfigs.Colors.OPACITY, UiConfigs.Colors.CONTROL_OPACITY,
                UiConfigs.Colors.SCRIM, UiConfigs.General.CORNER_RADIUS, UiConfigs.General.SIDEBAR_WIDTH)));
        list.add(new ConfigOptionWrapper(Text.of("malilib-ui.group.display")));
        list.addAll(ConfigOptionWrapper.createFor(List.of(
                UiConfigs.General.SHOW_ICONS, UiConfigs.General.BLUR, UiConfigs.General.COMPACT)));
        list.add(new ConfigOptionWrapper(Text.of("malilib-ui.group.sorting")));
        list.addAll(ConfigOptionWrapper.createFor(List.of(
                UiConfigs.General.PIN_MODE, UiConfigs.General.MOD_SORT, UiConfigs.General.TAB_SORT)));
        return list;
    }
}
