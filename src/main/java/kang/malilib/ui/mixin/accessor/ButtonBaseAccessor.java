package kang.malilib.ui.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;

@Mixin(ButtonBase.class)
public interface ButtonBaseAccessor {
    @Accessor("displayString")
    String malilibui$getText();

    @Accessor("displayString")
    void malilibui$setText(String text);

    @Accessor("enabled")
    boolean malilibui$isEnabled();

    @Accessor("enabled")
    void malilibui$setEnabled(boolean enabled);

    @Accessor("actionListener")
    IButtonActionListener malilibui$getListener();
}
