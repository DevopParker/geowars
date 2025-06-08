package com.geowars.core.engine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FireworkExplosion {

    private List<Particle> particles;
    private boolean isActive;
    private static final Random random = new Random();

    public FireworkExplosion(int x, int y, Color color, int particleCount) {
        particles = new ArrayList<>();
        isActive = true;

        for (int i = 0; i < particleCount; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            // double speed = 100 + random.nextDouble() * 300;
            double speed = 1 + random.nextDouble() * 200; // range: 50–1050
            double dx = Math.cos(angle) * speed;
            double dy = Math.sin(angle) * speed;

            double life = 2.0 + random.nextDouble() * 2.0; // life: 1.5 to 2.5 seconds
            particles.add(new Particle(x, y, dx, dy, color, life));
        }
    }


    public void update(double delta) {
        if (!isActive) return;

        for (Particle particle : particles) {
            particle.update(delta);
        }

        // ✅ Safely remove particles marked as dead
        particles.removeIf(p -> p.isDead());

        if (particles.isEmpty()) {
            isActive = false;
        }
    }

    public void render(Graphics2D g2d) {
        if (!isActive) return;

        // ✅ Defensive copy to avoid concurrent modification during render
        List<Particle> snapshot = new ArrayList<>(particles);
        for (Particle particle : snapshot) {
            particle.render(g2d);
        }
    }

    public boolean isActive() {
        return isActive;
    }
}
