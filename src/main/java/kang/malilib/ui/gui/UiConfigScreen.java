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
        list.addAll(ConfigOptionWrapper.createFor(UiConfigs.General.OPTIONS));
        list.addAll(ConfigOptionWrapper.createFor(UiConfigs.Colors.OPTIONS));
        return list;
    }
}
