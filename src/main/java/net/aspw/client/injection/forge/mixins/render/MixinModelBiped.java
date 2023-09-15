package net.aspw.client.injection.forge.mixins.render;

import net.aspw.client.Client;
import net.aspw.client.features.module.impl.combat.KillAura;
import net.aspw.client.features.module.impl.combat.TPAura;
import net.aspw.client.features.module.impl.visual.SilentView;
import net.aspw.client.util.MinecraftInstance;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * The type Mixin model biped.
 */
@Mixin(ModelBiped.class)
public abstract class MixinModelBiped {

    /**
     * The Biped right arm.
     */
    @Shadow
    public ModelRenderer bipedRightArm;

    /**
     * The Held item right.
     */
    @Shadow
    public int heldItemRight;

    /**
     * The Biped head.
     */
    @Shadow
    public ModelRenderer bipedHead;

    /**
     * Sets rotation angles.
     *
     * @param p_setRotationAngles_1_ the p set rotation angles 1
     * @param p_setRotationAngles_2_ the p set rotation angles 2
     * @param p_setRotationAngles_3_ the p set rotation angles 3
     * @param p_setRotationAngles_4_ the p set rotation angles 4
     * @param p_setRotationAngles_5_ the p set rotation angles 5
     * @param p_setRotationAngles_6_ the p set rotation angles 6
     * @param p_setRotationAngles_7_ the p set rotation angles 7
     */
    @Shadow
    public abstract void setRotationAngles(float p_setRotationAngles_1_, float p_setRotationAngles_2_, float p_setRotationAngles_3_, float p_setRotationAngles_4_, float p_setRotationAngles_5_, float p_setRotationAngles_6_, Entity p_setRotationAngles_7_);

    /**
     * The Biped body.
     */
    @Shadow
    public ModelRenderer bipedBody;

    /**
     * The Biped headwear.
     */
    @Shadow
    public ModelRenderer bipedHeadwear;

    /**
     * The Biped left arm.
     */
    @Shadow
    public ModelRenderer bipedLeftArm;

    /**
     * The Biped right leg.
     */
    @Shadow
    public ModelRenderer bipedRightLeg;

    /**
     * The Biped left leg.
     */
    @Shadow
    public ModelRenderer bipedLeftLeg;

    /**
     * The Held item left.
     */
    @Shadow
    public int heldItemLeft;

    @Inject(method = "setRotationAngles", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;swingProgress:F"))
    private void revertSwordAnimation(float p_setRotationAngles1, float p_setRotationAngles2, float p_setRotationAngles3, float p_setRotationAngles4, float p_setRotationAngles5, float p_setRotationAngles6, Entity p_setRotationAngles7, CallbackInfo callbackInfo) {
        final SilentView silentView = Objects.requireNonNull(Client.moduleManager.getModule(SilentView.class));
        final KillAura killAura = Objects.requireNonNull(Client.moduleManager.getModule(KillAura.class));
        final TPAura tpAura = Objects.requireNonNull(Client.moduleManager.getModule(TPAura.class));
        if (p_setRotationAngles7 instanceof EntityPlayer && p_setRotationAngles7.equals(MinecraftInstance.mc.thePlayer) && silentView.getNormalRotationsValue().get() && !silentView.getSilentValue().get() && silentView.getState() && (silentView.getModuleCheckValue().get() && silentView.shouldRotate() || !silentView.getModuleCheckValue().get())) {
            bipedHead.rotateAngleX = (float) Math.toRadians(SilentView.lerp(MinecraftInstance.mc.timer.renderPartialTicks, SilentView.getPrevHeadPitch(), SilentView.getHeadPitch()));
        }
        if (heldItemRight == 3) {
            this.bipedRightArm.rotateAngleZ = 0F;
            this.bipedRightArm.rotateAngleY = -0.5235988F;
            return;
        }
        if (heldItemRight == 0 || heldItemRight == 2)
            return;
        if ((killAura.getState() && killAura.getTarget() != null && !killAura.getAutoBlockModeValue().get().equals("None") || tpAura.getState() && tpAura.isBlocking()) && MinecraftInstance.mc.gameSettings.thirdPersonView != 0 && p_setRotationAngles7 instanceof EntityPlayer && p_setRotationAngles7.equals(MinecraftInstance.mc.thePlayer)) {
            if (MinecraftInstance.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                this.bipedRightArm.rotateAngleZ = 0F;
                this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX - ((float) Math.PI / 0.94f) * 0.034f;
                this.bipedRightArm.rotateAngleY = -0.5235988F;
            }
        }
    }
}