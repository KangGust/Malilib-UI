package kang.malilib.ui.gui;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * 绘制工具。圆角用一张运行时生成的抗锯齿遮罩贴图做九宫格绘制，
 * 所以弧边是平滑的（不是逐行 fill 的阶梯）。
 */
public final class Draw {
    private static final int MASK = 32;
    private static final Map<Integer, Identifier> CORNERS = new HashMap<>();

    private Draw() {
    }

    public static Font font() {
        return Minecraft.getInstance().font;
    }

    public static int width(String text) {
        return font().width(text);
    }

    public static void rect(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        if (w > 0 && h > 0) {
            g.fill(x, y, x + w, y + h, argb);
        }
    }

    public static void roundRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int argb) {
        if (w <= 0 || h <= 0) {
            return;
        }
        int r = Math.min(radius, Math.min(w, h) / 2);
        if (r <= 0) {
            rect(g, x, y, w, h, argb);
            return;
        }
        Identifier tex = cornerTexture(argb);
        rect(g, x, y + r, w, h - r * 2, argb);
        rect(g, x + r, y, w - r * 2, r, argb);
        rect(g, x + r, y + h - r, w - r * 2, r, argb);
        blit(g, tex, x, y, x + r, y + r, 0f, 1f, 0f, 1f);
        blit(g, tex, x + w - r, y, x + w, y + r, 1f, 0f, 0f, 1f);
        blit(g, tex, x, y + h - r, x + r, y + h, 0f, 1f, 1f, 0f);
        blit(g, tex, x + w - r, y + h - r, x + w, y + h, 1f, 0f, 1f, 0f);
    }

    /** 1px 描边圆角矩形；inner 是内部要填的颜色 */
    public static void roundFrame(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int border, int inner) {
        roundRect(g, x, y, w, h, radius, border);
        roundRect(g, x + 1, y + 1, w - 2, h - 2, Math.max(0, radius - 1), inner);
    }

    /** 柔和投影：几层递减透明度的圆角矩形，比逐像素模糊便宜很多 */
    public static void shadow(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius) {
        int base = 0x00000000 | 0x40000000;
        for (int i = 3; i >= 1; i--) {
            int a = (int) (0x12 * (4 - i) / 3.0f);
            roundRect(g, x - i, y - i, w + i * 2, h + i * 2, radius + i, (base & 0xFFFFFF) | (a << 24));
        }
    }

    public static void text(GuiGraphicsExtractor g, String text, int x, int y, int color) {
        g.text(font(), text, x, y, color, false);
    }

    public static void textShadow(GuiGraphicsExtractor g, String text, int x, int y, int color) {
        g.text(font(), text, x, y, color, true);
    }

    public static void textCentered(GuiGraphicsExtractor g, String text, int cx, int y, int color) {
        g.text(font(), text, cx - width(text) / 2, y, color, false);
    }

    public static void scissor(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.enableScissor(x, y, x + w, y + h);
    }

    public static String trim(String text, int maxWidth) {
        if (text == null || maxWidth <= 0) {
            return "";
        }
        if (width(text) <= maxWidth) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (width(sb.toString() + c + "…") > maxWidth) {
                break;
            }
            sb.append(c);
        }
        return sb + "…";
    }

    /** 单项圆形图标（用圆角遮罩画，避免额外贴图） */
    public static void dot(GuiGraphicsExtractor g, int cx, int cy, int size, int color) {
        roundRect(g, cx - size / 2, cy - size / 2, size, size, size / 2, color);
    }

    public static void blit(GuiGraphicsExtractor g, Identifier tex, int x0, int y0, int x1, int y1,
            float u0, float u1, float v0, float v1) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(tex);
        g.blit(texture.getTextureView(), texture.getSampler(), x0, y0, x1, y1, u0, u1, v0, v1);
    }

    /** 每个颜色一张左上角遮罩贴图（白色不可行，26.3 没有着色器颜色，只能把颜色烘进贴图） */
    private static Identifier cornerTexture(int argb) {
        return CORNERS.computeIfAbsent(argb, color -> {
            NativeImage image = new NativeImage(NativeImage.Format.RGBA, MASK, MASK, false);
            int a = (color >>> 24) & 0xFF;
            int r = (color >> 16) & 0xFF;
            int gg = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            for (int py = 0; py < MASK; py++) {
                for (int px = 0; px < MASK; px++) {
                    double dx = px + 0.5 - MASK;
                    double dy = py + 0.5 - MASK;
                    double coverage = MASK - Math.sqrt(dx * dx + dy * dy) + 0.5;
                    coverage = Math.max(0.0, Math.min(1.0, coverage));
                    image.setPixel(px, py, ((int) Math.round(a * coverage) << 24) | (r << 16) | (gg << 8) | b);
                }
            }
            Identifier id = Identifier.fromNamespaceAndPath("malilib-ui",
                    "round/" + Integer.toHexString(color));
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, image));
            return id;
        });
    }
}
