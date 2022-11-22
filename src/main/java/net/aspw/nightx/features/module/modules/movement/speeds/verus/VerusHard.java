package net.aspw.nightx.features.module.modules.movement.speeds.verus;

import net.aspw.nightx.NightX;
import net.aspw.nightx.event.MoveEvent;
import net.aspw.nightx.features.module.modules.movement.Speed;
import net.aspw.nightx.features.module.modules.movement.speeds.SpeedMode;
import net.aspw.nightx.utils.MovementUtils;

public class VerusHard extends SpeedMode {

    public VerusHard() {
        super("VerusHard");
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1F;
        super.onDisable();

        if (!mc.thePlayer.isSneaking())
            MovementUtils.strafe(0.3f);
    }

    @Override
    public void onMotion() {
        final Speed speed = NightX.moduleManager.getModule(Speed.class);
        if (speed == null)
            return;

        if (!mc.gameSettings.keyBindForward.isKeyDown() && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown() && !mc.gameSettings.keyBindBack.isKeyDown())
            return;

        mc.timer.timerSpeed = speed.verusTimer.get();

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
            if (mc.thePlayer.isSprinting()) {
                MovementUtils.strafe(MovementUtils.getSpeed() + 0.2F);
            }
        }

        MovementUtils.strafe(Math.max((float) MovementUtils.getBaseMoveSpeed(), MovementUtils.getSpeed())); // no sprint = faster - verus, since 2018
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void onMove(MoveEvent event) {
    }
}
