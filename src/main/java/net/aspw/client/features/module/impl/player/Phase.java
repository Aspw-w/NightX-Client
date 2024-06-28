package net.aspw.client.features.module.impl.player;

import net.aspw.client.event.*;
import net.aspw.client.features.module.Module;
import net.aspw.client.features.module.ModuleCategory;
import net.aspw.client.features.module.ModuleInfo;
import net.aspw.client.utils.MovementUtils;
import net.aspw.client.utils.block.BlockUtils;
import net.aspw.client.utils.timer.TickTimer;
import net.aspw.client.value.ListValue;
import net.minecraft.block.BlockAir;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

/**
 * The type Phase.
 */
@ModuleInfo(name = "Phase", category = ModuleCategory.PLAYER)
public class Phase extends Module {

    /**
     * The Mode value.
     */
    public final ListValue modeValue = new ListValue("Mode", new String[]{
            "FullBlock",
            "Vanilla",
            "Skip",
            "Spartan",
            "Clip",
            "AAC3.5.0",
            "AACv4",
            "Vulcan",
            "Packetless",
            "Redesky",
            "SmartVClip"
    }, "FullBlock");

    private final TickTimer tickTimer = new TickTimer();
    private final TickTimer mineplexTickTimer = new TickTimer();
    private boolean mineplexClip, noRot;
    private int stage;
    private boolean shouldContinue;
    private int clipState;
    private double yaw;
    private double value;

    @Override
    public void onEnable() {
        stage = 0;
        if (modeValue.get().equalsIgnoreCase("aacv4"))
            mc.timer.timerSpeed = 0.1F;
    }

    @Override
    public void onDisable() {
        if (modeValue.get().equalsIgnoreCase("aacv4"))
            mc.timer.timerSpeed = 1F;
        shouldContinue = false;
        clipState = 0;
        value = 0;
    }

    /**
     * On update.
     *
     * @param event the event
     */
    @EventTarget
    public void onUpdate(final UpdateEvent event) {
        if (modeValue.get().equals("FullBlock")) return;
        if (modeValue.get().equalsIgnoreCase("aacv4")) {
            switch (stage) {
                case 1: {
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY - 0.00000001, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                    break;
                }
                case 3: {
                    this.setState(false);
                    break;
                }
            }
            stage++;
            return;
        } else if (modeValue.get().equalsIgnoreCase("redesky")) {
            switch (stage) {
                case 0: {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.00000001, mc.thePlayer.posZ);
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY - 0.00000001, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                    break;
                }
                case 1: {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                    break;
                }
                case 3: {
                    this.setState(false);
                }
            }
            stage++;
            return;
        }

        final boolean isInsideBlock = BlockUtils.collideBlockIntersects2(mc.thePlayer.getEntityBoundingBox(), block -> !(block instanceof BlockAir));

        if (isInsideBlock && !modeValue.get().equalsIgnoreCase("Packetless") && !modeValue.get().equalsIgnoreCase("SmartVClip")) {
            mc.thePlayer.noClip = true;
            mc.thePlayer.motionY = 0D;
            mc.thePlayer.onGround = true;
        }

        final NetHandlerPlayClient netHandlerPlayClient = mc.getNetHandler();

        switch (modeValue.get().toLowerCase()) {
            case "vanilla": {
                if (!mc.thePlayer.onGround || !tickTimer.hasTimePassed(2) || !mc.thePlayer.isCollidedHorizontally || !(!isInsideBlock || mc.thePlayer.isSneaking()))
                    break;

                netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(0.5D, 0, 0.5D, true));
                netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.2D, mc.thePlayer.posZ, true));
                netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(0.5D, 0, 0.5D, true));
                netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 0.5D, mc.thePlayer.posY, mc.thePlayer.posZ + 0.5D, true));
                final double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                final double x = -Math.sin(yaw) * 0.04D;
                final double z = Math.cos(yaw) * 0.04D;
                mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
                tickTimer.reset();
                break;
            }
            case "skip": {
                if (!mc.thePlayer.onGround || !tickTimer.hasTimePassed(2) || !mc.thePlayer.isCollidedHorizontally || !(!isInsideBlock || mc.thePlayer.isSneaking()))
                    break;

                final double direction = MovementUtils.getDirection();
                final double posX = -Math.sin(direction) * 0.3;
                final double posZ = Math.cos(direction) * 0.3;

                for (int i = 0; i < 3; ++i) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.06, mc.thePlayer.posZ, true));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + posX * i, mc.thePlayer.posY, mc.thePlayer.posZ + posZ * i, true));
                }

                mc.thePlayer.setEntityBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(posX, 0.0D, posZ));
                mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX + posX, mc.thePlayer.posY, mc.thePlayer.posZ + posZ);
                tickTimer.reset();
                break;
            }
            case "spartan": {
                if (!mc.thePlayer.onGround || !tickTimer.hasTimePassed(2) || !mc.thePlayer.isCollidedHorizontally || !(!isInsideBlock || mc.thePlayer.isSneaking()))
                    break;

                netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(0.5D, 0, 0.5D, true));
                netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.2D, mc.thePlayer.posZ, true));
                netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(0.5D, 0, 0.5D, true));
                netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 0.5D, mc.thePlayer.posY, mc.thePlayer.posZ + 0.5D, true));
                final double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                final double x = -Math.sin(yaw) * 0.04D;
                final double z = Math.cos(yaw) * 0.04D;
                mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
                tickTimer.reset();
                break;
            }
            case "clip": {
                if (!tickTimer.hasTimePassed(2) || !mc.thePlayer.isCollidedHorizontally || !(!isInsideBlock || mc.thePlayer.isSneaking()))
                    break;

                final double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                final double oldX = mc.thePlayer.posX;
                final double oldZ = mc.thePlayer.posZ;

                for (int i = 1; i <= 10; i++) {
                    final double x = -Math.sin(yaw) * i;
                    final double z = Math.cos(yaw) * i;

                    if (BlockUtils.getBlock(new BlockPos(oldX + x, mc.thePlayer.posY, oldZ + z)) instanceof BlockAir && BlockUtils.getBlock(new BlockPos(oldX + x, mc.thePlayer.posY + 1, oldZ + z)) instanceof BlockAir) {
                        mc.thePlayer.setPosition(oldX + x, mc.thePlayer.posY, oldZ + z);
                        break;
                    }
                }
                tickTimer.reset();
                break;
            }
            case "aac3.5.0": {
                if (!tickTimer.hasTimePassed(2) || !mc.thePlayer.isCollidedHorizontally || !(!isInsideBlock || mc.thePlayer.isSneaking()))
                    break;

                final double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                final double oldX = mc.thePlayer.posX;
                final double oldZ = mc.thePlayer.posZ;
                final double x = -Math.sin(yaw);
                final double z = Math.cos(yaw);

                mc.thePlayer.setPosition(oldX + x, mc.thePlayer.posY, oldZ + z);
                tickTimer.reset();
                break;
            }
            case "vulcan": {
                if (!tickTimer.hasTimePassed(2) || !mc.thePlayer.isCollidedHorizontally || isInsideBlock)
                    break;

                final double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                final double oldX = mc.thePlayer.posX;
                final double oldZ = mc.thePlayer.posZ;
                final double x = -Math.sin(yaw);
                final double z = Math.cos(yaw);

                mc.thePlayer.setPosition(oldX + x, mc.thePlayer.posY, oldZ + z);
                mc.thePlayer.noClip = true;
                tickTimer.reset();
                break;
            }
            case "smartvclip": {
                boolean cageCollision = (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).up(3)).getBlock() != Blocks.air
                        && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down()).getBlock() != Blocks.air);
                noRot = (mc.thePlayer.ticksExisted >= 0 && mc.thePlayer.ticksExisted <= 40 && cageCollision);
                if (mc.thePlayer.ticksExisted >= 20 && mc.thePlayer.ticksExisted < 40 && cageCollision) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 4, mc.thePlayer.posZ, false));
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 4, mc.thePlayer.posZ);
                }
                break;
            }
            case "redesky":
            case "redesky2": {
                this.setState(false);
                break;
            }
        }

        tickTimer.update();
    }

    /**
     * On block bb.
     *
     * @param event the event
     */
    @EventTarget
    public void onBlockBB(final BlockBBEvent event) {
        if (modeValue.get().equals("FullBlock")) return;

        final AxisAlignedBB aABB = event.getBoundingBox();
        if (mc.thePlayer != null && aABB != null && aABB.maxY > mc.thePlayer.getEntityBoundingBox().minY && !modeValue.get().equalsIgnoreCase("Packetless") && !modeValue.get().equalsIgnoreCase("SmartVClip") && BlockUtils.collideBlockIntersects2(mc.thePlayer.getEntityBoundingBox(), aABB, block -> !(block instanceof BlockAir))) {
            event.setBoundingBox(new AxisAlignedBB(aABB.maxX, mc.thePlayer.getEntityBoundingBox().minY, aABB.maxZ, aABB.minX, aABB.minY, aABB.minZ));
        }
    }

    /**
     * On packet.
     *
     * @param event the event
     */
    @EventTarget
    public void onPacket(final PacketEvent event) {
        if (modeValue.get().equals("FullBlock")) return;
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C03PacketPlayer) {
            final C03PacketPlayer packetPlayer = (C03PacketPlayer) packet;

            if (modeValue.get().equalsIgnoreCase("AAC3.5.0")) {
                final float yaw = (float) MovementUtils.getDirection();

                packetPlayer.x = packetPlayer.x - MathHelper.sin(yaw) * 0.00000001D;
                packetPlayer.z = packetPlayer.z + MathHelper.cos(yaw) * 0.00000001D;
            }

            if (modeValue.get().equalsIgnoreCase("Vulcan")) {
                final float yaw = (float) MovementUtils.getDirection();

                packetPlayer.x = packetPlayer.x - MathHelper.sin(yaw) * 0.00000008D;
                packetPlayer.z = packetPlayer.z + MathHelper.cos(yaw) * 0.00000008D;
            }

            if (modeValue.get().equalsIgnoreCase("SmartVClip") && noRot && packetPlayer.rotating)
                event.cancelEvent();
        }

        if (packet instanceof C0BPacketEntityAction && modeValue.get().equalsIgnoreCase("SmartVClip") && noRot)
            event.cancelEvent();
    }

    @EventTarget
    private void onMove(final MoveEvent event) {
        if (modeValue.get().equalsIgnoreCase("packetless")) {
            if (mc.thePlayer.isCollidedHorizontally)
                mineplexClip = true;
            if (!mineplexClip)
                return;

            mineplexTickTimer.update();

            event.setX(0);
            event.setZ(0);

            if (mineplexTickTimer.hasTimePassed(3)) {
                mineplexTickTimer.reset();
                mineplexClip = false;
            } else if (mineplexTickTimer.hasTimePassed(1)) {
                final double offset = mineplexTickTimer.hasTimePassed(2) ? 1.6D : 0.06D;
                final double direction = MovementUtils.getDirection();

                mc.thePlayer.setPosition(mc.thePlayer.posX + (-Math.sin(direction) * offset), mc.thePlayer.posY, mc.thePlayer.posZ + (Math.cos(direction) * offset));
            }
        }
        if (modeValue.get().equals("FullBlock")) {
            if (mc.thePlayer.isCollidedHorizontally) {
                clipState++;
            }
            switch (clipState) {
                case 1:
                    double i = 0;
                    double moveYaw = MovementUtils.getDirection();
                    while ((i += 0.025) <= 2) {
                        if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(-(Math.sin(moveYaw) * i), 0, (Math.cos(moveYaw) * i)).expand(0, 0, 0)).isEmpty() && (mc.thePlayer.movementInput.moveForward != 0 || mc.thePlayer.movementInput.moveStrafe != 0)) {
                            if (i <= 0.06) {
                                mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX - (Math.sin(moveYaw) * 0.06), mc.thePlayer.posY, mc.thePlayer.posZ + (Math.cos(moveYaw) * 0.06));
                                shouldContinue = false;
                                event.setX(0);
                                event.setZ(0);
                                break;
                            } else {
                                mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX - (Math.sin(moveYaw) * 0.06), mc.thePlayer.posY, mc.thePlayer.posZ + (Math.cos(moveYaw) * 0.06));
                                value = i;
                                yaw = moveYaw;
                                shouldContinue = true;
                                event.setX(0);
                                event.setZ(0);
                                break;
                            }
                        } else {
                            shouldContinue = false;
                        }
                    }
                    clipState++;
                    if (!shouldContinue) {
                        clipState = 0;
                    }
                    break;
                case 2:
                    if (mc.thePlayer.movementInput.moveForward != 0 || mc.thePlayer.movementInput.moveStrafe != 0) {
                        mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX - (Math.sin(yaw) * value), mc.thePlayer.posY, mc.thePlayer.posZ + (Math.cos(yaw) * value));
                    } else {
                        mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX - (Math.sin(yaw) * -0.06), mc.thePlayer.posY, mc.thePlayer.posZ + (Math.cos(yaw) * -0.06));
                    }
                    clipState = 0;
                    break;
            }
        }
        if (modeValue.get().equalsIgnoreCase("SmartVClip") && noRot)
            event.zeroXZ();
    }

    /**
     * On push out.
     *
     * @param event the event
     */
    @EventTarget
    public void onPushOut(PushOutEvent event) {
        event.cancelEvent();
    }

    /**
     * On jump.
     *
     * @param event the event
     */
    @EventTarget
    public void onJump(JumpEvent event) {
        if (modeValue.get().equalsIgnoreCase("SmartVClip") && noRot) event.cancelEvent();
    }

    @Override
    public String getTag() {
        return modeValue.get();
    }
}
