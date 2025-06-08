package com.geowars.core.engine;

public class Shockwave {
    public float x, y;
    public float time = 0f;
    public float speed = 300f;
    public float maxRadius = 600f;

    public Shockwave(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getRadius() {
        return speed * time;
    }

    public boolean isExpired() {
        return time > 4.0f;
    }
}