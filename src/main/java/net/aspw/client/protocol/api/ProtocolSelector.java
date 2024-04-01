package net.aspw.client.protocol.api;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.aspw.client.protocol.ProtocolBase;
import net.aspw.client.visual.font.smooth.FontLoaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.raphimc.vialoader.util.ProtocolVersionList;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class ProtocolSelector extends GuiScreen {

    private final GuiScreen parent;
    private final boolean simple;
    private final FinishedCallback finishedCallback;

    private SlotList list;

    public ProtocolSelector(final GuiScreen parent) {
        this(parent, false, (version, unused) -> ProtocolBase.getManager().setTargetVersion(version));
    }

    public ProtocolSelector(final GuiScreen parent, final boolean simple, final FinishedCallback finishedCallback) {
        this.parent = parent;
        this.simple = simple;
        this.finishedCallback = finishedCallback;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(1, 5, height - 25, 60, 20, "Done"));

        list = new SlotList(mc, width, height, -26 + (FontLoaders.SF21.getHeight()) * 3, height, FontLoaders.SF21.getHeight());
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        list.actionPerformed(button);

        if (button.id == 1) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        list.handleMouseInput();
        super.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        list.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class SlotList extends GuiSlot {

        public SlotList(Minecraft client, int width, int height, int top, int bottom, int slotHeight) {
            super(client, width, height, top, bottom, slotHeight);
        }

        @Override
        protected int getSize() {
            return ProtocolVersionList.getProtocolsNewToOld().size() - 2;
        }

        @Override
        protected void elementClicked(int index, boolean b, int i1, int i2) {
            finishedCallback.finished(ProtocolVersionList.getProtocolsNewToOld().get(index), parent);
        }

        @Override
        protected boolean isSelected(int index) {
            return false;
        }

        @Override
        protected void drawBackground() {
            drawDefaultBackground();
        }

        @Override
        protected void drawSlot(int index, int x, int y, int slotHeight, int mouseX, int mouseY) {
            final ProtocolVersion targetVersion = ProtocolBase.getManager().getTargetVersion();
            final ProtocolVersion version = ProtocolVersionList.getProtocolsNewToOld().get(index);

            String color;
            if (targetVersion == version) {
                color = ProtocolSelector.this.simple ? ChatFormatting.GOLD.toString() : ChatFormatting.GREEN.toString();
            } else {
                color = ProtocolSelector.this.simple ? ChatFormatting.WHITE.toString() : ChatFormatting.DARK_RED.toString();
            }

            FontLoaders.SF21.drawCenteredStringWithShadow((color) + version.getName(), width / 2F, y - 2.5f, -1);
        }
    }

    public interface FinishedCallback {

        void finished(final ProtocolVersion version, final GuiScreen parent);

    }

}