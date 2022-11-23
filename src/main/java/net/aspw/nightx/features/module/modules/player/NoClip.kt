package net.aspw.nightx.features.module.modules.player

import net.aspw.nightx.event.EventTarget
import net.aspw.nightx.event.UpdateEvent
import net.aspw.nightx.features.module.Module
import net.aspw.nightx.features.module.ModuleCategory
import net.aspw.nightx.features.module.ModuleInfo

@ModuleInfo(name = "NoClip", spacedName = "No Clip", category = ModuleCategory.PLAYER)
class NoClip : Module() {

    override fun onDisable() {
        mc.thePlayer?.noClip = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.noClip = true
        mc.thePlayer.fallDistance = 0f
        mc.thePlayer.onGround = false

        mc.thePlayer.capabilities.isFlying = false
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0

        val speed = 0.6f
        mc.thePlayer.jumpMovementFactor = speed
        if (mc.gameSettings.keyBindJump.isKeyDown)
            mc.thePlayer.motionY += speed.toDouble()
        if (mc.gameSettings.keyBindSneak.isKeyDown)
            mc.thePlayer.motionY -= speed.toDouble()
    }
}