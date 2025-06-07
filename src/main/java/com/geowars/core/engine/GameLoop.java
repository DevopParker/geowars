package com.geowars.core.engine;

public class GameLoop implements Runnable {
    private boolean running = false;

    public void start() {
        if (!running) {
            running = true;
            Thread thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            GameCanvas.getGamePanel().update(delta);
            GameCanvas.getGamePanel().repaint();
        }

    }
}