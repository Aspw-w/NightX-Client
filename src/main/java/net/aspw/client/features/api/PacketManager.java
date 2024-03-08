package net.aspw.client.features.api;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.aspw.client.Launch;
import net.aspw.client.event.*;
import net.aspw.client.features.module.impl.combat.KillAura;
import net.aspw.client.features.module.impl.combat.LegitAura;
import net.aspw.client.features.module.impl.combat.TPAura;
import net.aspw.client.features.module.impl.other.BrandSpoofer;
import net.aspw.client.features.module.impl.visual.Animations;
import net.aspw.client.features.module.impl.visual.BetterView;
import net.aspw.client.features.module.impl.visual.Interface;
import net.aspw.client.protocol.ProtocolBase;
import net.aspw.client.utils.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

import java.util.Objects;

public class PacketManager extends MinecraftInstance implements Listenable {

    public static int swing;
    public static boolean isVisualBlocking = false;
    private static boolean flagged = false;
    public static int flagTicks;
    public static float eyeHeight;
    public static float lastEyeHeight;

    @EventTarget
    public void onWorld(WorldEvent event) {
        if (Objects.requireNonNull(Launch.moduleManager.getModule(BetterView.class)).getState())
            RotationUtils.Companion.enableLook();
        flagged = false;
        flagTicks = 1;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        for (Entity en : mc.theWorld.loadedEntityList) {
            if (shouldStopRender(en)) {
                en.renderDistanceWeight = 0.0;
            } else {
                en.renderDistanceWeight = 1.0;
            }
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        mc.leftClickCounter = 0;
        mc.thePlayer.prevRenderArmYaw = mc.thePlayer.rotationYaw;
        mc.thePlayer.prevRenderArmPitch = mc.thePlayer.rotationPitch;
        mc.thePlayer.renderArmYaw = mc.thePlayer.rotationYaw;
        mc.thePlayer.renderArmPitch = mc.thePlayer.rotationPitch;
        float START_HEIGHT = 1.62f;
        float END_HEIGHT = Animations.sneakLength.get() + 1.54f;
        lastEyeHeight = eyeHeight;
        if (mc.thePlayer.isSneaking()) {
            eyeHeight = END_HEIGHT;
        } else if (eyeHeight < START_HEIGHT) {
            float delta = START_HEIGHT - eyeHeight;
            delta *= 0.4;
            eyeHeight = START_HEIGHT - delta;
        }
        if (!Objects.requireNonNull(Launch.moduleManager.getModule(BetterView.class)).getState())
            Objects.requireNonNull(Launch.moduleManager.getModule(BetterView.class)).setState(true);
        if (!Objects.requireNonNull(Launch.moduleManager.getModule(BrandSpoofer.class)).getState())
            Objects.requireNonNull(Launch.moduleManager.getModule(BrandSpoofer.class)).setState(true);
        if ((Animations.swingAnimValue.get().equals("Smooth") || Animations.swingAnimValue.get().equals("Dash")) && event.getEventState() == EventState.PRE) {
            if (mc.thePlayer.swingProgressInt == 1) {
                swing = 9;
            } else {
                swing = Math.max(0, swing - 1);
            }
        }
        final KillAura killAura = Objects.requireNonNull(Launch.moduleManager.getModule(KillAura.class));
        final TPAura tpAura = Objects.requireNonNull(Launch.moduleManager.getModule(TPAura.class));
        final LegitAura legitAura = Objects.requireNonNull(Launch.moduleManager.getModule(LegitAura.class));
        if (Animations.swingLimitOnlyBlocking.get()) {
            if (mc.thePlayer.swingProgress >= 1f)
                mc.thePlayer.isSwingInProgress = false;
            if (mc.thePlayer.isBlocking() || (killAura.getState() && killAura.getTarget() != null && !killAura.getAutoBlockModeValue().get().equals("None") || tpAura.getState() && tpAura.isBlocking() || legitAura.getState() && legitAura.isBlocking())) {
                if (mc.thePlayer.swingProgress >= Animations.swingLimit.get())
                    mc.thePlayer.isSwingInProgress = false;
            }
        } else if (mc.thePlayer.swingProgress >= Animations.swingLimit.get()) {
            mc.thePlayer.isSwingInProgress = false;
        }
        if (Animations.fankeyBobbing.get() && MovementUtils.isMoving() && mc.thePlayer.onGround && !mc.thePlayer.isSneaking()) {
            mc.thePlayer.cameraYaw = 0.18f;
            mc.thePlayer.cameraPitch = 0.0f;
        }
    }

    @EventTarget
    public void onTeleport(TeleportEvent event) {
        flagged = true;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C03PacketPlayer && flagged) {
            if (mc.thePlayer.ticksExisted % 2 == 0)
                flagTicks++;
            if (flagTicks < 3) {
                if (RotationUtils.targetRotation != null) {
                    event.cancelEvent();
                    PacketUtils.sendPacketNoEvent(
                            new C03PacketPlayer.C06PacketPlayerPosLook(
                                    mc.thePlayer.posX,
                                    mc.thePlayer.posY,
                                    mc.thePlayer.posZ,
                                    mc.thePlayer.rotationYaw,
                                    mc.thePlayer.rotationPitch,
                                    mc.thePlayer.onGround
                            )
                    );
                    RotationUtils.Companion.reset();
                }
            } else {
                if (Objects.requireNonNull(Launch.moduleManager.getModule(Interface.class)).getState() && Objects.requireNonNull(Launch.moduleManager.getModule(Interface.class)).getTpDebugValue().get())
                    ClientUtils.displayChatMessage(Launch.CLIENT_CHAT + "tp");
                flagTicks = 1;
                flagged = false;
            }
        }

        if (ProtocolBase.getManager().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_10)) {
            if (packet instanceof C08PacketPlayerBlockPlacement) {
                ((C08PacketPlayerBlockPlacement) packet).facingX = 0.5F;
                ((C08PacketPlayerBlockPlacement) packet).facingY = 0.5F;
                ((C08PacketPlayerBlockPlacement) packet).facingZ = 0.5F;
            }
        }
    }

    public static boolean shouldStopRender(Entity entity) {
        return (EntityUtils.isMob(entity) ||
                EntityUtils.isAnimal(entity) ||
                entity instanceof EntityBoat ||
                entity instanceof EntityMinecart ||
                entity instanceof EntityItemFrame ||
                entity instanceof EntityTNTPrimed ||
                entity instanceof EntityArmorStand) &&
                entity != mc.thePlayer && mc.thePlayer.getDistanceToEntity(entity) > 35.0f;
    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}