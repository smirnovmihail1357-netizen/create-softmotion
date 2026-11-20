package com.softmotion.config;

import com.softmotion.util.Formula;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SoftMotionConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("softmotion");
    private static final Object COMPILE_LOCK = new Object();

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue VISUAL_SPEED_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue SLOW_BEARING_CONTRAPTIONS;
    public static final ModConfigSpec.ConfigValue<String> VISUAL_SPEED_FORMULA;

    private static volatile String compiledSource;
    private static volatile Formula compiledFormula;

    public static float visualMultiplier() {
        return VISUAL_SPEED_MULTIPLIER.get().floatValue();
    }

    public static boolean slowBearingContraptions() {
        return SLOW_BEARING_CONTRAPTIONS.get();
    }

    /** True when a non-empty formula is configured (even if it currently fails to parse). */
    public static boolean formulaActive() {
        return !VISUAL_SPEED_FORMULA.get().isBlank();
    }

    /**
     * Returns the visual multiplier for one element with the given real speed.
     *
     * <p>When the formula is empty the plain {@code visualSpeedMultiplier} is used.
     * Otherwise the formula is compiled once and evaluated with two variables:
     * {@code rpm} (real absolute speed in Create units) and {@code speed} (= rpm * 6,
     * the Flywheel rotational-speed unit). The result is clamped to 0..1; a broken
     * formula is logged once and falls back to the plain multiplier.</p>
     */
    public static float multiplier(float rpm) {
        String source = VISUAL_SPEED_FORMULA.get();
        Formula formula = compiledFormula;
        if (formula == null || !source.equals(compiledSource)) {
            synchronized (COMPILE_LOCK) {
                if (!source.equals(compiledSource)) {
                    Formula parsed = null;
                    if (!source.isBlank()) {
                        try {
                            parsed = Formula.compile(source);
                        } catch (RuntimeException e) {
                            LOGGER.warn("Invalid softmotion visualSpeedFormula, using visualSpeedMultiplier: {}", e.getMessage());
                        }
                    }
                    compiledFormula = parsed;
                    compiledSource = source;
                }
                formula = compiledFormula;
            }
        }
        if (formula == null) {
            return visualMultiplier();
        }
        double result = formula.evaluate(rpm, rpm * 6.0D);
        if (!Double.isFinite(result)) {
            return visualMultiplier();
        }
        return (float) Math.max(0.0D, Math.min(1.0D, result));
    }

    private SoftMotionConfig() {
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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
