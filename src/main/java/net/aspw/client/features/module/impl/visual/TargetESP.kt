package net.aspw.client.features.module.impl.visual

import net.aspw.client.event.AttackEvent
import net.aspw.client.event.EventTarget
import net.aspw.client.event.Render3DEvent
import net.aspw.client.features.module.Module
import net.aspw.client.features.module.ModuleCategory
import net.aspw.client.features.module.ModuleInfo
import net.aspw.client.utils.EntityUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "TargetESP", spacedName = "Target ESP", category = ModuleCategory.VISUAL)
class TargetESP : Module() {

    var targets = mutableListOf<Entity?>()

    override fun onDisable() {
        targets.clear()
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity !in targets && EntityUtils.isSelected(event.targetEntity, true))
            targets.add(event.targetEntity)
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (targets.isEmpty()) return

        for (target in targets) {
            if (target?.isDead!!)
                targets.remove(target)

            if (6 >= mc.thePlayer.getDistanceToEntity(target)) {
                GL11.glPushMatrix()
                GL11.glTranslated(
                    target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
                    target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY,
                    target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
                )
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glRotatef(90F, 1F, 0F, 0F)

                GL11.glLineWidth(3 + 1.25F)
                GL11.glColor3f(0F, 0F, 0F)
                GL11.glBegin(GL11.GL_LINE_LOOP)

                for (i in 0..360 step 60 - 24) {
                    GL11.glVertex2f(
                        cos(i * Math.PI / 180.0).toFloat() * 0.8f,
                        (sin(i * Math.PI / 180.0).toFloat() * 0.8f)
                    )
                }

                GL11.glEnd()

                GL11.glLineWidth(3f)
                GL11.glBegin(GL11.GL_LINE_LOOP)

                for (i in 0..360 step 60 - 24) {
                    GL11.glColor3f(255 / 255.0f, 140 / 255.0f, 255 / 255.0f)
                    GL11.glVertex2f(
                        cos(i * Math.PI / 180.0).toFloat() * 0.8f,
                        (sin(i * Math.PI / 180.0).toFloat() * 0.8f)
                    )
                }

                GL11.glEnd()

                GL11.glDisable(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_TEXTURE_2D)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_LINE_SMOOTH)

                GL11.glPopMatrix()

                GlStateManager.resetColor()
                GL11.glColor4f(1F, 1F, 1F, 1F)
            } else targets.remove(target)
        }
    }
}