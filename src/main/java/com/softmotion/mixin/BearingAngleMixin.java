package com.softmotion.mixin;

import com.softmotion.config.SoftMotionConfig;
import com.softmotion.util.SoftMotionAngles;
import com.softmotion.util.VisualAngleState;
import java.util.WeakHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity", remap = false)
public abstract class BearingAngleMixin {
    private static final WeakHashMap<Object, VisualAngleState> SOFTMOTION$ANGLES = new WeakHashMap<>();

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

        float delta = SoftMotionAngles.shortestDelta(state.lastRealAngle, realAngle);
        long now = System.nanoTime();
        float seconds = (now - state.lastTimeNanos) / 1_000_000_000.0F;
        state.lastTimeNanos = now;
        state.lastRpm = SoftMotionAngles.estimateRpm(delta, seconds, state.lastRpm);

        float multiplier = SoftMotionConfig.multiplier(state.lastRpm);
        state.visualAngle = SoftMotionAngles.normalize(state.visualAngle + delta * multiplier);
        state.lastRealAngle = realAngle;

        cir.setReturnValue(state.visualAngle);
    }
}
