package kang.malilib.ui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import kang.malilib.ui.config.UiConfigs;
import kang.malilib.ui.core.ConfigIndex;
import kang.malilib.ui.gui.ModsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;

/** 打开任意 masa 配置界面时，换成我们的界面 */
@Mixin(Gui.class)
public class GuiSetScreenMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true, require = 0)
    private void malilibui$replaceConfigScreen(Screen screen, CallbackInfo ci) {
        if (!(screen instanceof GuiConfigsBase configs)
                || ConfigIndex.bypassReplace
                || ConfigIndex.navigationSuppressed()
                || !UiConfigs.General.ENABLED.getBooleanValue()) {
            return;
        }
        ci.cancel();
        Minecraft.getInstance().gui.setScreen(new ModsScreen(configs.getParent(), configs.getModId()));
    }
}
