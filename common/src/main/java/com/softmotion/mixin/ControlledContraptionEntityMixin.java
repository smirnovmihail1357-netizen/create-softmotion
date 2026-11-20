package com.softmotion.mixin;

import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.softmotion.config.SoftMotionConfig;
import com.softmotion.util.SoftMotionAngles;
import com.softmotion.util.VisualAngleState;
import java.util.WeakHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ControlledContraptionEntity.class, remap = false)
public abstract class ControlledContraptionEntityMixin {
    private static final WeakHashMap<Object, VisualAngleState> SOFTMOTION$ENTITY_ANGLES = new WeakHashMap<>();

    @ModifyVariable(method = "setAngle", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private float softmotion$scaleContraptionEntityAngle(float realAngleRaw) {
        if (!SoftMotionConfig.slowBearingContraptions()
                || (!SoftMotionConfig.formulaActive() && SoftMotionConfig.visualMultiplier() >= 0.9999F)) {
            SOFTMOTION$ENTITY_ANGLES.remove(this);
            return realAngleRaw;
        }

        float baseMultiplier = SoftMotionConfig.visualMultiplier();
        float realAngle = SoftMotionAngles.normalize(realAngleRaw);
        long now = System.nanoTime();

        VisualAngleState state = SOFTMOTION$ENTITY_ANGLES.get(this);
        if (state == null) {
            state = new VisualAngleState(realAngle, realAngle, baseMultiplier, now);
            SOFTMOTION$ENTITY_ANGLES.put(this, state);
            return realAngleRaw;
        }

        // A config change must not leave the visual angle permanently behind the real one.
        if (state.lastBaseMultiplier != baseMultiplier) {
            state.visualAngle = realAngle;
            state.lastBaseMultiplier = baseMultiplier;
        }

        float delta = SoftMotionAngles.shortestDelta(state.lastRealAngle, realAngle);
        float seconds = (now - state.lastTimeNanos) / 1_000_000_000.0F;
        state.lastTimeNanos = now;

        // The entity does not expose its bearing, so estimate the real speed from the angle
        // change over wall-clock time.
        state.lastRpm = SoftMotionAngles.estimateRpm(delta, seconds, state.lastRpm);
        float multiplier = SoftMotionConfig.multiplier(state.lastRpm);

        state.visualAngle = SoftMotionAngles.normalize(state.visualAngle + delta * multiplier);
        state.lastRealAngle = realAngle;

        return state.visualAngle;
    }
}
