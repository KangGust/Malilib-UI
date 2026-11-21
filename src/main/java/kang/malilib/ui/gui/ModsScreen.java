package kang.malilib.ui.gui;

import java.util.ArrayList;
import java.util.List;

import fi.dy.masa.malilib.config.IConfigBase;
import kang.malilib.ui.config.UiConfigs;
import net.minecraft.resources.Identifier;
import kang.malilib.ui.config.UiStore;
import kang.malilib.ui.core.Actions;
import kang.malilib.ui.core.ConfigIndex;
import kang.malilib.ui.core.ModEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * 主界面：左侧是「常用 / 功能组 / 模组（含各自功能页）」的可滚动导航，
 * 右侧内嵌原 masa 界面真正的配置列表。
 */
public class ModsScreen extends Screen {
    private static final int PAD = 8;
    private static final int HEADER_H = 30;
    private static final int FOOTER_H = 26;
    private static final int SECTION_H = 20;
    private static final int MOD_H = 36;
    private static final int TAB_H = 22;
    private static final int PIN_H = 24;
    private static final int GROUP_H = 24;

    private static final int K_SECTION = 0;
    private static final int K_PIN = 1;
    private static final int K_GROUP = 2;
    private static final int K_MOD = 3;
    private static final int K_TAB = 4;
    private static final int K_MATCH = 5;

    private final Screen parent;
    private final Embedded embedded = new Embedded();
    private final List<Row> rows = new ArrayList<>();

    private ModEntry selected;
    private float scroll;
    private int contentHeight;

    private int leftX;
    private int leftY;
    private int leftW;
    private int leftH;
    private int rowsX;
    private int rowsY;
    private int rowsW;
    private int rowsH;
    private int listX;
    private int listY;
    private int listW;
    private int listH;

    private boolean barDrag;
    private boolean bodyDrag;
    private int dragSide;
    private double lastDragY;
    private double pressY;
    private boolean reorderMode;
    private ModEntry dragMod;
    private Row pressRow;
    private double pressX;
    private boolean dragMoved;
    private int lastSidebar = -1;
    private int lastHeight = -1;
    private Dropdown modMenu;
    private EditBox searchBox;
    private Dropdown scope;
    private int searchX;
    private int searchW;
    private int scopeX;
    private int scopeW = 84;
    private int gearX;
    private int headerCardH = 24;

    private static final class Row {
        final int kind;
        final String label;
        final String sub;
        final ModEntry mod;
        final ConfigIndex.Tab tab;
        final UiStore.Ref ref;
        final UiStore.Group group;
        int y;
        int h;

        Row(int kind, String label, String sub, ModEntry mod, ConfigIndex.Tab tab, UiStore.Ref ref, UiStore.Group group) {
            this.kind = kind;
            this.label = label;
            this.sub = sub;
            this.mod = mod;
            this.tab = tab;
            this.ref = ref;
            this.group = group;
        }
    }

    public ModsScreen(Screen parent, String preferredModId) {
        super(Component.literal("Malilib UI"));
        this.parent = parent;
        this.selected = resolveMod(preferredModId);
        this.scope = new Dropdown(List.of(Text.of("malilib-ui.scope.current"), Text.of("malilib-ui.scope.all")), 0);
    }

    private boolean scopeAll() {
        return this.scope != null && this.scope.selected == 1;
    }

    /** 切换搜索范围时同步右侧过滤：全范围模式由左栏显示结果，右侧不过滤 */
    private void applyScope() {
        if (this.scopeAll()) {
            this.embedded.setFilter("");
        } else {
            this.embedded.setFilter(this.searchText());
        }
    }

    private static ModEntry resolveMod(String preferredModId) {
        ConfigIndex index = ConfigIndex.get();
        if (preferredModId != null) {
            ModEntry entry = index.byId(preferredModId);
            if (entry != null) {
                return entry;
            }
        }
        String last = UiStore.get().lastMod;
        if (last != null && !last.isEmpty()) {
            ModEntry entry = index.byId(last);
            if (entry != null) {
                return entry;
            }
        }
        List<ModEntry> mods = index.mods();
        return mods.isEmpty() ? null : mods.get(0);
    }

    @Override
    protected void init() {
        if (this.searchBox == null) {
            this.searchBox = new UiEditBox(0, 0, 120, 16, Component.literal("Search"));
            this.searchBox.setMaxLength(80);
            this.searchBox.setHint(Component.literal("Search..."));
        }
        this.layout();
        this.searchBox.setFocused(true);
        ConfigIndex.rebuildHandler = () -> {
            if (this.embedded.screen() != null) {
                this.embedded.rebuild();
            }
        };
    }

    private String searchText() {
        return this.searchBox == null ? "" : this.searchBox.getValue();
    }

    private boolean searchFocused() {
        return this.searchBox != null && this.searchBox.isFocused();
    }

    private void layout() {
        this.leftX = PAD;
        this.leftY = PAD + HEADER_H;
        this.leftW = Math.min(Theme.sidebarWidth(), Math.max(120, this.width / 2 - 20));
        this.leftH = Math.max(60, this.height - this.leftY - PAD);

        this.rowsX = this.leftX + 4;
        this.rowsY = this.leftY + 4;
        this.rowsW = this.leftW - 16;
        this.rowsH = this.leftH - 8 - FOOTER_H;

        int rightX = this.leftX + this.leftW + PAD;
        this.listX = rightX + 6;
        this.listY = this.leftY + HEADER_H;
        this.listW = Math.max(80, this.width - this.listX - PAD - 6);
        this.listH = Math.max(60, this.leftH - HEADER_H - 6);
        this.headerCardH = HEADER_H - 6;
        this.gearX = this.width - PAD - 22;
        this.searchX = this.listX - 6;
        this.scopeX = this.gearX - 6 - this.scopeW;
        this.searchW = Math.max(60, this.scopeX - 6 - this.searchX);
        if (this.searchBox != null) {
            this.searchBox.setX(this.searchX);
            this.searchBox.setY(PAD + 1);
            this.searchBox.setWidth(this.searchW);
            this.searchBox.setHeight(this.headerCardH);
        }

        if (this.selected != null) {
            this.embedded.open(this.selected, this.listX, this.listY, this.listW, this.listH);
        }
    }

    /* ── 行模型 ───────────────────────────────────────────── */

    private int rowH(int normal, int compact) {
        return Theme.compact() ? compact : normal;
    }

    private void buildRows() {
        this.ensureModMenu();
        this.rows.clear();
        UiStore store = UiStore.get();
        ConfigIndex index = ConfigIndex.get();
        int y = 0;

        if (this.scopeAll() && !this.searchText().isEmpty()) {
            String needle = this.searchText().toLowerCase();
            y = this.addSection(y, Text.of("malilib-ui.section.results"));
            for (ModEntry mod : index.mods()) {
                boolean modHit = mod.name.toLowerCase().contains(needle);
                for (IConfigBase config : index.configsOf(mod).values()) {
                    if (modHit || config.getName().toLowerCase().contains(needle)
                            || config.getConfigGuiDisplayName().toLowerCase().contains(needle)) {
                        y = this.add(y, new Row(K_MATCH, config.getConfigGuiDisplayName(), config.getName(),
                                mod, null, null, null), this.rowH(24, 20));
                    }
                }
            }
            this.contentHeight = y;
            this.clampScroll();
            return;
        }

        if (!store.pins.isEmpty()) {
            y = this.addSection(y, Text.of("malilib-ui.section.pins"));
            for (UiStore.Ref ref : store.pins) {
                IConfigBase config = index.find(ref.mod, ref.config);
                if (config != null) {
                    y = this.add(y, new Row(K_PIN, config.getConfigGuiDisplayName(), "", null, null, ref, null),
                            this.rowH(PIN_H, 20));
                }
            }
        }
        if (!store.groups.isEmpty()) {
            y = this.addSection(y, Text.of("malilib-ui.section.groups"));
            for (UiStore.Group group : store.groups) {
                y = this.add(y, new Row(K_GROUP, group.name, "", null, null, null, group), this.rowH(GROUP_H, 20));
            }
        }
        y = this.addSection(y, Text.of("malilib-ui.section.mods"));
        for (ModEntry mod : index.mods()) {
            y = this.add(y, new Row(K_MOD, mod.name, mod.version, mod, null, null, null), this.rowH(MOD_H, 30));
            if (mod == this.selected) {
                for (ConfigIndex.Tab tab : index.tabs(mod)) {
                    y = this.add(y, new Row(K_TAB, tab.label(), "", mod, tab, null, null), this.rowH(TAB_H, 19));
                }
            }
        }
        this.contentHeight = y;
        this.clampScroll();
    }

    private int addSection(int y, String title) {
        return this.add(y, new Row(K_SECTION, title, "", null, null, null, null), this.rowH(SECTION_H, 16));
    }

    private int add(int y, Row row, int height) {
        row.y = y;
        row.h = height;
        this.rows.add(row);
        return y + height;
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight - this.rowsH);
    }

    private void clampScroll() {
        this.scroll = Math.max(0f, Math.min(this.scroll, this.maxScroll()));
    }

    private Row rowAt(double mouseX, double mouseY) {
        int local = (int) (mouseY - this.rowsY + this.scroll);
        for (Row row : this.rows) {
            if (local >= row.y && local < row.y + row.h) {
                return row;
            }
        }
        return null;
    }

    /* ── 渲染 ─────────────────────────────────────────────── */

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
        if (this.minecraft != null && this.minecraft.level != null
                && UiConfigs.General.BLUR.getBooleanValue()) {
            this.extractBlurredBackground(g);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
        this.buildRows();
        if (this.lastSidebar != Theme.sidebarWidth() || this.lastHeight != this.height) {
            this.lastSidebar = Theme.sidebarWidth();
            this.lastHeight = this.height;
            this.layout();
            this.buildRows();
        }

        Draw.rect(g, 0, 0, this.width, this.height, Theme.scrim());
        this.drawHeader(g, mouseX, mouseY);

        int radius = Theme.radius();
        Draw.shadow(g, this.leftX, this.leftY, this.leftW, this.leftH, radius);
        Draw.roundRect(g, this.leftX, this.leftY, this.leftW, this.leftH, radius, Theme.panel());

        int rightX = this.listX - 6;
        int rightW = this.width - rightX - PAD;
        Draw.shadow(g, rightX, this.leftY, rightW, this.leftH, radius);
        Draw.roundRect(g, rightX, this.leftY, rightW, this.leftH, radius, Theme.panel());

        this.drawSidebar(g, mouseX, mouseY);
        this.drawContentHeader(g, rightX, rightW, mouseX, mouseY);

        Draw.scissor(g, this.listX - 2, this.listY - 2, this.listW + 4, this.listH + 4);
        this.embedded.render(g, mouseX, mouseY, partialTicks);
        g.disableScissor();
        if (this.modMenu != null) {
            this.modMenu.drawPopup(g, mouseX, mouseY);
        }
        if (this.scope != null) {
            this.scope.drawPopup(g, mouseX, mouseY);
        }
    }

    private void drawHeader(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int cardY = PAD + 1;
        int cardH = this.headerCardH;
        Draw.roundRect(g, this.leftX, cardY, this.leftW, cardH, 7, Theme.surface());

        Identifier self = Icons.self();
        int iconSize = 20;
        int iconX = this.leftX + 5;
        int iconY = cardY + (cardH - iconSize) / 2;
        if (self != null) {
            Draw.roundRect(g, iconX, iconY, iconSize, iconSize, 5, Theme.surfaceHigh());
            Draw.scissor(g, iconX + 1, iconY + 1, iconSize - 2, iconSize - 2);
            Draw.blit(g, self, iconX + 1, iconY + 1, iconX + iconSize - 1, iconY + iconSize - 1, 0f, 1f, 0f, 1f);
            g.disableScissor();
        } else {
            Draw.roundRect(g, iconX, iconY, iconSize, iconSize, 5, Theme.accentSoft(0x33));
        }

        int textX = iconX + iconSize + 7;
        int textW = this.leftX + this.leftW - textX - 6;
        Draw.textShadow(g, Draw.trim("Malilib UI", textW), textX, cardY + 3, Theme.text());
        Draw.text(g, Draw.trim(kang.malilib.ui.MalilibUI.VERSION, textW), textX, cardY + 12, Theme.accentSoft(0xA0));

        boolean focused = this.searchFocused();
        if (this.searchBox != null) {
            Draw.roundFrame(g, this.searchX, cardY, this.searchW, cardH, cardH / 2,
                    focused ? Theme.accentSoft(0x90) : Theme.outline(), Theme.surface());
            this.searchBox.extractRenderState(g, mouseX, mouseY, 0f);
            if (this.searchText().isEmpty()) {
                Draw.text(g, "Search...", this.searchX + 9, cardY + (cardH - 8) / 2, Theme.textOff());
            }
            if (!this.searchText().isEmpty()) {
                Icons.icon(g, "x", this.searchX + this.searchW - 14, cardY + (cardH - 10) / 2, 10, Theme.textOff());
            }
        }

        boolean gearHover = this.hover(mouseX, mouseY, this.gearX, cardY, 22, cardH);
        Draw.roundRect(g, this.gearX, cardY, 22, cardH, 5, gearHover ? Theme.surfaceHigh() : Theme.surface());
        Icons.iconCentered(g, "settings", this.gearX, cardY, 22, cardH, 14, gearHover ? Theme.text() : Theme.textDim());

        if (this.scope != null) {
            this.scope.drawBox(g, this.scopeX, cardY, this.scopeW, cardH, mouseX, mouseY);
        }
    }

    private void drawSidebar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int y0 = this.rowsY - (int) this.scroll;
        Draw.scissor(g, this.rowsX - 2, this.rowsY, this.rowsW + 4, this.rowsH);
        for (Row row : this.rows) {
            int y = y0 + row.y;
            if (y + row.h < this.rowsY - 4 || y > this.rowsY + this.rowsH + 4) {
                continue;
            }
            this.drawRow(g, row, y, mouseX, mouseY);
        }
        g.disableScissor();
        this.drawScrollbar(g, this.rowsX + this.rowsW + 4, this.rowsY, this.rowsH, mouseX, mouseY);

        int footY = this.leftY + this.leftH - FOOTER_H;
        int btnY = footY + 3;
        int btnH = FOOTER_H - 8;
        boolean footHover = this.hover(mouseX, mouseY, this.rowsX, btnY, this.rowsW, btnH);
        Draw.roundRect(g, this.rowsX, btnY, this.rowsW, btnH, 6, footHover ? Theme.surfaceHigh() : Theme.surface());
        String footText = Text.of("malilib-ui.button.newGroup");
        int footTextW = Draw.width(footText);
        int footStart = this.rowsX + (this.rowsW - footTextW - 16) / 2;
        Icons.icon(g, "plus", footStart, btnY + (btnH - 10) / 2, 10, footHover ? Theme.accent() : Theme.textDim());
        Draw.text(g, footText, footStart + 14, btnY + (btnH - 8) / 2, Theme.textDim());
    }

    private void drawRow(GuiGraphicsExtractor g, Row row, int y, int mouseX, int mouseY) {
        int x = this.rowsX;
        int w = this.rowsW;
        boolean hovered = this.hover(mouseX, mouseY, x, y, w, row.h - 2);

        switch (row.kind) {
            case K_SECTION -> {
                Draw.text(g, Draw.trim(row.label, w - 8), x + 4, y + 8, Theme.textOff());
            }
            case K_MOD -> {
                boolean active = row.mod == this.selected;
                if (active) {
                    Draw.roundRect(g, x, y + 1, w, row.h - 3, 6, Theme.surfaceHigh());
                    Draw.roundRect(g, x, y + 6, 3, row.h - 13, 1, Theme.accent());
                } else if (hovered) {
                    Draw.roundRect(g, x, y + 1, w, row.h - 3, 6, Theme.surface());
                }
                int iconSize = 22;
                int iconX = x + 8;
                int iconY = y + (row.h - iconSize) / 2;
                this.drawModIcon(g, row.mod, iconX, iconY, iconSize);
                int textX = iconX + iconSize + 8;
                int textW = w - (textX - x) - 8;
                Draw.text(g, Draw.trim(row.label, textW), textX, y + 7, active ? Theme.text() : Theme.textDim());
                if (!row.sub.isEmpty()) {
                    Draw.text(g, Draw.trim(row.sub, textW), textX, y + 19, Theme.textOff());
                }
            }
            case K_TAB -> {
                boolean active = this.embedded.activeTabLabel() != null
                        && this.embedded.activeTabLabel().equals(row.label);
                if (hovered || active) {
                    Draw.roundRect(g, x + 18, y + 1, w - 18, row.h - 2, 5, Theme.surface());
                }
                if (active) {
                    Draw.roundRect(g, x + 18, y + 6, 3, row.h - 12, 1, Theme.accent());
                }
                Draw.text(g, Draw.trim(row.label, w - 40), x + 30, y + 7, active ? Theme.text() : Theme.textOff());
            }
            case K_MATCH -> {
                if (hovered) {
                    Draw.roundRect(g, x, y + 1, w, row.h - 2, 6, Theme.surface());
                }
                if (row.mod != null) {
                    this.drawModIcon(g, row.mod, x + 6, y + 5, 14);
                }
                boolean dual = row.sub != null && !row.sub.isEmpty() && !row.sub.equalsIgnoreCase(row.label);
                int matchW = w - 34;
                Draw.text(g, Draw.trim(row.label, matchW), x + 26, dual ? y + 3 : y + 7, Theme.textDim());
                if (dual) {
                    Draw.text(g, Draw.trim(row.sub, matchW), x + 26, y + 12, Theme.textOff());
                }
            }
            case K_PIN -> {
                if (hovered) {
                    Draw.roundRect(g, x, y + 1, w, row.h - 2, 6, Theme.surface());
                }
                IConfigBase config = ConfigIndex.get().find(row.ref.mod, row.ref.config);
                ModEntry mod = ConfigIndex.get().byId(row.ref.mod);
                if (mod != null) {
                    this.drawModIcon(g, mod, x + 6, y + 5, 14);
                }
                int textW = w - 60;
                String en = config == null ? "" : config.getName();
                boolean dual = !en.isEmpty() && !en.equalsIgnoreCase(row.label);
                Draw.text(g, Draw.trim(row.label, textW), x + 26, dual ? y + 4 : y + 8, Theme.textDim());
                if (dual) {
                    Draw.text(g, Draw.trim(en, textW), x + 26, y + 13, Theme.textOff());
                }
                if (config != null) {
                    this.drawPinControl(g, config, x + w - 34, y + 5, 28, 14, hovered);
                }
            }
            case K_GROUP -> {
                if (hovered) {
                    Draw.roundRect(g, x, y + 1, w, row.h - 2, 6, Theme.surface());
                }
                Draw.dot(g, x + 12, y + row.h / 2, 6, Theme.accent());
                Draw.text(g, Draw.trim(row.label, w - 110), x + 24, y + 8, Theme.textDim());
                boolean on = Actions.isGroupActive(row.group);
                this.drawChip(g, x + w - 76, y + 4, 34, 16, Text.of("malilib-ui.button.on"), this.hover(mouseX, mouseY, x + w - 76, y + 4, 34, 16), on);
                this.drawChip(g, x + w - 38, y + 4, 34, 16, Text.of("malilib-ui.button.off"), this.hover(mouseX, mouseY, x + w - 38, y + 4, 34, 16), false);
            }
            default -> {
            }
        }
    }

    private void drawPinControl(GuiGraphicsExtractor g, IConfigBase config, int x, int y, int w, int h, boolean hovered) {
        if (config instanceof fi.dy.masa.malilib.config.IConfigBoolean) {
            boolean on = Actions.isOn(config);
            int track = on ? Theme.accent() : Theme.mix(Theme.panel(), 0xFFFFFFFF, 0.18f);
            Draw.roundRect(g, x, y, w, h, h / 2, track);
            Draw.dot(g, on ? x + w - h / 2 - 1 : x + h / 2 + 1, y + h / 2, h - 4, on ? 0xFFFFFFFF : Theme.textOff());
        } else {
            Draw.roundRect(g, x - 26, y - 1, w + 26, h + 2, h / 2 + 1, hovered ? Theme.surfaceHigh() : Theme.surface());
            Draw.textCentered(g, Draw.trim(Actions.displayValue(config), w + 20), x + 13, y + 3, Theme.textDim());
        }
    }

    private void drawContentHeader(GuiGraphicsExtractor g, int rightX, int rightW, int mouseX, int mouseY) {
        int y = this.leftY + 6;
        ModEntry mod = this.embedded.mod();
        if (mod == null) {
            return;
        }
        this.drawModIcon(g, mod, rightX + 10, y, 20);
        String title = mod.name;
        Draw.text(g, Draw.trim(title, rightW - 190), rightX + 36, y + 1, Theme.text());
        if (!mod.version.isEmpty()) {
            Draw.text(g, Draw.trim(mod.version, rightW - 190), rightX + 36, y + 11, Theme.accentSoft(0xA0));
        }
        String tab = this.embedded.activeTabLabel();
        if (tab != null) {
            int tx = rightX + 36 + Math.max(Draw.width(title), Draw.width(mod.version)) + 10;
            Draw.text(g, Draw.trim("· " + tab, rightW - (tx - rightX) - 40), tx, y + 6, Theme.textOff());
        }
        int starW = 22;
        int starX = rightX + rightW - starW - 8;
        boolean starHover = this.hover(mouseX, mouseY, starX, y - 1, starW, 20);
        Draw.roundRect(g, starX, y - 1, starW, 20, 5, starHover ? Theme.surfaceHigh() : Theme.surface());
        Icons.iconCentered(g, "star", starX, y - 1, starW, 20, 12, starHover ? Theme.accent() : Theme.textOff());

        int switchW = 90;
        int switchX = starX - switchW - 6;
        if (this.modMenu != null) {
            this.modMenu.drawBox(g, switchX, y - 1, switchW, 20, mouseX, mouseY);
        }
    }

    private void ensureModMenu() {
        List<ModEntry> mods = ConfigIndex.get().mods();
        List<String> names = new ArrayList<>();
        for (ModEntry mod : mods) {
            names.add(mod.name);
        }
        int index = Math.max(0, mods.indexOf(this.selected));
        if (this.modMenu == null || !this.modMenu.options.equals(names)) {
            this.modMenu = new Dropdown(names, index);
        } else {
            this.modMenu.selected = index;
        }
    }

    private void selectMenuIndex() {
        if (this.modMenu == null) {
            return;
        }
        List<ModEntry> mods = ConfigIndex.get().mods();
        int index = this.modMenu.selected;
        if (index < 0 || index >= mods.size()) {
            return;
        }
        this.selected = mods.get(index);
        this.embedded.open(this.selected, this.listX, this.listY, this.listW, this.listH);
    }

    private void cycleMod(int delta) {
        List<ModEntry> mods = ConfigIndex.get().mods();
        if (mods.isEmpty()) {
            return;
        }
        int index = mods.indexOf(this.selected);
        int next = Math.floorMod(index + delta, mods.size());
        this.selected = mods.get(next);
        this.embedded.open(this.selected, this.listX, this.listY, this.listW, this.listH);
    }

    private void drawScrollbar(GuiGraphicsExtractor g, int x, int y, int h, int mouseX, int mouseY) {
        int max = this.maxScroll();
        if (max <= 0) {
            return;
        }
        Draw.roundRect(g, x, y, 4, h, 2, Theme.alpha(0xFFFFFFFF, 0.06f));
        float ratio = (float) this.rowsH / (this.rowsH + max);
        int barH = Math.max(20, (int) (h * ratio));
        int barY = y + (int) ((h - barH) * (this.scroll / max));
        boolean hovered = this.barDrag || this.hover(mouseX, mouseY, x - 3, y, 10, h);
        Draw.roundRect(g, x, barY, 4, barH, 2, hovered ? Theme.accent() : Theme.alpha(0xFFFFFFFF, 0.22f));
    }

    private void drawModIcon(GuiGraphicsExtractor g, ModEntry mod, int x, int y, int size) {
        if (Theme.icons() && mod.icon() != null) {
            Draw.blit(g, mod.icon(), x, y, x + size, y + size, 0f, 1f, 0f, 1f);
        } else {
            Draw.roundRect(g, x, y, size, size, 4, Theme.surfaceHigh());
            String letter = mod.name.isEmpty() ? "?" : mod.name.substring(0, 1).toUpperCase();
            Draw.textCentered(g, letter, x + size / 2, y + size / 2 - 4, Theme.textDim());
        }
    }

    private void drawChip(GuiGraphicsExtractor g, int x, int y, int w, int h, String label, boolean hovered, boolean active) {
        int bg = active ? Theme.accentSoft(0x40) : (hovered ? Theme.surfaceHigh() : Theme.surface());
        Draw.roundRect(g, x, y, w, h, Math.min(6, h / 2), bg);
        Draw.textCentered(g, Draw.trim(label, w - 8), x + w / 2, y + (h - 8) / 2, active ? Theme.accent() : Theme.textDim());
    }

    private boolean hover(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    /* ── 交互 ─────────────────────────────────────────────── */

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        double mx = click.x();
        double my = click.y();

        if (click.button() == 0 && this.hover(mx, my, this.rowsX + this.rowsW + 2, this.rowsY, 8, this.rowsH)) {
            this.barDrag = true;
            this.scrollFromMouse(my);
            return true;
        }
        if (this.hover(mx, my, this.searchX, PAD + 1, this.searchW, this.headerCardH)) {
            this.searchBox.setFocused(true);
            if (!this.searchText().isEmpty() && mx > this.searchX + this.searchW - 16) {
                this.searchBox.setValue("");
                this.embedded.setFilter("");
            } else {
                this.searchBox.mouseClicked(click, doubleClick);
            }
            return true;
        }
        this.searchBox.setFocused(false);
        if (this.hover(mx, my, this.gearX, PAD + 1, 22, this.headerCardH)) {
            return this.openScreen(new SettingsScreen(this));
        }
        int rightX = this.listX - 6;
        int rightW = this.width - rightX - PAD;
        if (this.hover(mx, my, rightX + rightW - 30, this.leftY + 5, 22, 20)) {
            return this.openScreen(new PickerScreen(this, PickerScreen.Mode.PIN, this.embedded.mod()));
        }
        int switchW = 90;
        int switchX = rightX + rightW - 30 - switchW - 6;
        if (this.modMenu != null && this.modMenu.mouseClicked(mx, my)) {
            this.selectMenuIndex();
            return true;
        }
        if (this.scope != null && this.scope.mouseClicked(mx, my)) {
            this.applyScope();
            return true;
        }
        int footY = this.leftY + this.leftH - FOOTER_H;
        if (this.hover(mx, my, this.rowsX, footY + 3, this.rowsW, FOOTER_H - 8)) {
            return this.openScreen(new PickerScreen(this, PickerScreen.Mode.GROUP, this.embedded.mod()));
        }

        if (this.hover(mx, my, this.rowsX, this.rowsY, this.rowsW + 8, this.rowsH)) {
            Row row = this.rowAt(mx, my);
            if (row != null && row.kind == K_MOD && mx - this.rowsX < 30) {
                this.dragMod = row.mod;
                this.pressY = my;
                this.reorderMode = false;
                return true;
            }
            this.pressRow = row;
            this.pressX = mx;
            this.pressY = my;
            this.dragMoved = false;
            this.beginDrag(my, 0);
            return true;
        }
        if (this.hover(mx, my, this.listX, this.listY, this.listW, this.listH)) {
            if (this.embedded.mouseClicked(click, doubleClick)) {
                return true;
            }
            this.beginDrag(my, 1);
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    private void beginDrag(double my, int side) {
        this.bodyDrag = true;
        this.dragSide = side;
        this.lastDragY = my;
    }

    private boolean clickRow(Row row, double mx, double my) {
        int x = this.rowsX;
        int w = this.rowsW;
        switch (row.kind) {
            case K_MOD -> {
                if (mx - x < 30) {
                    this.dragMod = row.mod;
                    this.pressY = my;
                    this.reorderMode = false;
                    return true;
                }
                this.selectMod(row.mod);
                return true;
            }
            case K_TAB -> {
                this.selectTab(row.mod, row.tab);
                return true;
            }
            case K_MATCH -> {
                this.selected = row.mod;
                this.embedded.open(row.mod, this.listX, this.listY, this.listW, this.listH);
                this.embedded.setFilter(row.sub == null ? "" : row.sub);
                return true;
            }
            case K_PIN -> {
                IConfigBase config = ConfigIndex.get().find(row.ref.mod, row.ref.config);
                if (config != null) {
                    if (mx > x + w - 40) {
                        Actions.toggle(config);
                        Actions.save(row.ref.mod);
                    } else {
                        this.selectMod(ConfigIndex.get().byId(row.ref.mod));
                    }
                }
                return true;
            }
            case K_GROUP -> {
                if (mx > x + w - 80) {
                    if (mx > x + w - 40) {
                        Actions.disableGroup(row.group);
                    } else {
                        Actions.enableGroup(row.group);
                    }
                } else {
                    this.openScreen(new GroupEditorScreen(this, row.group));
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void selectMod(ModEntry mod) {
        if (mod == null) {
            return;
        }
        this.embedded.flush();
        if (mod == this.selected) {
            this.selected = null;
            return;
        }
        this.selected = mod;
        this.embedded.open(mod, this.listX, this.listY, this.listW, this.listH);
        this.scroll = 0f;
    }

    private void selectTab(ModEntry mod, ConfigIndex.Tab tab) {
        this.selected = mod;
        this.embedded.open(mod, this.listX, this.listY, this.listW, this.listH);
        this.embedded.selectTab(tab.index(), tab.label());
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dragX, double dragY) {
        double my = click.y();
        if (this.barDrag) {
            this.scrollFromMouse(my);
            return true;
        }
        if (this.dragMod != null) {
            if (!this.reorderMode && Math.abs(my - this.pressY) > 4) {
                this.reorderMode = true;
            }
            if (this.reorderMode) {
                this.reorderTo(my);
                return true;
            }
        }
        if (this.pressRow != null && !this.reorderMode
                && (this.pressRow.kind == K_PIN || this.pressRow.kind == K_GROUP)
                && Math.abs(my - this.pressY) > 6) {
            this.reorderMode = true;
        }
        if (this.reorderMode) {
            this.reorderTo(my);
            return true;
        }
        if (this.bodyDrag) {
            if (!this.dragMoved) {
                if (Math.abs(my - this.pressY) <= 4 && Math.abs(click.x() - this.pressX) <= 4) {
                    return true;
                }
                this.dragMoved = true;
            }
            if (this.dragSide == 1 && this.embedded.mouseDragged(click, click.x() - this.pressX, my - this.pressY)) {
                this.lastDragY = my;
                return true;
            }
            double delta = my - this.lastDragY;
            this.lastDragY = my;
            if (this.dragSide == 1) {
                this.embedded.dragScroll(delta);
            } else {
                this.scroll -= (float) delta;
                this.clampScroll();
            }
            return true;
        }
        return super.mouseDragged(click, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (this.barDrag || this.bodyDrag || this.reorderMode) {
            Row row = this.pressRow;
            boolean clicked = !this.dragMoved && !this.reorderMode && row != null;
            boolean rightClick = click.button() == 1;
            this.barDrag = false;
            this.bodyDrag = false;
            this.reorderMode = false;
            this.dragMod = null;
            this.pressRow = null;
            this.dragMoved = false;
            if (clicked && row != null) {
                if (rightClick && row.kind == K_PIN && row.ref != null) {
                    UiStore.get().togglePin(row.ref.mod, row.ref.config);
                    return true;
                }
                this.clickRow(row, click.x(), click.y());
            }
            return true;
        }
        this.embedded.mouseReleased(click);
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (this.modMenu != null && this.modMenu.mouseScrolled(mouseX, mouseY, vertical)) {
            return true;
        }
        if (this.scope != null && this.scope.mouseScrolled(mouseX, mouseY, vertical)) {
            return true;
        }
        if (this.hover(mouseX, mouseY, this.rowsX, this.rowsY, this.rowsW + 10, this.rowsH)) {
            this.scroll -= (float) (vertical * 26);
            this.clampScroll();
            return true;
        }
        return this.embedded.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (this.searchFocused()) {
            if (this.searchBox != null && this.searchBox.keyPressed(input)) {
                this.applyScope();
                return true;
            }
        }
        if (this.embedded.keyPressed(input)) {
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (this.searchFocused() && this.searchBox != null && this.searchBox.charTyped(input)) {
            this.applyScope();
            return true;
        }
        return this.embedded.charTyped(input) || super.charTyped(input);
    }

    private void scrollFromMouse(double my) {
        int max = this.maxScroll();
        if (max <= 0) {
            return;
        }
        float ratio = Math.max(0f, Math.min(1f, (float) ((my - this.rowsY) / this.rowsH)));
        this.scroll = ratio * max;
    }

    /** 拖拽排序：把被拖的条目移到鼠标下的同类型条目位置 */
    private void reorderTo(double my) {
        Row target = this.rowAt(0, my);
        if (target == null) {
            return;
        }
        UiStore store = UiStore.get();

        if (this.dragMod != null) {
            if (target.kind != K_MOD || target.mod == this.dragMod) {
                return;
            }
            int from = store.modOrder.indexOf(this.dragMod.modId);
            int to = store.modOrder.indexOf(target.mod.modId);
            if (from < 0 || to < 0 || from == to) {
                return;
            }
            store.modOrder.add(to, store.modOrder.remove(from));
            store.save();
            UiConfigs.General.MOD_SORT.setOptionListValue(UiConfigs.SortMode.CUSTOM);
            UiConfigs.saveToFile();
            return;
        }

        Row origin = this.pressRow;
        if (origin == null || origin.kind != target.kind) {
            return;
        }
        if (origin.kind == K_PIN && origin.ref != null && target.ref != null) {
            int from = indexOfRef(store.pins, origin.ref.key());
            int to = indexOfRef(store.pins, target.ref.key());
            if (from < 0 || to < 0 || from == to) {
                return;
            }
            store.pins.add(to, store.pins.remove(from));
            store.save();
        } else if (origin.kind == K_GROUP && origin.group != null && target.group != null) {
            int from = store.groups.indexOf(origin.group);
            int to = store.groups.indexOf(target.group);
            if (from < 0 || to < 0 || from == to) {
                return;
            }
            store.groups.add(to, store.groups.remove(from));
            store.save();
        }
    }

    private static int indexOfRef(List<UiStore.Ref> refs, String key) {
        for (int i = 0; i < refs.size(); i++) {
            if (refs.get(i).key().equals(key)) {
                return i;
            }
        }
        return -1;
    }

    private boolean openScreen(Screen screen) {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(screen);
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        this.embedded.flush();
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
