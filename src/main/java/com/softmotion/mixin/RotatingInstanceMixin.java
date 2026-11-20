package com.softmotion.mixin;

import com.softmotion.config.SoftMotionConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Create 0.3.2g (the only Create for 1.16.5) funnels every Flywheel instance speed
 * through {@code com.simibubi.create.content.contraptions.base.KineticData#setRotationalSpeed(float)}.
 *
 * <p>Bytecode-verified in the 0.3.2g jar: {@code KineticTileInstance.setup(RotatingData, Axis, float)}
 * and {@code updateRotation(RotatingData, Axis, float)} pass the raw
 * {@code KineticTileEntity.getSpeed()} straight into this method (no SPEED_MULTIPLIER,
 * unlike Create 6's RotatingInstance). The stored value is therefore already the real
 * absolute speed in Create units: rpm = |speed| with no extra scaling. The value keeps
 * its sign, so counter-rotating instances stay counter-rotating.</p>
 */
@Mixin(targets = "com.simibubi.create.content.contraptions.base.KineticData", remap = false)
public abstract class RotatingInstanceMixin {
    @ModifyVariable(method = "setRotationalSpeed", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private float softmotion$scaleFlywheelRotationalSpeed(float originalSpeed) {
        float rpm = Math.abs(originalSpeed);
        return originalSpeed * SoftMotionConfig.multiplier(rpm);
    }
}
