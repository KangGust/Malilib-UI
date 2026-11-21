package kang.malilib.ui.core;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.mojang.blaze3d.platform.NativeImage;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.util.data.ModInfo;

/** 一个带配置界面的模组：名字、版本、图标、界面工厂 */
public final class ModEntry {
    public final ModInfo info;
    public final String modId;
    public final String name;
    public final String version;
    private Identifier icon;
    private boolean iconTried;

    public ModEntry(ModInfo info) {
        this.info = info;
        this.modId = info.modId();
        this.name = info.modName();
        this.version = FabricLoader.getInstance().getModContainer(this.modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("");
    }

    public GuiBase createScreen() {
        return this.info.configScreenSupplier() == null ? null : this.info.configScreenSupplier().get();
    }

    public Identifier icon() {
        if (!this.iconTried) {
            this.iconTried = true;
            this.icon = loadIcon(this.modId);
        }
        return this.icon;
    }

    private static Identifier loadIcon(String modId) {
        try {
            Optional<ModContainer> optional = FabricLoader.getInstance().getModContainer(modId);
            if (optional.isEmpty()) {
                return null;
            }
            ModContainer container = optional.get();
            Optional<String> iconPath = container.getMetadata().getIconPath(64);
            if (iconPath.isEmpty()) {
                return null;
            }
            Optional<Path> file = container.findPath(iconPath.get());
            if (file.isEmpty() || !Files.isReadable(file.get())) {
                return null;
            }
            Identifier id = Identifier.fromNamespaceAndPath("malilib-ui", "mod_icon/" + modId);
            try (InputStream in = Files.newInputStream(file.get())) {
                NativeImage image = NativeImage.read(in);
                Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, image));
            }
            return id;
        } catch (Exception e) {
            return null;
        }
    }
}
