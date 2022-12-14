package net.aspw.nightx.features.module.modules.world

import net.aspw.nightx.event.*
import net.aspw.nightx.features.module.Module
import net.aspw.nightx.features.module.ModuleCategory
import net.aspw.nightx.features.module.ModuleInfo
import net.aspw.nightx.utils.PacketUtils
import net.aspw.nightx.utils.RotationUtils
import net.aspw.nightx.utils.block.BlockUtils
import net.aspw.nightx.utils.render.RenderUtils
import net.aspw.nightx.utils.timer.TickTimer
import net.aspw.nightx.value.BoolValue
import net.aspw.nightx.value.FloatValue
import net.aspw.nightx.value.IntegerValue
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.awt.Color

@ModuleInfo(name = "CivBreak", spacedName = "Civ Break", category = ModuleCategory.WORLD)
class CivBreak : Module() {

    private var blockPos: BlockPos? = null
    private var enumFacing: EnumFacing? = null
    private val tickTimer = TickTimer()

    private val range = FloatValue("Range", 5F, 1F, 6F)
    private val speedValue = IntegerValue("Break-Speed", 0, 0, 20)
    private val rotationsValue = BoolValue("Rotations", true)
    private val visualSwingValue = BoolValue("VisualSwing", false)

    private val airResetValue = BoolValue("Air-Reset", false)
    private val rangeResetValue = BoolValue("Range-Reset", false)

    private val R = IntegerValue("R", 255, 0, 255)
    private val G = IntegerValue("G", 255, 0, 255)
    private val B = IntegerValue("B", 255, 0, 255)
    private val outLine = BoolValue("Outline", true)

    @EventTarget
    fun onBlockClick(event: ClickBlockEvent) {
        blockPos = event.clickedBlock
        enumFacing = event.enumFacing
    }

    override fun onDisable() {
        blockPos ?: return
        blockPos = null
    }

    @EventTarget
    fun onUpdate(event: MotionEvent) {
        val pos = blockPos ?: return

        if (airResetValue.get() && BlockUtils.getBlock(pos) is BlockAir ||
            rangeResetValue.get() && BlockUtils.getCenterDistance(pos) > range.get()
        ) {
            blockPos = null
            return
        }

        if (BlockUtils.getBlock(pos) is BlockAir || BlockUtils.getCenterDistance(pos) > range.get()) {
            return
        }

        if (blockPos !== null) {
            event.onGround = true
        }

        when (event.eventState) {
            EventState.PRE -> if (rotationsValue.get()) {
                RotationUtils.setTargetRotation((RotationUtils.faceBlock(pos) ?: return).rotation)
            }

            EventState.POST -> {
                if (visualSwingValue.get()) {
                    mc.thePlayer.swingItem()
                } else {
                    mc.netHandler.addToSendQueue(C0APacketAnimation())
                }

                // Break
                if (tickTimer.hasTimePassed(speedValue.get()))
                    PacketUtils.sendPacketNoEvent(
                        C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                            blockPos,
                            enumFacing
                        )
                    )
                PacketUtils.sendPacketNoEvent(
                    C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        blockPos,
                        enumFacing
                    )
                )
            }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        RenderUtils.drawBlockBox(blockPos ?: return, Color(R.get(), G.get(), B.get()), outLine.get())
    }
}
