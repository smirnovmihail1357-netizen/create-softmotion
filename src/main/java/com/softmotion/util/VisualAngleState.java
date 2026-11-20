package com.softmotion.util;

public final class VisualAngleState {
    public float lastRealAngle;
    public float visualAngle;
    /** Plain multiplier at the time of the last update; a change rebases the visual angle. */
    public float lastBaseMultiplier;
    /** Wall clock of the last update, used to estimate real RPM. */
    public long lastTimeNanos;
    /** Smoothed real-RPM estimate used by the formula. */
    public float lastRpm;

    public VisualAngleState(float lastRealAngle, float visualAngle, float baseMultiplier, long lastTimeNanos) {
        this.lastRealAngle = lastRealAngle;
        this.visualAngle = visualAngle;
        this.lastBaseMultiplier = baseMultiplier;
        this.lastTimeNanos = lastTimeNanos;
    }
}
