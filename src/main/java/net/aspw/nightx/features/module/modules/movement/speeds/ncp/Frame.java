package net.aspw.nightx.features.module.modules.movement.speeds.ncp;

import net.aspw.nightx.NightX;
import net.aspw.nightx.event.MoveEvent;
import net.aspw.nightx.features.module.modules.movement.speeds.SpeedMode;
import net.aspw.nightx.features.module.modules.world.Scaffold;
import net.aspw.nightx.utils.MovementUtils;
import net.aspw.nightx.utils.timer.TickTimer;

public class Frame extends SpeedMode {

    private final TickTimer tickTimer = new TickTimer();
    private int motionTicks;
    private boolean move;

    public Frame() {
        super("Frame");
    }

    @Override
    public void onMotion() {
        if (mc.thePlayer.movementInput.moveForward > 0.0f || mc.thePlayer.movementInput.moveStrafe > 0.0f) {
            final double speed = 4.25;

            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();

                if (motionTicks == 1) {
                    tickTimer.reset();
                    if (move) {
                        mc.thePlayer.motionX = 0;
                        mc.thePlayer.motionZ = 0;
                        move = false;
                    }
                    motionTicks = 0;
                } else
                    motionTicks = 1;
            } else if (!move && motionTicks == 1 && tickTimer.hasTimePassed(5)) {
                mc.thePlayer.motionX *= speed;
                mc.thePlayer.motionZ *= speed;
                move = true;
            }

            if (!mc.thePlayer.onGround)
                MovementUtils.strafe();
            tickTimer.update();
        }
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void onDisable() {
        final Scaffold scaffold = NightX.moduleManager.getModule(Scaffold.class);

        if (!mc.thePlayer.isSneaking() && !scaffold.getState())
            MovementUtils.strafe(0.2f);
    }

    @Override
    public void onMove(MoveEvent event) {
    }
}
