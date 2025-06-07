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

    // ------------------------------
    // Non-static Inner Classes (depend on GameCanvas instance)
    // ------------------------------
    public GameCanvas() {
        setTitle("GeoWars");
        setSize((int)Constants.WINDOW_WIDTH, (int)Constants.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Add the main game panel
        gamePanel = new GamePanel();
        add(gamePanel);

        // Set up input
        SwingUtilities.invokeLater(() -> {
            gamePanel.setFocusable(true);
            gamePanel.requestFocusInWindow();
            gamePanel.requestFocus();
        });

        // Show the window
        setVisible(true);

        // Start the game loop
        new GameLoop().start();
    }

    /**
     * Handles the main game rendering surface.
     * This panel draws game visuals based on game state.
     */
    class GamePanel extends JPanel {
        // Mouse coords
        private Point mousePos = new Point(0,0);
        private boolean showDebugOverlay = true;

        // FPS
        private int frameCount = 0;
        private int currentFPS = 0;
        private long lastTime = System.currentTimeMillis();

        // Keys pressed
        private final Set<Integer> pressedKeys = java.util.Collections.synchronizedSet(new HashSet<>());

        // Player
        private double playerX;
        private double playerY;
        private final int playerSize = 20;
        private double playerAngle = 0; // The current angle in radians
        private double targetAngle = 0; // where we want to face


        public GamePanel() {
            playerX = (Constants.WINDOW_WIDTH - playerSize) / 2;
            playerY = (Constants.WINDOW_HEIGHT - playerSize) / 2;

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mousePos = e.getPoint();
                    repaint();
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
            int speed = 200; // pixels per second
            double dx = 0;
            double dy = 0;

            if (pressedKeys.contains(KeyEvent.VK_W)) playerY -= (speed * delta);
            if (pressedKeys.contains(KeyEvent.VK_S)) playerY += (speed * delta);
            if (pressedKeys.contains(KeyEvent.VK_A)) playerX -= (speed * delta);
            if (pressedKeys.contains(KeyEvent.VK_D)) playerX += (speed * delta);

            if (pressedKeys.contains(KeyEvent.VK_W)) dy -= 1;
            if (pressedKeys.contains(KeyEvent.VK_S)) dy += 1;
            if (pressedKeys.contains(KeyEvent.VK_A)) dx -= 1;
            if (pressedKeys.contains(KeyEvent.VK_D)) dx += 1;

            double length = Math.sqrt(dx * dx + dy * dy);
            if (length != 0) {
                dx /= length;
                dy /= length;

                targetAngle = Math.atan2(dy, dx); // this gives angle in radians
                playerX += dx * speed * delta;
                playerY += dy * speed * delta;
            }

            double angleDiff = normalizeAngle(targetAngle - playerAngle);
            double rotationSpeed = 5.0; // radians per second
            playerAngle += clamp(angleDiff, -rotationSpeed * delta, rotationSpeed * delta);
        }

        // paintComponent() lives here
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

            // Player creation
            g.setColor(Color.BLUE);
            Graphics2D g2d = (Graphics2D) g.create();

            // Center of the player
                    int px = (int) playerX;
                    int py = (int) playerY;

            // Size of the triangle
                    int size = playerSize;

            // Translate and rotate around center
                    g2d.translate(px, py);
                    g2d.rotate(playerAngle);

            // Triangle points (pointing right by default)
                    int[] xPoints = { size, -size / 2, -size / 2 };
                    int[] yPoints = { 0, -size / 2, size / 2 };

            // Draw the triangle
                    g2d.setColor(Color.BLUE);
                    g2d.fillPolygon(xPoints, yPoints, 3);

            // Clean up
                    g2d.dispose();


            // Mouse Coordinates
            if (showDebugOverlay) {
                g.setColor(Color.BLACK);
                String coords = "Mouse: (" + mousePos.x + ", " + mousePos.y + ")";
                g.setFont(new Font("Dialog", Font.PLAIN, 14));
                g.drawString(coords, getWidth() - 130, 20);

                g.setFont(new Font("Dialog", Font.PLAIN, 14));
                g.setColor(Color.BLUE);
                g.drawString("FPS: " + currentFPS, 10, 20);
            }
        }
    }

    /**
     * Handles player input from the keyboard or mouse.
     */
    class InputHandler extends KeyAdapter {
        // keyPressed(), keyReleased(), etc.
    }

    /**
     * Manages scene transitions, menus, etc.
     */
    class SceneManager {
        // switchScene(), getCurrentScene(), etc.
    }

    // ------------------------------
    // Static Inner Classes (independent utilities)
    // ------------------------------

    /**
     * Static inner class for maintaining game-wide state or configuration.
     */
    static class GameState {
        // current level, score, flags like isPaused
    }

    /**
     * Static inner class for calculating deltas, FPS, frame limiting, etc.
     */
    static class Time {
        // deltaTime, frame timing calculations
    }

    /**
     * Static inner class for drawing reusable UI elements (health bars, etc.).
     */
    static class UIDrawer {
        // drawHealthBar(), drawScore(), drawFPS()
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
}
