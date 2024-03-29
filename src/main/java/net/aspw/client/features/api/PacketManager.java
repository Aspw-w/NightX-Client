package net.aspw.client.features.api;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.aspw.client.Launch;
import net.aspw.client.event.*;
import net.aspw.client.features.module.impl.combat.KillAura;
import net.aspw.client.features.module.impl.combat.KillAuraRecode;
import net.aspw.client.features.module.impl.combat.TPAura;
import net.aspw.client.features.module.impl.other.BrandSpoofer;
import net.aspw.client.features.module.impl.visual.Animations;
import net.aspw.client.features.module.impl.visual.SilentRotations;
import net.aspw.client.protocol.ProtocolBase;
import net.aspw.client.utils.*;
import net.aspw.client.utils.render.RenderUtils;
import net.aspw.client.utils.timer.MSTimer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

import java.util.Objects;

public class PacketManager extends MinecraftInstance implements Listenable {

    private static final MSTimer packetCountTimer = new MSTimer();
    public static int swing;
    public static boolean isVisualBlocking = false;
    public static float eyeHeight;
    public static float lastEyeHeight;
    public static int sendPacketCounts;
    public static int receivePacketCounts;
    private int preSend = 0;
    private int preReceive = 0;
    public static int lastTpX = 0;
    public static int lastTpY = 0;
    public static int lastTpZ = 0;

    @EventTarget
    public void onWorld(WorldEvent event) {
        if (Objects.requireNonNull(Launch.moduleManager.getModule(SilentRotations.class)).getState())
            RotationUtils.Companion.enableLook();
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
    public void onRender3D(Render3DEvent event) {
        if (RotationUtils.targetRotation != null) {
            if (Objects.requireNonNull(Launch.moduleManager.getModule(SilentRotations.class)).getBodyTweaks().get()) {
                mc.thePlayer.renderYawOffset = RotationUtils.targetRotation.getYaw();
                mc.thePlayer.rotationYawHead = RotationUtils.targetRotation.getYaw();
            }
            mc.thePlayer.prevRenderArmYaw = RotationUtils.targetRotation.getYaw();
            mc.thePlayer.prevRenderArmPitch = RotationUtils.targetRotation.getPitch();
            mc.thePlayer.renderArmYaw = RotationUtils.targetRotation.getYaw();
            mc.thePlayer.renderArmPitch = RotationUtils.targetRotation.getPitch();
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        mc.leftClickCounter = 0;

        float START_HEIGHT = 1.62f;
        float END_HEIGHT;

        lastEyeHeight = eyeHeight;

        if (ProtocolBase.getManager().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_9) && ProtocolBase.getManager().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2) && !mc.isIntegratedServerRunning())
            END_HEIGHT = 1.47f;
        else if (ProtocolBase.getManager().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_14) && !mc.isIntegratedServerRunning())
            END_HEIGHT = 1.32f;
        else END_HEIGHT = 1.54f;

        float delta;
        if (ProtocolBase.getManager().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_9) && ProtocolBase.getManager().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2) && !mc.isIntegratedServerRunning())
            delta = 0.147f;
        else if (ProtocolBase.getManager().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_14) && !mc.isIntegratedServerRunning())
            delta = 0.132f;
        else delta = 0.154f;

        if (mc.thePlayer.isSneaking())
            eyeHeight = AnimationUtils.animate(END_HEIGHT, eyeHeight, RenderUtils.deltaTime * delta);
        else if (eyeHeight < START_HEIGHT)
            eyeHeight = AnimationUtils.animate(START_HEIGHT, eyeHeight, RenderUtils.deltaTime * delta);

        if (!Objects.requireNonNull(Launch.moduleManager.getModule(SilentRotations.class)).getState())
            Objects.requireNonNull(Launch.moduleManager.getModule(SilentRotations.class)).setState(true);
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
        final KillAuraRecode killAuraRecode = Objects.requireNonNull(Launch.moduleManager.getModule(KillAuraRecode.class));

        if (Animations.swingLimitOnlyBlocking.get()) {
            if (mc.thePlayer.swingProgress >= 1f)
                mc.thePlayer.isSwingInProgress = false;
            if (mc.thePlayer.isBlocking() || (killAura.getState() && killAura.getTarget() != null && !killAura.getAutoBlockModeValue().get().equals("None") || tpAura.getState() && tpAura.isBlocking() || killAuraRecode.getState() && killAuraRecode.isBlocking())) {
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
        lastTpX = (int) event.getPosX();
        lastTpY = (int) event.getPosY();
        lastTpZ = (int) event.getPosZ();

        if (RotationUtils.targetRotation != null) {
            RotationUtils.targetRotation.setYaw(event.getYaw());
            RotationUtils.targetRotation.setPitch(event.getPitch());
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        final Packet<?> packet = event.getPacket();

        if (packet.toString().startsWith("net.minecraft.network.play.client.C"))
            preSend++;
        if (packet.toString().startsWith("net.minecraft.network.play.server.S"))
            preReceive++;

        if (packetCountTimer.hasTimePassed(1000L)) {
            sendPacketCounts = preSend;
            receivePacketCounts = preReceive;
            preSend = 0;
            preReceive = 0;
            packetCountTimer.reset();
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
                entity.isInvisible() ||
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