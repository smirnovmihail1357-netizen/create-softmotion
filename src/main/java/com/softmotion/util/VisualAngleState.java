package com.softmotion.util;

public final class VisualAngleState {
    public float lastRealAngle;
    public float visualAngle;

    public VisualAngleState(float lastRealAngle, float visualAngle) {
        this.lastRealAngle = lastRealAngle;
        this.visualAngle = visualAngle;
    }
}
