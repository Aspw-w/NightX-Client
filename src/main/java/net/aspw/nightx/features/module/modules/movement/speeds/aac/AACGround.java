package net.aspw.nightx.features.module.modules.movement.speeds.aac;

import net.aspw.nightx.NightX;
import net.aspw.nightx.event.MoveEvent;
import net.aspw.nightx.features.module.modules.movement.Speed;
import net.aspw.nightx.features.module.modules.movement.speeds.SpeedMode;
import net.aspw.nightx.utils.MovementUtils;
import net.minecraft.network.play.client.C03PacketPlayer;

public class AACGround extends SpeedMode {
    public AACGround() {
        super("AACGround");
    }

    @Override
    public void onMotion() {

    }

    @Override
    public void onUpdate() {
        if (!MovementUtils.isMoving())
            return;

        mc.timer.timerSpeed = NightX.moduleManager.getModule(Speed.class).aacGroundTimerValue.get();
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
    }

    @Override
    public void onMove(MoveEvent event) {

    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1F;
    }
}
