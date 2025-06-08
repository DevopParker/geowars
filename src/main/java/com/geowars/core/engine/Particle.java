package com.geowars.core.engine;
import java.awt.*;

public class Particle {
    private double x, y;
    private double velX, velY;
    private double gravity = 50;
    private double life = 1.0;
    private double maxLife;
    private Color color;
    private int size;

    public Particle(double x, double y, double velX, double velY, Color color, double life) {
        this.x = x;
        this.y = y;
        this.velX = velX;
        this.velY = velY;
        this.color = color;
        this.life = life;
        this.maxLife = life;
        this.size = (int)(Math.random() * 6 + 4);
    }

    public void update(double delta) {
        x += velX * delta;
        y += velY * delta;
        velY += gravity * delta;
        life -= delta;
        double damping = Math.pow(0.2, delta); // slows over time
        velX *= damping;
        velY *= damping;
        if (Math.abs(velX) < 0.01) velX = 0;
        if (Math.abs(velY) < 0.01) velY = 0;


        System.out.printf("dx: %.2f, dy: %.2f, delta: %.4f, moveX: %.2f, moveY: %.2f\n",
                velX, velY, delta, velX * delta, velY * delta);
    }

    public void render(Graphics2D g2d) {
        if (life <= 0) return;

        float alpha = (float)(life / maxLife);
        int alphaValue = Math.max(0, Math.min(255, (int)(255 * alpha)));
        Color fadedColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                alphaValue
        );

        //System.out.println("Alpha: " + alpha + ", life: " + life);

        g2d.setColor(fadedColor);
        int x1 = (int)x;
        int y1 = (int)y;
        int x2 = (int)(x - velX * 0.05); // Trail in opposite direction of motion
        int y2 = (int)(y - velY * 0.05);

        Stroke originalStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2));

        g2d.drawLine(x1, y1, x2, y2);
        g2d.setStroke(originalStroke);


        if (alpha > 0.5) {
            g2d.setColor(Color.WHITE);
            g2d.fillOval((int)x - 1, (int)y - 1, 2, 2);
        }
    }

    public boolean isDead() {
        return life <= 0;
    }
}