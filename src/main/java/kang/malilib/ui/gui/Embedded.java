package kang.malilib.ui.gui;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiScrollBar;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import fi.dy.masa.malilib.render.GuiContext;
import kang.malilib.ui.config.UiConfigs;
import kang.malilib.ui.config.UiStore;
import kang.malilib.ui.core.ConfigIndex;
import kang.malilib.ui.core.ModEntry;
import kang.malilib.ui.mixin.accessor.GuiBaseAccessor;
import kang.malilib.ui.mixin.accessor.GuiConfigsBaseAccessor;
import kang.malilib.ui.mixin.accessor.WidgetListBaseAccessor;
import kang.malilib.ui.mixin.accessor.WidgetSearchBarAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

/** 右侧内容区：内嵌一个 masa 配置界面，只使用它的配置列表控件 */
public final class Embedded {
    private static final float ROW_HEIGHT = 22f;

    private ModEntry mod;
    private GuiBase screen;
    private WidgetListConfigOptions list;
    private String tabLabel;
    private String filter = "";
    private int x;
    private int y;
    private int w;
    private int h;
    private float dragAccum;
    private float pixelOffset;
    private double wheelAccum;

    public ModEntry mod() {
        return this.mod;
    }

    public GuiBase screen() {
        return this.screen;
    }

    public boolean isEmpty() {
        return this.mod == null;
    }

    public void open(ModEntry mod, int x, int y, int w, int h) {
        if (this.mod == mod && this.screen != null) {
            this.setBounds(x, y, w, h);
            return;
        }
        this.flush();
        this.mod = mod;
        this.screen = mod == null ? null : mod.createScreen();
        if (mod != null) {
            UiStore.get().lastMod = mod.modId;
        }
        this.w = 0;
        this.setBounds(x, y, w, h);
        if (mod != null) {
            this.restoreLastTab();
        }
    }

    /** 打开某模组时恢复上次停留的功能页 */
    private void restoreLastTab() {
        String wantLabel = UiStore.get().lastTabOf(this.mod.modId);
        if (wantLabel == null || wantLabel.isEmpty()) {
            return;
        }
        for (ConfigIndex.Tab tab : ConfigIndex.get().tabs(this.mod)) {
            if (tab.label().equals(wantLabel)) {
                this.selectTab(tab.index(), tab.label());
                return;
            }
        }
    }

    public void setBounds(int x, int y, int w, int h) {
        if (x == this.x && y == this.y && w == this.w && h == this.h && this.list != null) {
            return;
        }
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.rebuild();
    }

    /** 按内容区尺寸重建原界面与列表，并接管列表的坐标 */
    public void rebuild() {
        int keepScroll = this.list == null ? -1 : this.list.getScrollbar().getValue();
        this.list = null;
        if (!(this.screen instanceof GuiConfigsBase configs) || this.w <= 60 || this.h <= 60) {
            return;
        }
        RowScope.currentModId = this.mod == null ? "" : this.mod.modId;
        configs.width = this.w + 20;
        configs.height = this.h + 80;
        configs.initGui();
        GuiConfigsBaseAccessor access = (GuiConfigsBaseAccessor) configs;
        int proportional = Math.max(120, Math.round(this.w * 0.34f));
        access.malilibui$setConfigWidth(Math.max(84, Math.min(access.malilibui$getConfigWidth(), proportional)));
        WidgetListConfigOptions created = access.malilibui$createListWidget(this.x, this.y);
        created.setSize(this.w, this.h);
        ((WidgetListBaseAccessor) created).malilibui$setEntryHeight(
                UiConfigs.General.ENABLED.getBooleanValue() ? 26 : 22);
        created.initGui();
        if (created.getSearchBarWidget() != null) {
            created.getSearchBarWidget().setSearchOpen(true);
            created.refreshEntries();
        }
        this.list = created;
        if (keepScroll > 0) {
            GuiScrollBar bar = created.getScrollbar();
            bar.setValue(Math.min(keepScroll, bar.getMaxValue()));
        }
        this.dragAccum = 0f;
        this.refreshTabLabel();
        this.applyFilter();
    }

    /** 把我们顶部搜索栏的文字同步给原列表的过滤器（过滤逻辑仍用 malilib/Searchables） */
    public void setFilter(String text) {
        this.filter = text == null ? "" : text;
        this.applyFilter();
    }

    private void applyFilter() {
        if (this.list == null || this.list.getSearchBarWidget() == null) {
            return;
        }
        GuiTextFieldGeneric box = ((WidgetSearchBarAccessor) this.list.getSearchBarWidget()).malilibui$getSearchBox();
        if (box != null && !this.filter.equals(box.getValue())) {
            box.setValue(this.filter);
            this.list.refreshEntries();
            this.list.resetScrollbarPosition();
        }
    }

    private void refreshTabLabel() {
        this.tabLabel = null;
        if (this.mod == null || this.screen == null) {
            return;
        }
        int active = ConfigIndex.get().activeTab(this.screen);
        for (ConfigIndex.Tab tab : ConfigIndex.get().tabs(this.mod)) {
            if (tab.index() == active) {
                this.tabLabel = tab.label();
            }
        }
    }

    /** 切换功能页：走原按钮的监听器，再按我们的尺寸重建列表 */
    public void selectTab(int buttonIndex, String label) {
        if (this.screen == null) {
            return;
        }
        this.flush();
        ConfigIndex.selectTab(this.screen, buttonIndex);
        this.rebuild();
        this.tabLabel = label;
        if (this.mod != null && label != null) {
            UiStore.get().setLastTab(this.mod.modId, label);
        }
    }

    public String activeTabLabel() {
        return this.tabLabel;
    }

    public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
        if (this.list == null) {
            return;
        }
        GuiContext ctx = GuiContext.fromGuiGraphics(g);
        PinHits.clear();
        this.list.drawContents(ctx, mouseX, mouseY, partialTicks);
        GuiBaseAccessor access = (GuiBaseAccessor) this.list;
        access.malilibui$drawHoveredWidget(ctx, mouseX, mouseY);
        access.malilibui$drawButtonHoverTexts(ctx, mouseX, mouseY, partialTicks);
    }

    private MouseButtonEvent adjust(MouseButtonEvent click) {
        return click;
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        String config = PinHits.hitAt(click.x(), click.y());
        if (config != null && !RowScope.currentModId.isEmpty()) {
            UiStore.get().togglePin(RowScope.currentModId, config);
            this.rebuild();
            return true;
        }
        return this.list != null && this.list.onMouseClicked(this.adjust(click), doubleClick);
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        boolean handled = this.list != null && this.list.onMouseReleased(this.adjust(click));
        this.flush();
        return handled;
    }

    /** 把拖动转发给内嵌列表（滑杆长按拖动靠它） */
    public boolean mouseDragged(MouseButtonEvent click, double dragX, double dragY) {
        return this.list != null && this.list.mouseDragged(click, dragX, dragY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (this.list == null) {
            return false;
        }
        if (mouseX < this.x || mouseX > this.x + this.w || mouseY < this.y || mouseY > this.y + this.h) {
            return false;
        }
        this.wheelAccum += vertical * ROW_HEIGHT;
        int step = (int) (this.wheelAccum / ROW_HEIGHT);
        if (step != 0) {
            this.wheelAccum -= step * ROW_HEIGHT;
            GuiScrollBar bar = this.list.getScrollbar();
            bar.setValue(bar.getValue() - step);
        }
        this.pixelOffset = 0f;
        this.wheelAccum = 0;
        return true;
    }

    public boolean keyPressed(KeyEvent input) {
        return this.list != null && this.list.onKeyTyped(input);
    }

    public boolean charTyped(CharacterEvent input) {
        return this.list != null && this.list.onCharTyped(input);
    }

    /** 触屏/鼠标按住拖动滚动（换算成行数，和滚轮走同一套滚动条） */
    /** 右侧拖动：按整行滚动（与 malilib 原生一致，不做像素级平移） */
    public void dragScroll(double deltaY) {
        if (this.list == null) {
            return;
        }
        this.dragAccum += (float) deltaY;
        int step = (int) (this.dragAccum / ROW_HEIGHT);
        if (step != 0) {
            this.dragAccum -= step * ROW_HEIGHT;
            GuiScrollBar bar = this.list.getScrollbar();
            bar.setValue(bar.getValue() - step);
        }
    }

    /** 提交未保存的修改（切换功能页 / 切换模组 / 关闭界面时调用） */
    /** 到顶/到底后不再保留视觉偏移，避免越界拖动与松手回弹 */
    private float clampedOffset(float offset) {
        if (this.list == null) {
            return 0f;
        }
        GuiScrollBar bar = this.list.getScrollbar();
        if (offset > 0 && bar.getValue() <= 0) {
            return 0f;
        }
        if (offset < 0 && bar.getValue() >= bar.getMaxValue()) {
            return 0f;
        }
        return offset;
    }

    public void flush() {
        if (this.list == null || !(this.screen instanceof GuiConfigsBase)) {
            return;
        }
        if (this.list.wereConfigsModified()) {
            this.list.applyPendingModifications();
            ((GuiConfigsBaseAccessor) this.screen).malilibui$onSettingsChanged();
            this.list.clearConfigsModifiedFlag();
        }
    }

    public boolean isEmptyList() {
        return this.list != null && this.list.getCurrentEntries().isEmpty();
    }

    public int listX() {
        return this.x;
    }

    public int listY() {
        return this.y;
    }

    public int listWidth() {
        return this.w;
    }

    public int listHeight() {
        return this.h;
    }
}
