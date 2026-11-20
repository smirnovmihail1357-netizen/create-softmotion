package com.softmotion.util;

public final class SoftMotionAngles {
    public static float normalize(float angle) {
        angle %= 360.0f;
        if (angle < 0.0f) {
            angle += 360.0f;
        }
        return angle;
    }

    public static float shortestDelta(float from, float to) {
        float delta = (to - from) % 360.0f;
        if (delta > 180.0f) {
            delta -= 360.0f;
        }
        if (delta < -180.0f) {
            delta += 360.0f;
        }
        return delta;
    }

    /**
     * Estimates the real speed (Create RPM units) from a real-angle change over wall-clock time.
     * Small/zero deltas keep the previous estimate so the formula does not flicker when the
     * target is queried several times per frame with the same angle.
     */
    public static float estimateRpm(float deltaDegrees, float seconds, float previous) {
        if (seconds <= 0.0F || Math.abs(deltaDegrees) < 0.00001F) {
            return previous;
        }
        // Flywheel units = degrees per second = rpm * 6.
        float instant = Math.min(100_000.0F, Math.abs(deltaDegrees) / (6.0F * seconds));
        return previous <= 0.0F ? instant : previous + (instant - previous) * 0.25F;
    }

    private SoftMotionAngles() {
    }
}
