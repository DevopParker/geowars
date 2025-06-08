package com.geowars.core.engine;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class FireworkManager {
    private ArrayList<FireworkExplosion> explosions;

    public FireworkManager() {
        explosions = new ArrayList<>();
    }

    public void createExplosion(double x, double y, Color color, int particleCount) {
        FireworkExplosion explosion = new FireworkExplosion((int)x, (int)y, color, particleCount);
        explosions.add(explosion);
    }

    public void update(double delta) {
        for (int i = explosions.size() - 1; i >= 0; i--) {
            FireworkExplosion explosion = explosions.get(i);
            explosion.update(delta);

            if (!explosion.isActive()) {
                explosions.remove(i);
            }
        }
    }

    public void render(Graphics2D g2d) {
        for (FireworkExplosion explosion : explosions) {
            explosion.render(g2d);
        }
    }
}