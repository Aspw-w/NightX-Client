package net.aspw.client.utils.block

import net.aspw.client.utils.MinecraftInstance
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockButton
import net.minecraft.block.BlockLever
import net.minecraft.block.BlockPressurePlate
import net.minecraft.block.BlockSign
import net.minecraft.block.BlockWeb
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.floor


object BlockUtils : MinecraftInstance() {

    /**
     * Get player is in block
     */
    @JvmStatic
    fun isInsideBlock(): Boolean {
        return isInsideBlock(mc.thePlayer.entityBoundingBox)
    }

    /**
     * Get aABB is in block
     */
    @JvmStatic
    fun isInsideBlock(aABB: AxisAlignedBB): Boolean {
        return collideBlockIntersects(aABB) { block -> !(block is BlockAir || block is BlockWeb || block is BlockSign || block is BlockButton || block is BlockPressurePlate || block is BlockLever || !block!!.isCollidable) }
    }

    /**
     * Get block from [blockPos]
     */
    @JvmStatic
    fun getBlock(blockPos: BlockPos?): Block? = mc.theWorld?.getBlockState(blockPos)?.block

    /**
     * Get material from [blockPos]
     */
    @JvmStatic
    fun getMaterial(blockPos: BlockPos?): Material? = getBlock(blockPos)?.material

    /**
     * Check [blockPos] is replaceable
     */
    @JvmStatic
    fun isReplaceable(blockPos: BlockPos?) = getMaterial(blockPos)?.isReplaceable ?: false

    /**
     * Get state from [blockPos]
     */
    @JvmStatic
    fun getState(blockPos: BlockPos?): IBlockState = mc.theWorld.getBlockState(blockPos)

    /**
     * Check if [blockPos] is clickable
     */
    @JvmStatic
    fun canBeClicked(blockPos: BlockPos?) = getBlock(blockPos)?.canCollideCheck(getState(blockPos), false) ?: false &&
            mc.theWorld.worldBorder.contains(blockPos)

    /**
     * Get block name by [id]
     */
    @JvmStatic
    fun getBlockName(id: Int): String = Block.getBlockById(id).localizedName

    /**
     * Check if block is full block
     */
    @JvmStatic
    fun isFullBlock(blockPos: BlockPos?): Boolean {
        val axisAlignedBB = getBlock(blockPos)?.getCollisionBoundingBox(mc.theWorld, blockPos, getState(blockPos))
            ?: return false
        return axisAlignedBB.maxX - axisAlignedBB.minX == 1.0 && axisAlignedBB.maxY - axisAlignedBB.minY == 1.0 && axisAlignedBB.maxZ - axisAlignedBB.minZ == 1.0
    }

    /**
     * Get distance to center of [blockPos]
     */
    @JvmStatic
    fun getCenterDistance(blockPos: BlockPos) =
        mc.thePlayer.getDistance(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5)

    /**
     * Search blocks around the player in a specific [radius]
     */
    @JvmStatic
    fun searchBlocks(radius: Int): Map<BlockPos, Block?> {
        val blocks: MutableMap<BlockPos, Block?> = HashMap()
        for (x in radius downTo -radius) {
            for (y in radius downTo -radius) {
                for (z in radius downTo -radius) {
                    val blockPos = BlockPos(
                        mc.thePlayer.posX.toInt() + x,
                        mc.thePlayer.posY.toInt() + y,
                        mc.thePlayer.posZ.toInt() + z
                    )
                    if (getCenterDistance(blockPos) <= radius) blocks[blockPos] = getBlock(blockPos)
                }
            }
        }

        val sortedBlocks: MutableMap<BlockPos, Block?> = LinkedHashMap()
        blocks.entries.stream().sorted(
            Comparator.comparingDouble<Map.Entry<BlockPos, Block?>> { obj: Map.Entry<BlockPos, Block?> ->
                getCenterDistance(obj.key)
            }
        ).forEach { entry: Map.Entry<BlockPos, Block?> ->
            sortedBlocks[entry.key] = entry.value
        }
        return sortedBlocks
    }

    /**
     * Check if [axisAlignedBB] has collidable blocks using custom [collide] check
     */
    @JvmStatic
    fun collideBlock(axisAlignedBB: AxisAlignedBB, collide: (Block?) -> Boolean): Boolean {
        for (x in MathHelper.floor_double(mc.thePlayer.entityBoundingBox.minX) until
                MathHelper.floor_double(mc.thePlayer.entityBoundingBox.maxX) + 1) {
            for (z in MathHelper.floor_double(mc.thePlayer.entityBoundingBox.minZ) until
                    MathHelper.floor_double(mc.thePlayer.entityBoundingBox.maxZ) + 1) {
                val block = getBlock(BlockPos(x.toDouble(), axisAlignedBB.minY, z.toDouble()))

                if (!collide(block))
                    return false
            }
        }

        return true
    }

    /**
     * Check if [axisAlignedBB] has collidable blocks using custom [collide] check
     */
    @JvmStatic
    fun collideBlockIntersects(axisAlignedBB: AxisAlignedBB, collide: (Block?) -> Boolean): Boolean {
        return mc.theWorld.getCollisionBoxes(axisAlignedBB).stream().anyMatch { cBB: AxisAlignedBB ->
            collide(
                getBlock(BlockPos(cBB.minX, cBB.minY, cBB.minZ))
            )
        }
    }

    @JvmStatic
    fun floorVec3(vec3: Vec3) = Vec3(floor(vec3.xCoord), floor(vec3.yCoord), floor(vec3.zCoord))
}