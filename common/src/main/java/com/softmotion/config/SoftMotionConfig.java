package com.softmotion.config;

import com.softmotion.util.Formula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader-agnostic facade over the client configuration.
 *
 * <p>Each platform module registers a {@link Provider} at startup:
 * Forge/NeoForge use their native config spec, Fabric uses the tiny TOML
 * reader in this package. Until a provider is registered (or if none ever
 * loads on a dedicated server) the documented defaults are used.</p>
 */
public final class SoftMotionConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("softmotion");
    private static final Object COMPILE_LOCK = new Object();

    public interface Provider {
        double visualSpeedMultiplier();

        String visualSpeedFormula();

        boolean slowBearingContraptions();
    }

    private static final Provider DEFAULTS = new Provider() {
        @Override
        public double visualSpeedMultiplier() {
            return 0.25D;
        }

        @Override
        public String visualSpeedFormula() {
            return "";
        }

        @Override
        public boolean slowBearingContraptions() {
            return true;
        }
    };

    private static volatile Provider provider = DEFAULTS;

    public static void setProvider(Provider newProvider) {
        provider = newProvider == null ? DEFAULTS : newProvider;
    }

    public static float visualMultiplier() {
        return (float) provider.visualSpeedMultiplier();
    }

    public static boolean slowBearingContraptions() {
        return provider.slowBearingContraptions();
    }

    /** True when a non-empty formula is configured (even if it currently fails to parse). */
    public static boolean formulaActive() {
        return !provider.visualSpeedFormula().isBlank();
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
        String source = provider.visualSpeedFormula();
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

    private static volatile String compiledSource;
    private static volatile Formula compiledFormula;
}
