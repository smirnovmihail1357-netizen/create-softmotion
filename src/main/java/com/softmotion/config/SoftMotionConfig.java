package com.softmotion.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class SoftMotionConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue VISUAL_SPEED_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue SLOW_BEARING_CONTRAPTIONS;

    public static float visualMultiplier() {
        return VISUAL_SPEED_MULTIPLIER.get().floatValue();
    }

    public static boolean slowBearingContraptions() {
        return SLOW_BEARING_CONTRAPTIONS.get();
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
                        "0.0 = visually frozen",
                        "0.25 = visual rotation speed divided by 4",
                        "1.0 = vanilla Create visuals")
                .translation("softmotion.config.visualSpeedMultiplier")
                .defineInRange("visualSpeedMultiplier", 0.25D, 0.0D, 1.0D);

        SLOW_BEARING_CONTRAPTIONS = builder
                .comment(
                        "If true, assembled contraption entities attached to Mechanical Bearings",
                        "are also slowed visually using visualSpeedMultiplier.",
                        "This is client-only and does not change server-side contraption logic.")
                .translation("softmotion.config.slowBearingContraptions")
                .define("slowBearingContraptions", true);

        builder.pop();

        SPEC = builder.build();
    }
}
