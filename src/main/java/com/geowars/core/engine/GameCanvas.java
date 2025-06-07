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

            if (pressedKeys.contains(KeyEvent.VK_W)) playerY -= (speed * delta);
            if (pressedKeys.contains(KeyEvent.VK_S)) playerY += (speed * delta);
            if (pressedKeys.contains(KeyEvent.VK_A)) playerX -= (speed * delta);
            if (pressedKeys.contains(KeyEvent.VK_D)) playerX += (speed * delta);
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
            g.fillOval((int)playerX, (int)playerY, playerSize, playerSize);

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
     * Static inner class for drawing reusable UI elements (health bars, etc).
     */
    static class UIDrawer {
        // drawHealthBar(), drawScore(), drawFPS()
    }

    public static GamePanel getGamePanel() {
        return gamePanel;
    }
}
