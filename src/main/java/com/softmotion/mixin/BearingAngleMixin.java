package com.softmotion.mixin;

import com.softmotion.config.SoftMotionConfig;
import com.softmotion.util.SoftMotionAngles;
import com.softmotion.util.VisualAngleState;
import java.util.WeakHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity", remap = false)
public abstract class BearingAngleMixin {
    private static final WeakHashMap<Object, VisualAngleState> SOFTMOTION$ANGLES = new WeakHashMap<>();

    @Inject(method = "getInterpolatedAngle", at = @At("RETURN"), cancellable = true, require = 0)
    private void softmotion$scaleBearingInterpolatedAngle(float partialTicks, CallbackInfoReturnable<Float> cir) {
        float realAngle = SoftMotionAngles.normalize(cir.getReturnValueF());
        float multiplier = SoftMotionConfig.visualMultiplier();

        VisualAngleState state = SOFTMOTION$ANGLES.get(this);
        if (state == null) {
            state = new VisualAngleState(realAngle, realAngle);
            SOFTMOTION$ANGLES.put(this, state);
            cir.setReturnValue(realAngle);
            return;
        }

        float delta = SoftMotionAngles.shortestDelta(state.lastRealAngle, realAngle);
        state.visualAngle = SoftMotionAngles.normalize(state.visualAngle + delta * multiplier);
        state.lastRealAngle = realAngle;

        cir.setReturnValue(state.visualAngle);
    }
}
