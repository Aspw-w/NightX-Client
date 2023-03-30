package net.aspw.client.injection.forge.mixins.render;

import net.aspw.client.Client;
import net.aspw.client.features.module.modules.visual.Rotate;
import net.aspw.client.features.module.modules.visual.SilentView;
import net.aspw.client.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public abstract class MixinModelBiped<T extends MixinRendererLivingEntity> {

    @Shadow
    public ModelRenderer bipedRightArm;

    @Shadow
    public int heldItemRight;

    @Shadow
    public ModelRenderer bipedHead;

    @Shadow
    public abstract void setRotationAngles(float p_setRotationAngles_1_, float p_setRotationAngles_2_, float p_setRotationAngles_3_, float p_setRotationAngles_4_, float p_setRotationAngles_5_, float p_setRotationAngles_6_, Entity p_setRotationAngles_7_);

    @Shadow
    public ModelRenderer bipedBody;

    @Inject(method = "setRotationAngles", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;swingProgress:F"))
    private void revertSwordAnimation(float p_setRotationAngles_1_, float p_setRotationAngles_2_, float p_setRotationAngles_3_, float p_setRotationAngles_4_, float p_setRotationAngles_5_, float p_setRotationAngles_6_, Entity p_setRotationAngles_7_, CallbackInfo callbackInfo) {
        if (heldItemRight == 3)
            this.bipedRightArm.rotateAngleY = 0F;

        if (p_setRotationAngles_7_ instanceof EntityPlayer && p_setRotationAngles_7_.equals(Minecraft.getMinecraft().thePlayer)) {
            SilentView silentView = Client.moduleManager.getModule(SilentView.class);
            Rotate spinBot = Client.moduleManager.getModule(Rotate.class);
            if (spinBot.getState() && !spinBot.getPitchMode().get().equalsIgnoreCase("none"))
                this.bipedHead.rotateAngleX = spinBot.getPitch() / 57.295776f;
            if (silentView.getState() && silentView.getMode().get().equals("ETB") && silentView.shouldRotate()) {
                this.bipedHead.rotateAngleX = (int) RotationUtils.serverRotation.getPitch() / (140 / (int) Math.PI);
            }
            if (silentView.getHeadPitch().get() && silentView.getState() && silentView.getMode().get().equals("Normal") && silentView.shouldRotate()) {
                this.bipedHead.rotateAngleX = RotationUtils.serverRotation.getPitch() / 57.295776f;
            }
        }
    }
}