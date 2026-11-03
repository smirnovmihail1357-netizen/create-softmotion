package com.softmotion.mixin;

import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.softmotion.config.SoftMotionConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@OnlyIn(Dist.CLIENT)
@Mixin(RotatingInstance.class)
public abstract class RotatingInstanceMixin {
    @ModifyVariable(method = "setRotationalSpeed", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private float softmotion$scaleFlywheelRotationalSpeed(float originalSpeed) {
        return originalSpeed * SoftMotionConfig.visualMultiplier();
    }
}
