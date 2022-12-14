package net.aspw.nightx.features.module.modules.render

import net.aspw.nightx.event.AttackEvent
import net.aspw.nightx.event.EventTarget
import net.aspw.nightx.features.module.Module
import net.aspw.nightx.features.module.ModuleCategory
import net.aspw.nightx.features.module.ModuleInfo
import net.aspw.nightx.utils.EntityUtils
import net.aspw.nightx.utils.misc.RandomUtils
import net.aspw.nightx.value.BoolValue
import net.aspw.nightx.value.IntegerValue
import net.aspw.nightx.value.ListValue
import net.minecraft.block.Block
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "HitParticles", spacedName = "Hit Particles", category = ModuleCategory.RENDER, array = false)
class HitParticles : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Thunder", "Blood", "Fire", "Criticals", "Sharpness"), "Thunder")
    private val timesValue = IntegerValue("Times", 1, 1, 10)
    private val soundValue = BoolValue("Sound", false, { modeValue.get().equals("thunder", true) })
    private val blockState = Block.getStateId(Blocks.redstone_block.defaultState)

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (EntityUtils.isSelected(event.targetEntity, true)) {
            displayEffectFor(event.targetEntity as EntityLivingBase)
        }
    }

    private fun displayEffectFor(entity: EntityLivingBase) {
        repeat(timesValue.get()) {
            when (modeValue.get().lowercase()) {
                "thunder" -> {
                    mc.netHandler.handleSpawnGlobalEntity(
                        S2CPacketSpawnGlobalEntity(
                            EntityLightningBolt(
                                mc.theWorld,
                                entity.posX,
                                entity.posY,
                                entity.posZ
                            )
                        )
                    )
                    if (soundValue.get()) {
                        mc.soundHandler.playSound(
                            PositionedSoundRecord.create(
                                ResourceLocation("random.explode"),
                                1.0f
                            )
                        )
                        mc.soundHandler.playSound(
                            PositionedSoundRecord.create(
                                ResourceLocation("ambient.weather.thunder"),
                                1.0f
                            )
                        )
                    }
                }

                "blood" -> {
                    repeat(10) {
                        mc.effectRenderer.spawnEffectParticle(
                            EnumParticleTypes.BLOCK_CRACK.particleID,
                            entity.posX,
                            entity.posY + entity.height / 2,
                            entity.posZ,
                            entity.motionX + RandomUtils.nextFloat(-0.5f, 0.5f),
                            entity.motionY + RandomUtils.nextFloat(-0.5f, 0.5f),
                            entity.motionZ + RandomUtils.nextFloat(-0.5f, 0.5f),
                            blockState
                        )
                    }
                }

                "fire" ->
                    mc.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.LAVA)

                "criticals" -> mc.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.CRIT)
                "sharpness" -> mc.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.CRIT_MAGIC)
            }
        }
    }
}