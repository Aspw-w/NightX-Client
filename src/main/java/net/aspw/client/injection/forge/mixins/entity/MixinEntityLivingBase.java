package net.aspw.client.injection.forge.mixins.entity;

import de.enzaxd.viaforge.ViaForge;
import net.aspw.client.Client;
import net.aspw.client.event.JumpEvent;
import net.aspw.client.features.module.modules.client.AntiNausea;
import net.aspw.client.features.module.modules.client.BlockAnimations;
import net.aspw.client.features.module.modules.movement.DoubleJump;
import net.aspw.client.features.module.modules.movement.Flight;
import net.aspw.client.features.module.modules.movement.Jesus;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Objects;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity {


    @Shadow
    public int swingProgressInt;
    @Shadow
    public boolean isSwingInProgress;
    @Shadow
    public float swingProgress;
    @Shadow
    protected boolean isJumping;
    @Shadow
    private int jumpTicks;

    @Shadow
    protected abstract float getJumpUpwardsMotion();

    @Shadow
    public abstract PotionEffect getActivePotionEffect(Potion potionIn);

    @Shadow
    public abstract boolean isPotionActive(Potion potionIn);

    @Shadow
    public void onLivingUpdate() {
    }

    @Shadow
    protected abstract void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos);

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract ItemStack getHeldItem();

    @Shadow
    protected abstract void updateAITick();

    @Inject(method = "updatePotionEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionEffect;onUpdate(Lnet/minecraft/entity/EntityLivingBase;)Z"),
            locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void checkPotionEffect(CallbackInfo ci, Iterator<Integer> iterator, Integer integer, PotionEffect potioneffect) {
        if (potioneffect == null)
            ci.cancel();
    }

    /**
     * @author CCBlueX
     */
    @Overwrite
    protected void jump() {
        final JumpEvent jumpEvent = new JumpEvent(this.getJumpUpwardsMotion());
        Client.eventManager.callEvent(jumpEvent);
        if (jumpEvent.isCancelled())
            return;

        this.motionY = jumpEvent.getMotion();

        if (this.isPotionActive(Potion.jump))
            this.motionY += (float) (this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;

        if (this.isSprinting()) {
            float f = this.rotationYaw * 0.017453292F;
            this.motionX -= MathHelper.sin(f) * 0.2F;
            this.motionZ += MathHelper.cos(f) * 0.2F;
        }

        this.isAirBorne = true;
    }

    @ModifyConstant(method = "onLivingUpdate", constant = @Constant(doubleValue = 0.005D))
    private double ViaVersion_MovementThreshold(double constant) {
        if (ViaForge.getInstance().getVersion() > 47)
            return 0.003D;
        return 0.005D;
    }

    @Inject(method = "onLivingUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;isJumping:Z", ordinal = 1))
    private void onJumpSection(CallbackInfo callbackInfo) {
        if (Client.moduleManager.getModule(DoubleJump.class).getState() && isJumping && this.jumpTicks == 0) {
            this.jump();
            this.jumpTicks = 10;
        }

        final Jesus liquidWalk = Client.moduleManager.getModule(Jesus.class);

        if (liquidWalk.getState() && !isJumping && !isSneaking() && isInWater() &&
                liquidWalk.modeValue.get().equalsIgnoreCase("Swim") || Objects.requireNonNull(Client.moduleManager.getModule(Flight.class)).modeValue.get().equals("Water") && Objects.requireNonNull(Client.moduleManager.getModule(Flight.class)).getState()) {
            this.updateAITick();
        }
    }

    @Inject(method = "getLook", at = @At("HEAD"), cancellable = true)
    private void getLook(CallbackInfoReturnable<Vec3> callbackInfoReturnable) {
        if (((EntityLivingBase) (Object) this) instanceof EntityPlayerSP)
            callbackInfoReturnable.setReturnValue(getVectorForRotation(this.rotationPitch, this.rotationYaw));
    }

    @Inject(method = "isPotionActive(Lnet/minecraft/potion/Potion;)Z", at = @At("HEAD"), cancellable = true)
    private void isPotionActive(Potion p_isPotionActive_1_, final CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        final AntiNausea antiBlind = Client.moduleManager.getModule(AntiNausea.class);

        if ((p_isPotionActive_1_ == Potion.confusion || p_isPotionActive_1_ == Potion.blindness) && antiBlind.getState() && antiBlind.getConfusionEffect().get())
            callbackInfoReturnable.setReturnValue(false);
    }

    //visionfx sucks

    /**
     * @author
     * @reason
     */
    @Overwrite
    private int getArmSwingAnimationEnd() {
        int speed = Client.moduleManager.getModule(BlockAnimations.class).getState() ? 2 + (20 - BlockAnimations.SpeedSwing.get() - 16) : 6;
        return this.isPotionActive(Potion.digSpeed) ? speed - (1 + this.getActivePotionEffect(Potion.digSpeed).getAmplifier()) : (this.isPotionActive(Potion.digSlowdown) ? speed + (1 + this.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : speed);
    }

}
