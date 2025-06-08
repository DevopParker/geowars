package com.geowars.core.engine;
import com.geowars.core.util.Constants;
import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameCanvas extends JFrame {
    private static GamePanel gamePanel;

    public GameCanvas() {
        setTitle("GeoWars");
        setSize((int)Constants.WINDOW_WIDTH, (int)Constants.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel();
        add(gamePanel);

        SwingUtilities.invokeLater(() -> {
            gamePanel.setFocusable(true);
            gamePanel.requestFocusInWindow();
        });

        setVisible(true);
        new GameLoop().start();
    }

    class GamePanel extends JPanel {
        private Point mousePos = new Point(0, 0);
        private boolean showDebugOverlay = true;
        private int frameCount = 0;
        private int currentFPS = 0;
        private long lastTime = System.currentTimeMillis();
        private final Set<Integer> pressedKeys = java.util.Collections.synchronizedSet(new HashSet<>());

        private double playerX;
        private double playerY;
        private final int playerSize = 20;
        private double playerAngle = 0;
        private double facingX = 1;
        private double facingY = 0;

        // Firework system
        private FireworkManager fireworkManager;
        private double fireworkTimer = 0;
        private final double FIREWORK_INTERVAL = 3.0 + Math.random() * 1.0; // 5-6 seconds initially
        private double nextFireworkTime = FIREWORK_INTERVAL;
        private final java.util.Random random = new java.util.Random();

        // GridPoint panel
        private final int gridSpacing = 25;
        private final java.util.List<java.util.List<GridPoint>> grid = new java.util.ArrayList<>();
        private final java.util.List<Shockwave> shockwaves = new java.util.ArrayList<>();


        public GamePanel() {
            playerX = (Constants.WINDOW_WIDTH - playerSize) / 2;
            playerY = (Constants.WINDOW_HEIGHT - playerSize) / 2;

            // Initialize firework manager
            fireworkManager = new FireworkManager();

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mousePos = e.getPoint();
                }
            });

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    pressedKeys.add(e.getKeyCode());
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    pressedKeys.remove(e.getKeyCode());
                }
            });

            initGrid();
        }

        private void initGrid() {
            grid.clear();
            for (int y = 0; y <= Constants.WINDOW_HEIGHT; y += gridSpacing) {
                List<GridPoint> row = new ArrayList<>();
                for (int x = 0; x <= Constants.WINDOW_WIDTH; x += gridSpacing) {
                    row.add(new GridPoint(x, y));
                }
                grid.add(row);
            }
        }


        public void update(double delta) {
            // Player movement code
            double inputX = 0;
            double inputY = 0;
            double speed = 200;

            if (pressedKeys.contains(KeyEvent.VK_W)) inputY -= 1;
            if (pressedKeys.contains(KeyEvent.VK_S)) inputY += 1;
            if (pressedKeys.contains(KeyEvent.VK_A)) inputX -= 1;
            if (pressedKeys.contains(KeyEvent.VK_D)) inputX += 1;

            boolean hasInput = inputX != 0 || inputY != 0;

            if (hasInput) {
                double len = Math.sqrt(inputX * inputX + inputY * inputY);
                inputX /= len;
                inputY /= len;

                double targetAngle = Math.atan2(inputY, inputX);
                double angleDiff = normalizeAngle(targetAngle - playerAngle);
                double rotationSpeed = Math.PI * 4;

                if (Math.abs(angleDiff) > rotationSpeed * delta) {
                    playerAngle += Math.signum(angleDiff) * rotationSpeed * delta;
                } else {
                    playerAngle = targetAngle;
                }

                facingX = Math.cos(playerAngle);
                facingY = Math.sin(playerAngle);

                playerX += inputX * speed * delta;
                playerY += inputY * speed * delta;
            }

            // Update firework timer
            fireworkTimer += delta;

            // Check if it's time to spawn a firework
            if (fireworkTimer >= nextFireworkTime) {
                spawnRandomFirework();
                fireworkTimer = 0;
                // Set next firework time to 5-6 seconds randomly
                nextFireworkTime = 2.0;// + random.nextDouble() * 1.0;
            }

            // Update existing fireworks
            fireworkManager.update(delta);

            // Update shockwaves
            for (Shockwave sw : shockwaves) {
                sw.time += delta;
            }

            // Remove old shockwaves
            shockwaves.removeIf(Shockwave::isExpired);

            float time = (float)System.currentTimeMillis() / 1000f;

            for (List<GridPoint> row : grid) {
                for (GridPoint p : row) {

                    // 🌊 Smooth, slow, wide-band base wave motion
                    float waveSpeed = 0.4f;           // 🐢 slow global wave speed
                    float wavelength = 150f;          // 🌊 big bands of motion
                    float amplitude = 4f;             // subtle swaying

                    float waveX = (float)Math.sin((p.y / wavelength) + (time * waveSpeed)) * amplitude;
                    float waveY = (float)Math.sin((p.x / wavelength) + (time * waveSpeed)) * amplitude;

                    float offsetX = waveX;
                    float offsetY = waveY;

                    // 💥 Firework influence: slow, soft outward pulse
                    for (Shockwave sw : shockwaves) {
                        float dx = p.x - sw.x;
                        float dy = p.y - sw.y;
                        float dist = (float)Math.sqrt(dx * dx + dy * dy);

                        float angle = (float)Math.atan2(dy, dx);
                        float fireAmplitude = 15f;
                        float fireWavelength = 100f;
                        float fireFrequency = 0.5f; // 👈 slow wave movement

                        float phase = (dist / fireWavelength) - (sw.time * fireFrequency);
                        float wavefront = sw.getRadius(); // sw.time * fireFrequency * fireWavelength
                        float thickness = 30f;

                        if (Math.abs(dist - wavefront) < thickness) {
                            float falloff = 1.0f - (Math.abs(dist - wavefront) / thickness);
                            float wave = (float)Math.sin(phase * 2 * Math.PI) * fireAmplitude * falloff;

                            offsetX += wave * Math.cos(angle);
                            offsetY += wave * Math.sin(angle);
                        }
                    }

                    // 🧘 Ease into the new offset — soft spring effect
                    p.velocityX += (offsetX - p.offsetX) * 0.02f;
                    p.velocityX *= 0.96f;
                    p.offsetX += p.velocityX;

                    p.velocityY += (offsetY - p.offsetY) * 0.02f;
                    p.velocityY *= 0.96f;
                    p.offsetY += p.velocityY;
                }
            }
        }

        private void spawnRandomFirework() {
            // Random position on screen with some margin from edges
            int margin = 50;
            double x = margin + random.nextDouble() * (Constants.WINDOW_WIDTH - 2 * margin);
            double y = margin + random.nextDouble() * (Constants.WINDOW_HEIGHT - 2 * margin);

            // Random colors for variety
            Color[] colors = {
                    Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                    Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK,
                    new Color(255, 100, 100), // Light red
                    new Color(100, 255, 100), // Light green
                    new Color(100, 100, 255), // Light blue
                    new Color(255, 255, 100), // Light yellow
                    new Color(255, 100, 255), // Light magenta
                    new Color(100, 255, 255)  // Light cyan
            };

            Color randomColor = colors[random.nextInt(colors.length)];

            // Random particle count for variety
            int particleCount = 500; //40 + random.nextInt(60); // 40-100 particles

            fireworkManager.createExplosion(x, y, randomColor, particleCount);
            shockwaves.add(new Shockwave((float)x, (float)y));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            frameCount++;
            long now = System.currentTimeMillis();
            if (now - lastTime >= 1000) {
                currentFPS = frameCount;
                frameCount = 0;
                lastTime = now;
            }

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Enable antialiasing for smoother particles
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw grid
            g2d.setColor(new Color(0, 100, 255)); // Blue grid

            for (int row = 0; row < grid.size(); row++) {
                List<GridPoint> line = grid.get(row);
                for (int col = 0; col < line.size(); col++) {
                    GridPoint p = line.get(col);

                    // Horizontal
                    if (col < line.size() - 1) {
                        GridPoint next = line.get(col + 1);
                        g2d.drawLine((int)p.getDrawX(), (int)p.getDrawY(), (int)next.getDrawX(), (int)next.getDrawY());
                    }

                    // Vertical
                    if (row < grid.size() - 1) {
                        GridPoint below = grid.get(row + 1).get(col);
                        g2d.drawLine((int)p.getDrawX(), (int)p.getDrawY(), (int)below.getDrawX(), (int)below.getDrawY());
                    }
                }
            }

            // Render fireworks first (behind player)
            fireworkManager.render(g2d);

            // Render player
            int px = (int) playerX;
            int py = (int) playerY;
            int size = playerSize;

            g2d.translate(px, py);
            g2d.rotate(playerAngle);

            int[] xPoints = { size, -size / 2, -size / 2 };
            int[] yPoints = { 0, -size / 2, size / 2 };

            g2d.setColor(Color.BLUE);
            g2d.fillPolygon(xPoints, yPoints, 3);

            g2d.dispose();

            if (showDebugOverlay) {
                g.setColor(Color.BLACK);
                String coords = "Mouse: (" + mousePos.x + ", " + mousePos.y + ")";
                g.setFont(new Font("Dialog", Font.PLAIN, 14));
                g.drawString(coords, getWidth() - 130, 20);

                g.setColor(Color.BLUE);
                g.drawString("FPS: " + currentFPS, 10, 20);

                // Show time until next firework
                double timeLeft = nextFireworkTime - fireworkTimer;
                g.drawString("Next firework in: " + String.format("%.1f", timeLeft) + "s", 10, 40);
            }
        }
    }

    public static GamePanel getGamePanel() {
        return gamePanel;
    }

    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}