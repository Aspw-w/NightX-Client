package net.aspw.client.features.module.impl.movement.speeds.aac;

import net.aspw.client.event.MoveEvent;
import net.aspw.client.features.module.impl.movement.speeds.SpeedMode;
import net.aspw.client.utils.MovementUtils;

/**
 * The type Aac low hop.
 */
public class AACLowHop extends SpeedMode {
    private boolean legitJump;

    /**
     * Instantiates a new Aac low hop.
     */
    public AACLowHop() {
        super("AACLowHop");
    }

    @Override
    public void onEnable() {
        legitJump = true;
    }

    @Override
    public void onMotion() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                if (legitJump) {
                    mc.thePlayer.jump();
                    legitJump = false;
                    return;
                }

                mc.thePlayer.motionY = 0.343F;
                MovementUtils.strafe(0.534F);
            }
        } else {
            legitJump = true;
        }
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void onMove(MoveEvent event) {
    }
}
