package we.devs.opium.client.modules.movement;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec2f;
import we.devs.opium.client.events.PlayerMoveEvent;
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.client.values.impl.ValueBoolean;


@RegisterModule(name = "InstantSpeed", description = "Removes the acceleration time.", category = Module.Category.MOVEMENT)
public class InstantSpeed extends Module {
    ValueBoolean ignoreFalling = new ValueBoolean("IgnoreFalling", "ignore Falling", "This settings determines if the InstantSpeed is active during a Fall.", true);

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player != null && mc.world != null) {
            if (mc.player.isFallFlying() && !this.ignoreFalling.getValue()) return;
            double speedEffect = 1.0;
            double slowEffect = 1.0;
            if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                double amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
                speedEffect = 1 + (0.2 * (amplifier + 1));
            }
            if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                double amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
                slowEffect = 1 + (0.2 * (amplifier + 1));
            }
            final double base = 0.2873f * speedEffect / slowEffect;
            Vec2f motion = handleVanillaMotion((float) base);
            event.setX(motion.x);
            event.setZ(motion.y);
        }
    }

    public Vec2f handleVanillaMotion(final float speed) {
        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;
        if (forward == 0.0f && strafe == 0.0f) {
            return Vec2f.ZERO;
        } else if (forward != 0.0f && strafe != 0.0f) {
            forward *= (float) Math.sin(0.7853981633974483);
            strafe *= (float) Math.cos(0.7853981633974483);
        }
        return new Vec2f((float) (forward * speed * -Math.sin(Math.toRadians(mc.player.getYaw())) + strafe * speed * Math.cos(Math.toRadians(mc.player.getYaw()))),
                (float) (forward * speed * Math.cos(Math.toRadians(mc.player.getYaw())) - strafe * speed * -Math.sin(Math.toRadians(mc.player.getYaw()))));
    }
}
