package com.softmotion.mixin;

import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.softmotion.config.SoftMotionConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = RotatingInstance.class, remap = false)
public abstract class RotatingInstanceMixin {
    @ModifyVariable(method = "setRotationalSpeed", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private float softmotion$scaleFlywheelRotationalSpeed(float originalSpeed) {
        // Create passes getSpeed() * RotatingInstance.SPEED_MULTIPLIER, so this is the real
        // absolute speed in Create units. The formula (or the plain multiplier) returns a
        // fraction of the real speed, which keeps the sign of counter-rotating instances.
        float rpm = Math.abs(originalSpeed) / RotatingInstance.SPEED_MULTIPLIER;
        return originalSpeed * SoftMotionConfig.multiplier(rpm);
    }
}
