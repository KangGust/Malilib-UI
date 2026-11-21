package kang.malilib.ui.gui;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/** 本模组自带贴图：自身图标 + Tabler 图标集（可运行时按颜色染色，结果按色缓存） */
public final class Icons {
    public static final Identifier SELF = Identifier.fromNamespaceAndPath("malilib-ui", "self_icon");
    private static final Map<String, Identifier> ICONS = new HashMap<>();
    private static boolean selfTried;
    private static boolean selfLoaded;

    private Icons() {
    }

    public static Identifier self() {
        if (!selfTried) {
            selfTried = true;
            selfLoaded = load("/assets/malilib-ui/icon.png", SELF) != null;
        }
        return selfLoaded ? SELF : null;
    }

    public static void icon(GuiGraphicsExtractor g, String name, int x, int y, int size) {
        icon(g, name, x, y, size, 0xFFFFFFFF);
    }

    /** 按颜色画图标（白色源图 × 目标色，alpha 保留） */
    public static void icon(GuiGraphicsExtractor g, String name, int x, int y, int size, int color) {
        Identifier id = tinted(name, color);
        if (id != null) {
            Draw.blit(g, id, x, y, x + size, y + size, 0f, 1f, 0f, 1f);
        }
    }

    public static void iconCentered(GuiGraphicsExtractor g, String name, int x, int y, int w, int h, int size) {
        icon(g, name, x + (w - size) / 2, y + (h - size) / 2, size, 0xFFFFFFFF);
    }

    public static void iconCentered(GuiGraphicsExtractor g, String name, int x, int y, int w, int h, int size, int color) {
        icon(g, name, x + (w - size) / 2, y + (h - size) / 2, size, color);
    }

    public static Identifier tinted(String name, int color) {
        String key = name + "#" + Integer.toHexString(color);
        Identifier cached = ICONS.get(key);
        if (cached != null) {
            return cached;
        }
        Identifier id = Identifier.fromNamespaceAndPath("malilib-ui",
                "icon/" + name + "_" + Integer.toHexString(color));
        try (InputStream in = Icons.class.getResourceAsStream("/assets/malilib-ui/textures/icons/" + name + ".png")) {
            if (in == null) {
                return null;
            }
            NativeImage base = NativeImage.read(in);
            int w = base.getWidth();
            int h = base.getHeight();
            int[] pixels = base.getPixelsABGR();
            NativeImage out = new NativeImage(NativeImage.Format.RGBA, w, h, true);
            int tr = (color >> 16) & 0xFF;
            int tg = (color >> 8) & 0xFF;
            int tb = color & 0xFF;
            int ta = (color >>> 24) & 0xFF;
            for (int i = 0; i < pixels.length; i++) {
                int abgr = pixels[i];
                int a = ((abgr >>> 24) & 0xFF) * ta / 255;
                if (a == 0) {
                    continue;
                }
                int r = (abgr & 0xFF) * tr / 255;
                int gg = ((abgr >> 8) & 0xFF) * tg / 255;
                int b = ((abgr >> 16) & 0xFF) * tb / 255;
                out.setPixelABGR(i % w, i / w, (a << 24) | (b << 16) | (gg << 8) | r);
            }
            base.close();
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, out));
            ICONS.put(key, id);
            return id;
        } catch (Exception e) {
            return null;
        }
    }

    private static Identifier load(String resource, Identifier id) {
        try (InputStream in = Icons.class.getResourceAsStream(resource)) {
            if (in == null) {
                return null;
            }
            NativeImage image = NativeImage.read(in);
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, image));
            return id;
        } catch (Exception e) {
            return null;
        }
    }
}
