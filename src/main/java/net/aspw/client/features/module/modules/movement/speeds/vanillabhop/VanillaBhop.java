package net.aspw.client.features.module.modules.movement.speeds.vanillabhop;

import net.aspw.client.Client;
import net.aspw.client.event.EventState;
import net.aspw.client.event.MotionEvent;
import net.aspw.client.event.MoveEvent;
import net.aspw.client.features.module.modules.movement.Speed;
import net.aspw.client.features.module.modules.movement.speeds.SpeedMode;
import net.aspw.client.features.module.modules.player.Scaffold;
import net.aspw.client.utils.MovementUtils;

public class VanillaBhop extends SpeedMode {

    public VanillaBhop() {
        super("VanillaBhop");
    }

    @Override
    public void onMotion(MotionEvent eventMotion) {
        if (MovementUtils.isMoving()) {
            MovementUtils.strafe(0.9f);
        } else {
            mc.thePlayer.motionX = 0.0;
            mc.thePlayer.motionZ = 0.0;
        }
        final Speed speed = Client.moduleManager.getModule(Speed.class);

        if (speed == null || eventMotion.getEventState() != EventState.PRE) {
        }
    }

    @Override
    public void onMotion() {
    }

    @Override
    public void onUpdate() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround)
                mc.thePlayer.jump();
        }
    }

    @Override
    public void onDisable() {
        final Scaffold scaffold = Client.moduleManager.getModule(Scaffold.class);

        if (!mc.thePlayer.isSneaking() && !scaffold.getState())
            MovementUtils.strafe(0.2f);
    }

    @Override
    public void onMove(MoveEvent event) {
    }
}