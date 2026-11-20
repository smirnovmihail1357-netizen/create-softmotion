package com.softmotion.forge;

import com.softmotion.config.SoftMotionConfig;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Forge 1.20.1 config spec. Mirrors the NeoForge 1.21.1 config one-to-one
 * (same keys, comments, defaults and translation keys), so the generated
 * softmotion-client.toml is familiar to users on both platforms.
 */
public final class SoftMotionForgeConfig implements SoftMotionConfig.Provider {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.DoubleValue VISUAL_SPEED_MULTIPLIER;
    private static final ForgeConfigSpec.BooleanValue SLOW_BEARING_CONTRAPTIONS;
    private static final ForgeConfigSpec.ConfigValue<String> VISUAL_SPEED_FORMULA;

    @Override
    public double visualSpeedMultiplier() {
        return VISUAL_SPEED_MULTIPLIER.get();
    }

    @Override
    public String visualSpeedFormula() {
        return VISUAL_SPEED_FORMULA.get();
    }

    @Override
    public boolean slowBearingContraptions() {
        return SLOW_BEARING_CONTRAPTIONS.get();
    }

    public SoftMotionForgeConfig() {
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("client");

        VISUAL_SPEED_MULTIPLIER = builder
                .comment(
                        "Client-only visual multiplier for rendered Create rotation.",
                        "Does NOT change Create kinetic speed, stress, RPM, networks, or gameplay.",
                        "Used as-is when visualSpeedFormula is empty, and as a fallback if the formula is invalid.",
                        "0.0 = visually frozen",
                        "0.25 = visual rotation speed divided by 4",
                        "1.0 = vanilla Create visuals")
                .translation("softmotion.config.visualSpeedMultiplier")
                .defineInRange("visualSpeedMultiplier", 0.25D, 0.0D, 1.0D);

        VISUAL_SPEED_FORMULA = builder
                .comment(
                        "Optional formula for the visual speed multiplier, evaluated per element.",
                        "Empty = use visualSpeedMultiplier (default behaviour).",
                        "Variables: rpm = real absolute speed of the element in Create units,",
                        "speed = rpm * 6 (Flywheel rotational-speed unit).",
                        "The result is a multiplier, clamped to 0.0..1.0.",
                        "Operators: + - * / % ^, comparisons, &&, ||, !, ? : .",
                        "Functions: abs, min, max, clamp, sqrt, sign, floor, ceil, round, pow, log, exp, sin, cos, tan, if.",
                        "Examples:",
                        "  min(1, 8 / (rpm + 0.001))       -- faster machines are slowed down more",
                        "  clamp(0.25 + rpm / 200, 0.1, 1) -- gentle speed-dependent slowdown",
                        "  if(rpm > 100, 0.1, 0.5)         -- two speed bands")
                .translation("softmotion.config.visualSpeedFormula")
                .define("visualSpeedFormula", "");

        SLOW_BEARING_CONTRAPTIONS = builder
                .comment(
                        "If true, assembled contraption entities attached to Mechanical Bearings",
                        "are also slowed visually. The formula is applied to them as well.",
                        "This is client-only and does not change server-side contraption logic.")
                .translation("softmotion.config.slowBearingContraptions")
                .define("slowBearingContraptions", true);

        builder.pop();

        SPEC = builder.build();
    }
}
