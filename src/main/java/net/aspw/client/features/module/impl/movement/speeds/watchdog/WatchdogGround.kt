package net.aspw.client.features.module.impl.movement.speeds.watchdog

import net.aspw.client.Launch
import net.aspw.client.event.EventState
import net.aspw.client.event.JumpEvent
import net.aspw.client.event.MotionEvent
import net.aspw.client.event.MoveEvent
import net.aspw.client.features.module.impl.movement.Speed
import net.aspw.client.features.module.impl.movement.speeds.SpeedMode
import net.aspw.client.utils.MovementUtils
import net.minecraft.potion.Potion
import net.minecraft.stats.StatList

class WatchdogGround : SpeedMode("WatchdogGround") {

    override fun onJump(event: JumpEvent) {
        if (mc.thePlayer != null && MovementUtils.isMoving())
            event.cancelEvent()
    }

    override fun onMotion(eventMotion: MotionEvent) {
        val speed = Launch.moduleManager.getModule(
            Speed::class.java
        )
        if (speed == null || eventMotion.eventState !== EventState.PRE || mc.thePlayer.isInWater) return
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.41999998688698
                if (mc.thePlayer.isPotionActive(Potion.jump))
                    mc.thePlayer.motionY += ((mc.thePlayer.getActivePotionEffect(Potion.jump).amplifier + 1).toFloat() * 0.1f).toDouble()
                mc.thePlayer.isAirBorne = true
                mc.thePlayer.triggerAchievement(StatList.jumpStat)
                val baseSpeed = 0.48f
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
                    MovementUtils.strafe(baseSpeed + ((mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1).toFloat() * 0.12f))
                else MovementUtils.strafe(baseSpeed)
            }
        }
    }

    override fun onEnable() {
        Launch.moduleManager.getModule(
            Speed::class.java
        ) ?: return
    }

    override fun onUpdate() {}
    override fun onMotion() {}
    override fun onMove(event: MoveEvent) {}
}