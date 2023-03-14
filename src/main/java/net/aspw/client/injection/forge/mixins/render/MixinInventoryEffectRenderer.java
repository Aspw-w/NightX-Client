package net.aspw.client.injection.forge.mixins.render;

import net.aspw.client.Client;
import net.aspw.client.features.module.modules.client.Hud;
import net.aspw.client.injection.forge.mixins.gui.MixinGuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(InventoryEffectRenderer.class)
public abstract class MixinInventoryEffectRenderer extends MixinGuiContainer {

    @Shadow
    private boolean hasActivePotionEffects;

    @Overwrite
    public void updateActivePotionEffects() {
        final Hud hud = Client.moduleManager.getModule(Hud.class);
        if (!hud.getInvEffectOffset().get()) {
            this.guiLeft = (this.width - this.xSize) / 2;
            this.hasActivePotionEffects = !this.mc.thePlayer.getActivePotionEffects().isEmpty();
        } else if (!this.mc.thePlayer.getActivePotionEffects().isEmpty()) {
            this.guiLeft = 160 + (this.width - this.xSize - 200) / 2;
            this.hasActivePotionEffects = true;
        } else {
            this.guiLeft = (this.width - this.xSize) / 2;
            this.hasActivePotionEffects = false;
        }
    }

}