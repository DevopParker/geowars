package com.geowars.core.engine;

public class GridPoint {
    public float x, y;
    public float offsetX = 0;
    public float offsetY = 0;
    public float velocityX = 0;
    public float velocityY = 0;
    public float basePhase = (float)(Math.random() * Math.PI * 2); // time-based wave offset
    public float baseAmplitude = 3f + (float)(Math.random() * 2f); // small persistent wave

    public float phaseOffset = (float)(Math.random() * Math.PI * 2); // 🌊 new

    public GridPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getDrawX() {
        return x + offsetX;
    }

    public float getDrawY() {
        return y + offsetY;
    }
}