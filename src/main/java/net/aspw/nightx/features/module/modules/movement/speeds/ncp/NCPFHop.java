package net.aspw.nightx.features.module.modules.movement.speeds.ncp;

import net.aspw.nightx.NightX;
import net.aspw.nightx.event.MoveEvent;
import net.aspw.nightx.features.module.modules.movement.speeds.SpeedMode;
import net.aspw.nightx.features.module.modules.world.Scaffold;
import net.aspw.nightx.utils.MovementUtils;

public class NCPFHop extends SpeedMode {

    public NCPFHop() {
        super("NCPFHop");
    }

    @Override
    public void onEnable() {
        mc.timer.timerSpeed = 1.0866F;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.thePlayer.speedInAir = 0.02F;
        mc.timer.timerSpeed = 1F;
        super.onDisable();

        final Scaffold scaffold = NightX.moduleManager.getModule(Scaffold.class);

        if (!mc.thePlayer.isSneaking() && !scaffold.getState())
            MovementUtils.strafe(0.2f);
    }

    @Override
    public void onMotion() {

    }

    @Override
    public void onUpdate() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
                mc.thePlayer.motionX *= 1.01D;
                mc.thePlayer.motionZ *= 1.01D;
                mc.thePlayer.speedInAir = 0.0223F;
            }

            mc.thePlayer.motionY -= 0.00099999D;

            MovementUtils.strafe();
        } else {
            mc.thePlayer.motionX = 0D;
            mc.thePlayer.motionZ = 0D;
        }
    }

    @Override
    public void onMove(MoveEvent event) {

    }


}
