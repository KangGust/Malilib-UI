package kang.malilib.ui.gui;

import java.util.ArrayList;
import java.util.List;

/** 每帧登记“收藏星标”的点击区，供点击时命中判定 */
public final class PinHits {
    public record Hit(int x, int y, int w, int h, String config) {
    }

    private static final List<Hit> HITS = new ArrayList<>();

    private PinHits() {
    }

    public static void clear() {
        HITS.clear();
    }

    public static void add(int x, int y, int w, int h, String config) {
        HITS.add(new Hit(x, y, w, h, config));
    }

    public static String hitAt(double mx, double my) {
        for (Hit hit : HITS) {
            if (mx >= hit.x() && mx < hit.x() + hit.w() && my >= hit.y() && my < hit.y() + hit.h()) {
                return hit.config();
            }
        }
        return null;
    }
}
