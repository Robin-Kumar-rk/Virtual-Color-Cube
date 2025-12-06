package com.colorcube.ui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.colorcube.model.CubeModel;
import com.colorcube.model.Face;
import com.colorcube.model.Move;

public class Cube3DPanel extends JPanel {
    private CubeModel model;
    private double pitch = -30; // Rotation around X axis
    private double yaw = 45; // Rotation around Y axis
    private double scale = 100; // Zoom level

    private int lastX, lastY;

    // Animation state
    private Move currentMove;
    private double animProgress; // 0.0 to 1.0
    private Timer animTimer;
    private Runnable onAnimComplete;
    private static final int ANIM_DURATION = 200; // ms

    public Cube3DPanel(CubeModel model) {
        this.model = model;
        setBackground(Color.DARK_GRAY);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastX;
                int dy = e.getY() - lastY;

                yaw += dx * 0.5;
                pitch += dy * 0.5;

                lastX = e.getX();
                lastY = e.getY();
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                scale -= e.getPreciseWheelRotation() * 5;
                if (scale < 20)
                    scale = 20;
                if (scale > 300)
                    scale = 300;
                repaint();
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    public void animateMove(Move move, Runnable onComplete) {
        if (animTimer != null && animTimer.isRunning())
            return;

        this.currentMove = move;
        this.onAnimComplete = onComplete;
        this.animProgress = 0;

        animTimer = new Timer(15, new ActionListener() {
            long startTime = -1;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (startTime == -1)
                    startTime = System.currentTimeMillis();
                long elapsed = System.currentTimeMillis() - startTime;
                animProgress = (double) elapsed / ANIM_DURATION;

                if (animProgress >= 1.0) {
                    animProgress = 1.0;
                    animTimer.stop();
                    currentMove = null;
                    if (onAnimComplete != null)
                        onAnimComplete.run();
                }
                repaint();
            }
        });
        animTimer.start();
    }

    public boolean isAnimating() {
        return animTimer != null && animTimer.isRunning();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Center of screen
        double cx = w / 2.0;
        double cy = h / 2.0;

        List<FaceletQuad> quads = new ArrayList<>();

        // Generate 27 cubies
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    addCubie(quads, x, y, z);
                }
            }
        }

        // Sort by Z (Painter's algorithm)
        Collections.sort(quads);

        // Draw
        for (FaceletQuad q : quads) {
            drawQuad(g2, q, cx, cy);
        }
    }

    private void addCubie(List<FaceletQuad> quads, int x, int y, int z) {
        // Determine if this cubie is affected by animation
        boolean animating = false;
        if (currentMove != null) {
            switch (currentMove.getFace()) {
                case U:
                    if (y == -1)
                        animating = true;
                    break;
                case D:
                    if (y == 1)
                        animating = true;
                    break;
                case L:
                    if (x == -1)
                        animating = true;
                    break;
                case R:
                    if (x == 1)
                        animating = true;
                    break;
                case F:
                    if (z == 1)
                        animating = true;
                    break;
                case B:
                    if (z == -1)
                        animating = true;
                    break;
            }
        }

        // Add 6 faces for this cubie
        addCubieFace(quads, x, y, z, Face.U, animating);
        addCubieFace(quads, x, y, z, Face.D, animating);
        addCubieFace(quads, x, y, z, Face.F, animating);
        addCubieFace(quads, x, y, z, Face.B, animating);
        addCubieFace(quads, x, y, z, Face.L, animating);
        addCubieFace(quads, x, y, z, Face.R, animating);
    }

    private void addCubieFace(List<FaceletQuad> quads, int x, int y, int z, Face face, boolean animating) {
        // Determine color
        Color color = Color.BLACK;
        String faceletsStr = model.getFaceletString();

        // Check if this face is on the surface
        boolean isSurface = false;
        int index = -1;

        switch (face) {
            case U:
                if (y == -1) {
                    isSurface = true;
                    index = (z + 1) * 3 + (x + 1); // 0-8
                }
                break;
            case D:
                if (y == 1) {
                    isSurface = true;
                    index = 27 + (1 - z) * 3 + (x + 1); // 27-35
                }
                break;
            case F:
                if (z == 1) {
                    isSurface = true;
                    index = 18 + (y + 1) * 3 + (x + 1); // 18-26
                }
                break;
            case B:
                if (z == -1) {
                    isSurface = true;
                    index = 45 + (y + 1) * 3 + (1 - x); // 45-53
                }
                break;
            case L:
                if (x == -1) {
                    isSurface = true;
                    index = 36 + (y + 1) * 3 + (z + 1); // 36-44
                }
                break;
            case R:
                if (x == 1) {
                    isSurface = true;
                    index = 9 + (y + 1) * 3 + (1 - z); // 9-17
                }
                break;
        }

        if (isSurface && index >= 0 && index < 54) {
            color = getColor(faceletsStr.charAt(index));
        }

        // Geometry
        double cx = x, cy = y, cz = z;
        double uX = 0, uY = 0, uZ = 0;
        double vX = 0, vY = 0, vZ = 0;

        // Face normal and basis vectors
        switch (face) {
            case U: // Up (y-)
                cy -= 0.5;
                uX = 1;
                vZ = 1;
                break;
            case D: // Down (y+)
                cy += 0.5;
                uX = 1;
                vZ = -1;
                break;
            case F: // Front (z+)
                cz += 0.5;
                uX = 1;
                vY = 1;
                break;
            case B: // Back (z-)
                cz -= 0.5;
                uX = -1;
                vY = 1;
                break;
            case L: // Left (x-)
                cx -= 0.5;
                uZ = 1;
                vY = 1;
                break;
            case R: // Right (x+)
                cx += 0.5;
                uZ = -1;
                vY = 1;
                break;
        }

        Point3D[] verts = new Point3D[4];
        // P = C + a*U + b*V where a,b in {-0.5, 0.5}
        for (int i = 0; i < 4; i++) {
            double a = (i == 1 || i == 2) ? 0.5 : -0.5;
            double b = (i == 2 || i == 3) ? 0.5 : -0.5;

            // Calculate vertex position
            // uX, uY, uZ are components of U vector
            // vX, vY, vZ are components of V vector
            double vx = cx + a * uX + b * vX;
            double vy = cy + a * uY + b * vY;
            double vz = cz + a * uZ + b * vZ;

            verts[i] = new Point3D(vx, vy, vz);
        }

        if (animating) {
            for (int i = 0; i < 4; i++) {
                verts[i] = applyAnimationTransform(verts[i]);
            }
        }

        quads.add(new FaceletQuad(verts, color));
    }

    private Point3D applyAnimationTransform(Point3D p) {
        // Apply rotation
        double angle = 90.0 * currentMove.getDir() * animProgress;
        // Axis of rotation
        double ax = 0, ay = 0, az = 0;
        switch (currentMove.getFace()) {
            case U:
                ay = 1;
                break;
            case D:
                ay = 1;
                break;
            case R:
                ax = 1;
                break;
            case L:
                ax = 1;
                break;
            case F:
                az = 1;
                break;
            case B:
                az = 1;
                break;
        }

        // Fix angles based on previous derivation
        switch (currentMove.getFace()) {
            case U:
                angle = -90.0 * currentMove.getDir() * animProgress;
                break;
            case D:
                angle = 90.0 * currentMove.getDir() * animProgress;
                break;
            case R:
                angle = 90.0 * currentMove.getDir() * animProgress;
                break;
            case L:
                angle = -90.0 * currentMove.getDir() * animProgress;
                break;
            case F:
                angle = 90.0 * currentMove.getDir() * animProgress;
                break;
            case B:
                angle = -90.0 * currentMove.getDir() * animProgress;
                break;
        }

        return rotatePoint(p, ax, ay, az, angle);
    }

    private Point3D rotatePoint(Point3D p, double ax, double ay, double az, double angleDeg) {
        double rad = Math.toRadians(angleDeg);
        double c = Math.cos(rad);
        double s = Math.sin(rad);

        double x = p.x, y = p.y, z = p.z;
        double nx = x, ny = y, nz = z;

        if (ax > 0.5) { // X axis
            ny = y * c - z * s;
            nz = y * s + z * c;
        } else if (ay > 0.5) { // Y axis
            nx = x * c + z * s;
            nz = -x * s + z * c;
        } else if (az > 0.5) { // Z axis
            nx = x * c - y * s;
            ny = x * s + y * c;
        }

        return new Point3D(nx, ny, nz);
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
                return new Color(255, 165, 0); // Orange
            case 'B':
                return new Color(30, 144, 255);
            default:
                return Color.GRAY;
        }
    }

    private void drawQuad(Graphics2D g2, FaceletQuad q, double cx, double cy) {
        GeneralPath path = new GeneralPath();
        Point3D p0 = project(q.verts[0]);
        path.moveTo(cx + p0.x, cy + p0.y);

        Point3D p2 = null;

        for (int i = 1; i < 4; i++) {
            Point3D p = project(q.verts[i]);
            path.lineTo(cx + p.x, cy + p.y);
            if (i == 2)
                p2 = p;
        }
        path.closePath();

        // Shine using GradientPaint from top-left (p0) to bottom-right (p2)
        if (p2 != null) {
            // Brighter color for shine
            Color c1 = q.color.brighter();
            GradientPaint gp = new GradientPaint(
                    (float) (cx + p0.x), (float) (cy + p0.y), c1,
                    (float) (cx + p2.x), (float) (cy + p2.y), q.color);
            g2.setPaint(gp);
        } else {
            g2.setColor(q.color);
        }

        g2.fill(path);
        g2.setColor(Color.BLACK);
        g2.draw(path);
    }

    private Point3D project(Point3D p) {
        // Rotate around X (pitch)
        double y1 = p.y * Math.cos(Math.toRadians(pitch)) - p.z * Math.sin(Math.toRadians(pitch));
        double z1 = p.y * Math.sin(Math.toRadians(pitch)) + p.z * Math.cos(Math.toRadians(pitch));

        // Rotate around Y (yaw)
        double x2 = p.x * Math.cos(Math.toRadians(yaw)) + z1 * Math.sin(Math.toRadians(yaw));
        double z2 = -p.x * Math.sin(Math.toRadians(yaw)) + z1 * Math.cos(Math.toRadians(yaw));

        return new Point3D(x2 * scale, y1 * scale, z2);
    }

    private static class Point3D {
        double x, y, z;

        Point3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private class FaceletQuad implements Comparable<FaceletQuad> {
        Point3D[] verts;
        Color color;

        FaceletQuad(Point3D[] verts, Color color) {
            this.verts = verts;
            this.color = color;
        }

        @Override
        public int compareTo(FaceletQuad o) {
            double z1 = getTransformedZ();
            double z2 = o.getTransformedZ();
            return Double.compare(z2, z1);
        }

        double getTransformedZ() {
            double sum = 0;
            for (Point3D p : verts) {
                Point3D proj = project(p);
                sum += proj.z;
            }
            return sum / 4.0;
        }
    }
}
