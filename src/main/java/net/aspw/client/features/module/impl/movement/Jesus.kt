package net.aspw.client.features.module.impl.movement

import net.aspw.client.event.BlockBBEvent
import net.aspw.client.event.EventTarget
import net.aspw.client.event.MoveEvent
import net.aspw.client.event.PacketEvent
import net.aspw.client.event.UpdateEvent
import net.aspw.client.features.module.Module
import net.aspw.client.features.module.ModuleCategory
import net.aspw.client.features.module.ModuleInfo
import net.aspw.client.utils.MovementUtils
import net.aspw.client.utils.block.BlockUtils.collideBlock
import net.aspw.client.utils.block.BlockUtils.getBlock
import net.aspw.client.value.FloatValue
import net.aspw.client.value.ListValue
import net.minecraft.block.Block
import net.minecraft.block.BlockLiquid
import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import java.util.Locale

@ModuleInfo(name = "Jesus", spacedName = "Jesus", category = ModuleCategory.MOVEMENT)
class Jesus : Module() {
    @JvmField
    val modeValue = ListValue(
        "Mode",
        arrayOf(
            "Vanilla",
            "NCP",
            "Bounce",
            "AAC",
            "AACFly",
            "AAC3.3.11",
            "AAC4.2.1",
            "Twillight",
            "MatrixFast",
            "MatrixDolphin",
            "Dolphin",
            "Swim",
            "Vulcan",
            "Verus"
        ),
        "NCP"
    )
    private val aacFlyValue = FloatValue("Motion", 0.5f, 0.1f, 1f) { modeValue.get() == "AACFly" }
    private var nextTick = false

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (mc.thePlayer == null || mc.thePlayer.isSneaking) return
        when (modeValue.get().lowercase(Locale.getDefault())) {
            "ncp", "vanilla" -> if (collideBlock(mc.thePlayer.entityBoundingBox) { block: Block? -> block is BlockLiquid } && mc.thePlayer.isInsideOfMaterial(
                    Material.air
                ) && !mc.thePlayer.isSneaking) mc.thePlayer.motionY = 0.08

            "bounce" -> {
                if (mc.thePlayer.isInWater)
                    mc.thePlayer.motionY += 0.7
            }

            "aac" -> {
                val blockPos = mc.thePlayer.position.down()
                if (!mc.thePlayer.onGround && getBlock(blockPos) === Blocks.water || mc.thePlayer.isInWater) {
                    if (!mc.thePlayer.isSprinting) {
                        mc.thePlayer.motionX *= 0.99999
                        mc.thePlayer.motionY *= 0.0
                        mc.thePlayer.motionZ *= 0.99999
                        if (mc.thePlayer.isCollidedHorizontally) mc.thePlayer.motionY =
                            ((mc.thePlayer.posY - (mc.thePlayer.posY - 1).toInt()).toInt() / 8f).toDouble()
                    } else {
                        mc.thePlayer.motionX *= 0.99999
                        mc.thePlayer.motionY *= 0.0
                        mc.thePlayer.motionZ *= 0.99999
                        if (mc.thePlayer.isCollidedHorizontally) mc.thePlayer.motionY =
                            ((mc.thePlayer.posY - (mc.thePlayer.posY - 1).toInt()).toInt() / 8f).toDouble()
                    }
                    if (mc.thePlayer.fallDistance >= 4) mc.thePlayer.motionY =
                        -0.004 else if (mc.thePlayer.isInWater) mc.thePlayer.motionY = 0.09
                }
                if (mc.thePlayer.hurtTime != 0) mc.thePlayer.onGround = false
            }

            "matrixfast" -> if (mc.thePlayer.isInWater) {
                mc.thePlayer.motionY = 0.0
                MovementUtils.strafe(0.6f)
            }

            "verus" -> if (mc.thePlayer.isInWater) {
                mc.thePlayer.motionY = 0.0
                MovementUtils.strafe(0.32f)
            }

            "vulcan" -> if (mc.thePlayer.isInWater) {
                mc.thePlayer.motionY = 0.015 *
                        if (!MovementUtils.isMoving()) 0
                        else if (mc.thePlayer.ticksExisted % 2 == 0) 1
                        else -1
                MovementUtils.strafe(0.145f)
            }

            "matrixdolphin" -> if (mc.thePlayer.isInWater) {
                mc.thePlayer.motionY = 0.6
            }

            "aac3.3.11" -> if (mc.thePlayer.isInWater) {
                mc.thePlayer.motionX *= 1.17
                mc.thePlayer.motionZ *= 1.17
                if (mc.thePlayer.isCollidedHorizontally) mc.thePlayer.motionY = 0.24 else if (mc.theWorld.getBlockState(
                        BlockPos(
                            mc.thePlayer.posX, mc.thePlayer.posY + 1.0, mc.thePlayer.posZ
                        )
                    ).block !== Blocks.air
                ) mc.thePlayer.motionY += 0.04
            }

            "dolphin" -> if (mc.thePlayer.isInWater) mc.thePlayer.motionY += 0.03999999910593033
            "aac4.2.1" -> {
                //i didn't fix if player collided wall(trigger aac flag)
                if (!mc.thePlayer.onGround && getBlock(mc.thePlayer.position.down()) === Blocks.water || mc.thePlayer.isInWater) {
                    mc.thePlayer.motionY *= 0.0
                    //decrease value due to compatible 4 direction. forward is 0.089F
                    mc.thePlayer.jumpMovementFactor = 0.08f
                    if (mc.thePlayer.fallDistance > 0) return else if (mc.thePlayer.isInWater) mc.gameSettings.keyBindJump.pressed =
                        true
                }
            }

            "twillight" -> if (mc.thePlayer.isInWater) {
                mc.thePlayer.motionX *= 1.04
                mc.thePlayer.motionZ *= 1.04
                MovementUtils.strafe()
            }
        }
    }

    override fun onDisable() {
        if (mc.thePlayer.isInWater) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if ("aacfly" == modeValue.get().lowercase(Locale.getDefault()) && mc.thePlayer.isInWater) {
            event.y = aacFlyValue.get().toDouble()
            mc.thePlayer.motionY = aacFlyValue.get().toDouble()
        }
        if ("twillight".equals(modeValue.get(), ignoreCase = true) && mc.thePlayer.isInWater) {
            event.y = 0.01
            mc.thePlayer.motionY = 0.01
        }
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        if (mc.thePlayer == null || mc.thePlayer.entityBoundingBox == null) return
        if (event.block is BlockLiquid && !collideBlock(mc.thePlayer.entityBoundingBox) { block: Block? -> block is BlockLiquid } && !mc.thePlayer.isSneaking) {
            when (modeValue.get().lowercase(Locale.getDefault())) {
                "ncp", "vanilla" -> event.boundingBox = AxisAlignedBB.fromBounds(
                    event.x.toDouble(),
                    event.y.toDouble(),
                    event.z.toDouble(),
                    (event.x + 1).toDouble(),
                    (event.y + 1).toDouble(),
                    (event.z + 1).toDouble()
                )
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null || !modeValue.get().equals("NCP", ignoreCase = true)) return
        if (event.packet is C03PacketPlayer) {
            if (collideBlock(
                    AxisAlignedBB(
                        mc.thePlayer.entityBoundingBox.maxX,
                        mc.thePlayer.entityBoundingBox.maxY,
                        mc.thePlayer.entityBoundingBox.maxZ,
                        mc.thePlayer.entityBoundingBox.minX,
                        mc.thePlayer.entityBoundingBox.minY - 0.01,
                        mc.thePlayer.entityBoundingBox.minZ
                    )
                ) { block: Block? -> block is BlockLiquid }
            ) {
                nextTick = !nextTick
                if (nextTick) event.packet.y -= 0.001
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}