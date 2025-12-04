package com.colorcube.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.colorcube.model.CubeModel;
import com.colorcube.model.Face;

public class CubeNetPanel extends JPanel {
    private CubeModel model;
    private int cellSize = 30;
    private Map<Face, Character> faceKeys;
    private BiConsumer<Face, Character> onKeyChange;

    public CubeNetPanel(CubeModel model, Map<Face, Character> faceKeys, BiConsumer<Face, Character> onKeyChange) {
        this.model = model;
        this.faceKeys = faceKeys;
        this.onKeyChange = onKeyChange;
        setPreferredSize(new Dimension(400, 300));
        setBackground(Color.LIGHT_GRAY);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    private void handleMouseClick(int x, int y) {
        // Check if a center facelet was clicked
        checkCenterClick(x, y, Face.U, 3, 0);
        checkCenterClick(x, y, Face.L, 0, 3);
        checkCenterClick(x, y, Face.F, 3, 3);
        checkCenterClick(x, y, Face.R, 6, 3);
        checkCenterClick(x, y, Face.B, 9, 3);
        checkCenterClick(x, y, Face.D, 3, 6);
    }

    private void checkCenterClick(int x, int y, Face face, int gridX, int gridY) {
        int baseX = 20 + gridX * cellSize;
        int baseY = 20 + gridY * cellSize;

        // Center is at (1,1) in the 3x3 grid
        int centerX = baseX + cellSize;
        int centerY = baseY + cellSize;

        if (x >= centerX && x < centerX + cellSize && y >= centerY && y < centerY + cellSize) {
            // Center clicked
            String input = JOptionPane.showInputDialog(this, "Enter key for " + face + " face (a-z, 0-9):");
            if (input != null && input.length() == 1) {
                char key = Character.toLowerCase(input.charAt(0));
                if (Character.isLetterOrDigit(key)) {
                    onKeyChange.accept(face, key);
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid key. Please enter a letter or digit.");
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the net
        drawFace(g, Face.U, 3, 0);
        drawFace(g, Face.L, 0, 3);
        drawFace(g, Face.F, 3, 3);
        drawFace(g, Face.R, 6, 3);
        drawFace(g, Face.B, 9, 3);
        drawFace(g, Face.D, 3, 6);
    }

    private void drawFace(Graphics g, Face face, int gridX, int gridY) {
        int baseX = 20 + gridX * cellSize;
        int baseY = 20 + gridY * cellSize;

        String s = model.getFaceletString();
        int offset = face.ordinal() * 9;

        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int x = baseX + c * cellSize;
                int y = baseY + r * cellSize;

                char colorCode = s.charAt(offset + r * 3 + c);
                g.setColor(getColor(colorCode));
                g.fillRect(x, y, cellSize, cellSize);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, cellSize, cellSize);

                // Draw key on center
                if (r == 1 && c == 1 && faceKeys != null) {
                    Character key = faceKeys.get(face);
                    if (key != null) {
                        String keyStr = String.valueOf(key).toUpperCase();
                        int textW = fm.stringWidth(keyStr);
                        int textH = fm.getAscent();

                        // Contrast color
                        g.setColor(
                                getColor(colorCode) == Color.WHITE || getColor(colorCode) == Color.YELLOW ? Color.BLACK
                                        : Color.WHITE);
                        g.drawString(keyStr, x + (cellSize - textW) / 2, y + (cellSize + textH) / 2 - 4);
                    }
                }
            }
        }
    }

    private Color getColor(char code) {
        switch (code) {
            case 'W':
                return Color.WHITE;
            case 'R':
                return Color.RED;
            case 'G':
                return Color.GREEN;
            case 'Y':
                return Color.YELLOW;
            case 'O':
                return new Color(255, 165, 0);
            case 'B':
                return Color.BLUE;
            default:
                return Color.GRAY;
        }
    }
}
