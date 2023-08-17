package net.aspw.client.injection.forge.mixins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * The type Mixin gui key binding list.
 */
@Mixin(GuiKeyBindingList.class)
public abstract class MixinGuiKeyBindingList extends GuiSlot {

    /**
     * Instantiates a new Mixin gui key binding list.
     *
     * @param mcIn         the mc in
     * @param width        the width
     * @param height       the height
     * @param topIn        the top in
     * @param bottomIn     the bottom in
     * @param slotHeightIn the slot height in
     */
    public MixinGuiKeyBindingList(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn) {
        super(mcIn, width, height, topIn, bottomIn, slotHeightIn);
    }

    /**
     * @author As_pw
     * @reason ScrollBar
     */
    @Overwrite
    protected int getScrollBarX() {
        return this.width - 5;
    }
}