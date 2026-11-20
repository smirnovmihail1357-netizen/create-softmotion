package com.softmotion.mixin;

import com.simibubi.create.content.kinetics.base.flwdata.KineticData;
import com.softmotion.config.SoftMotionConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Create 0.5.1 replacement for the 1.20.1 {@code RotatingInstanceMixin}.
 *
 * <p>Create 0.5.1 has no RotatingInstance: every flywheel render instance
 * (shafts, cogwheels, belts, gearboxes, mixers) funnels its visual rotation
 * speed through {@code KineticBlockEntityInstance#updateRotation}/{@code setup},
 * whose terminal overloads pass the incoming float <em>raw</em> to
 * {@code KineticData#setRotationalSpeed} (no {@code SPEED_MULTIPLIER},
 * unlike Create 6.x). {@code setRotationalSpeed} is therefore the single
 * loader-portable choke point for rendered rotational speed, and the value
 * it receives is already the speed in Create units.</p>
 */
@Mixin(value = KineticData.class, remap = false)
public abstract class KineticDataMixin {
    @ModifyVariable(method = "setRotationalSpeed", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private float softmotion$scaleRotationalSpeed(float speed) {
        // Raw Create-units speed (sign encodes rotation direction) — see the
        // funnel in KineticBlockEntityInstance#updateRotation/#setup on 0.5.1.
        float rpm = Math.abs(speed);
        return speed * SoftMotionConfig.multiplier(rpm);
    }
}
