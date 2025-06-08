package com.geowars.core.engine;
import com.geowars.core.util.Constants;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashSet;
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