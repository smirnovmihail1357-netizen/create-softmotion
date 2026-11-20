package com.softmotion.mixin;

import com.softmotion.config.SoftMotionConfig;
import com.softmotion.util.SoftMotionAngles;
import com.softmotion.util.VisualAngleState;
import java.util.WeakHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity", remap = false)
public abstract class BearingAngleMixin {
    private static final WeakHashMap<Object, VisualAngleState> SOFTMOTION$ANGLES = new WeakHashMap<>();

    @Unique
    private float softmotion$angularSpeed;

    /** Capture the real angular speed without a compile-time reference to the Create class. */
    @Inject(method = "getAngularSpeed", at = @At("RETURN"), require = 0)
    private void softmotion$captureAngularSpeed(CallbackInfoReturnable<Float> cir) {
        softmotion$angularSpeed = cir.getReturnValueF();
    }

    @Inject(method = "getInterpolatedAngle", at = @At("RETURN"), cancellable = true, require = 0)
    private void softmotion$scaleBearingInterpolatedAngle(float partialTicks, CallbackInfoReturnable<Float> cir) {
        float realAngle = SoftMotionAngles.normalize(cir.getReturnValueF());
        float baseMultiplier = SoftMotionConfig.visualMultiplier();

        VisualAngleState state = SOFTMOTION$ANGLES.get(this);
        if (state == null) {
            state = new VisualAngleState(realAngle, realAngle, baseMultiplier, System.nanoTime());
            SOFTMOTION$ANGLES.put(this, state);
            cir.setReturnValue(realAngle);
            return;
        }

        // A config change must not leave the visual angle permanently behind the real one.
        if (state.lastBaseMultiplier != baseMultiplier) {
            state.visualAngle = realAngle;
            state.lastBaseMultiplier = baseMultiplier;
        }

        // getAngularSpeed() returns degrees per tick, including client-only corrections.
        // KineticBlockEntity.convertToAngular(speed) = speed * 360 / 60 / 20 = speed * 0.3,
        // so dividing by 0.3 converts it back to Create's RPM-like speed units.
        float rpm = Math.abs(softmotion$angularSpeed) / 0.3F;
        float multiplier = SoftMotionConfig.multiplier(rpm);
        float delta = SoftMotionAngles.shortestDelta(state.lastRealAngle, realAngle);
        state.visualAngle = SoftMotionAngles.normalize(state.visualAngle + delta * multiplier);
        state.lastRealAngle = realAngle;

        cir.setReturnValue(state.visualAngle);
    }
}
