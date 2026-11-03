package com.softmotion.mixin;

import com.softmotion.config.SoftMotionConfig;
import com.softmotion.util.SoftMotionAngles;
import com.softmotion.util.VisualAngleState;
import java.util.WeakHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "com.simibubi.create.content.contraptions.ControlledContraptionEntity", remap = false)
public abstract class ControlledContraptionEntityMixin {
    private static final WeakHashMap<Object, VisualAngleState> SOFTMOTION$ENTITY_ANGLES = new WeakHashMap<>();

    @ModifyVariable(method = "setAngle", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private float softmotion$scaleContraptionEntityAngle(float realAngleRaw) {
        if (!SoftMotionConfig.slowBearingContraptions()) {
            SOFTMOTION$ENTITY_ANGLES.remove(this);
            return realAngleRaw;
        }

        float multiplier = SoftMotionConfig.visualMultiplier();
        if (multiplier >= 0.9999f) {
            SOFTMOTION$ENTITY_ANGLES.remove(this);
            return realAngleRaw;
        }

        float realAngle = SoftMotionAngles.normalize(realAngleRaw);

        VisualAngleState state = SOFTMOTION$ENTITY_ANGLES.get(this);
        if (state == null) {
            state = new VisualAngleState(realAngle, realAngle);
            SOFTMOTION$ENTITY_ANGLES.put(this, state);
            return realAngleRaw;
        }

        float delta = SoftMotionAngles.shortestDelta(state.lastRealAngle, realAngle);
        state.visualAngle = SoftMotionAngles.normalize(state.visualAngle + delta * multiplier);
        state.lastRealAngle = realAngle;

        return state.visualAngle;
    }
}
