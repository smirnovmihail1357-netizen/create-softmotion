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

    private SoftMotionAngles() {
    }
}
