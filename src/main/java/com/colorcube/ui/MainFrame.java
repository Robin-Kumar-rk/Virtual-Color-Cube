package com.colorcube.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import java.awt.Color;

import com.colorcube.core.Scrambler;
import com.colorcube.model.CubeModel;
import com.colorcube.model.Face;
import com.colorcube.model.Move;
import com.colorcube.persistence.DBManager;

public class MainFrame extends JFrame {
    private CubeModel model;
    private Cube3DPanel panel3D;
    private CubeNetPanel panelNet;
    private DBManager dbManager;

    private JPanel savedItemsPanel;
    private JSplitPane innerSplitPane;
    private JSplitPane outerSplitPane;
    private JPanel rightPanel;
    private boolean isSavedPanelVisible = false;

    private Map<Face, Character> faceKeys;

    public MainFrame() {
        super("3x3 Color Cube - Virtual Practice");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600); // Reduced height to prevent taskbar overlap
        setLocationRelativeTo(null);

        model = new CubeModel();
        dbManager = new DBManager();

        // Initialize key bindings from DB or defaults
        faceKeys = dbManager.loadKeyBindings();

        // Ensure all faces have a key
        faceKeys.putIfAbsent(Face.U, 'u');
        faceKeys.putIfAbsent(Face.D, 'd');
        faceKeys.putIfAbsent(Face.L, 'l');
        faceKeys.putIfAbsent(Face.R, 'r');
        faceKeys.putIfAbsent(Face.F, 'f');
        faceKeys.putIfAbsent(Face.B, 'b');

        initUI();
        refreshSavedList();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Inner SplitPane (3D + Net)
        panel3D = new Cube3DPanel(model);
        panelNet = new CubeNetPanel(model, faceKeys, this::updateKeyBinding);

        innerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel3D, panelNet);
        innerSplitPane.setResizeWeight(0.6); // 60/40 default when saved pane is hidden

        // Right: Saved Progress
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Saved Progress"), BorderLayout.NORTH);

        savedItemsPanel = new JPanel();
        savedItemsPanel.setLayout(new BoxLayout(savedItemsPanel, BoxLayout.Y_AXIS));

        // Wrapper to align items to the top
        JPanel topAlignedWrapper = new JPanel(new BorderLayout());
        topAlignedWrapper.add(savedItemsPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(topAlignedWrapper);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // Outer SplitPane (Inner + Right Panel)
        outerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, innerSplitPane, null); // Start hidden
        outerSplitPane.setResizeWeight(1.0); // Give all space to inner pane
        add(outerSplitPane, BorderLayout.CENTER);

        // Set initial divider locations
        SwingUtilities.invokeLater(() -> {
            innerSplitPane.setDividerLocation(0.6); // 60% of width
        });

        // Top: Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton btnScramble = createStyledButton("New Scramble", e -> doScramble());
        toolbar.add(btnScramble);

        toolbar.addSeparator(new Dimension(10, 0));

        JButton btnReset = createStyledButton("Reset", e -> {
            if (panel3D.isAnimating())
                return;
            model.reset();
            refreshViews();
        });
        toolbar.add(btnReset);

        toolbar.addSeparator(new Dimension(10, 0));

        JButton btnSave = createStyledButton("Save Progress", e -> doSave());
        toolbar.add(btnSave);

        toolbar.addSeparator(new Dimension(10, 0));

        JButton btnHelp = createStyledButton("Help", e -> showHelp());
        toolbar.add(btnHelp);

        // Add glue to push next component to right
        toolbar.add(Box.createHorizontalGlue());

        JButton btnToggleList = createStyledButton("Saved Progress", e -> toggleSavedPanel());
        toolbar.add(btnToggleList);

        add(toolbar, BorderLayout.NORTH);

        // Key Bindings
        setupKeyBindings();
    }

    private void toggleSavedPanel() {
        isSavedPanelVisible = !isSavedPanelVisible;
        if (isSavedPanelVisible) {
            outerSplitPane.setRightComponent(rightPanel);
            outerSplitPane.setResizeWeight(0.8);
            // 80% / 20%
            outerSplitPane.setDividerLocation(0.8);
            // 50% of total (which is 62.5% of 80%) for 3D
            innerSplitPane.setResizeWeight(0.625);
            innerSplitPane.setDividerLocation(0.625);
        } else {
            outerSplitPane.setRightComponent(null);
            outerSplitPane.setResizeWeight(1.0);
            // 60% / 40%
            innerSplitPane.setResizeWeight(0.6);
            innerSplitPane.setDividerLocation(0.6);
        }
        revalidate();
        repaint();
    }

    private void showHelp() {
        String helpText = "Controls:\n" +
                "- Rotate Cube: Click and drag on the 3D cube.\n" +
                "- Zoom: Mouse wheel.\n" +
                "- Scramble: Click 'New Scramble'.\n" +
                "- Reset: Click 'Reset'.\n\n" +
                "Keyboard Shortcuts:\n" +
                "- R, L, U, D, F, B: Clockwise rotations.\n" +
                "- Shift + Key: Counter-clockwise (Prime) rotations.\n" +
                "- Ctrl + Z: Undo.\n\n" +
                "Custom Keys:\n" +
                "- Click the center of any face in the 2D view to assign a custom key.\n\n" +
                "Sessions:\n" +
                "- Save: Click 'Save Progress'.\n" +
                "- Load/Delete: Select from the list and use buttons.";
        JOptionPane.showMessageDialog(this, helpText, "Help & Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createStyledButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.addActionListener(action);
        btn.setFocusPainted(false);
        btn.setFocusable(false); // Fix: Prevent button from retaining focus (blue background)
        btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btn.setMargin(new java.awt.Insets(5, 10, 5, 10));
        return btn;
    }

    private void updateKeyBinding(Face face, char newKey) {
        // Remove old binding
        char oldKey = faceKeys.get(face);
        JComponent content = (JComponent) this.getContentPane();
        // Remove old keystrokes from InputMap
        content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .remove(KeyStroke.getKeyStroke(Character.toUpperCase(oldKey), 0));
        content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .remove(KeyStroke.getKeyStroke(Character.toUpperCase(oldKey), KeyEvent.SHIFT_DOWN_MASK));

        // Update map
        faceKeys.put(face, newKey);
        dbManager.saveKeyBinding(face, newKey);

        // Add new binding
        bindFaceKey(content, face, newKey);
    }

    private void doScramble() {
        if (panel3D.isAnimating())
            return;
        List<Move> scramble = Scrambler.generateScramble(20);
        // Animate scramble? Too slow. Just apply.
        for (Move m : scramble) {
            model.applyMove(m);
        }
        refreshViews();
    }

    private void doSave() {
        if (panel3D.isAnimating())
            return;
        String name = JOptionPane.showInputDialog(this, "Enter name for this session:");
        if (name != null && !name.trim().isEmpty()) {
            dbManager.saveProgress(name, model.getFaceletString());
            refreshSavedList();
            JOptionPane.showMessageDialog(this, "Saved successfully!");
        }
    }

    private void doLoad(DBManager.SavedSession session) {
        if (panel3D.isAnimating())
            return;
        if (session != null) {
            model.setFacelets(session.faceletString);
            // In a real app we would restore move history too
            refreshViews();
        }
    }

    private void doDelete(DBManager.SavedSession session) {
        if (panel3D.isAnimating())
            return;
        if (session != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete session '" + session.name + "'?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                dbManager.deleteProgress(session.id);
                refreshSavedList();
            }
        }
    }

    private void refreshSavedList() {
        savedItemsPanel.removeAll();
        List<DBManager.SavedSession> sessions = dbManager.loadAllProgress();
        for (DBManager.SavedSession s : sessions) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            row.setBackground(Color.WHITE);
            // Increased height to accommodate name + buttons stacked
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
            row.setPreferredSize(new Dimension(300, 65));

            JLabel lblName = new JLabel("  " + s.toString());
            lblName.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            // Name at top
            row.add(lblName, BorderLayout.NORTH);

            // Buttons below, left aligned
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
            btnPanel.setOpaque(false);

            JButton btnRestore = new JButton("Load");
            btnRestore.setToolTipText("Load this session");
            btnRestore.setMargin(new java.awt.Insets(2, 8, 2, 8));
            btnRestore.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            btnRestore.addActionListener(e -> doLoad(s));

            JButton btnDelete = new JButton("Delete");
            btnDelete.setToolTipText("Delete this session");
            btnDelete.setMargin(new java.awt.Insets(2, 8, 2, 8));
            btnDelete.setForeground(Color.RED);
            btnDelete.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
            btnDelete.addActionListener(e -> doDelete(s));

            btnPanel.add(btnRestore);
            btnPanel.add(btnDelete);

            row.add(btnPanel, BorderLayout.CENTER);

            savedItemsPanel.add(row);
        }
        savedItemsPanel.revalidate();
        savedItemsPanel.repaint();
    }

    private void refreshViews() {
        panel3D.repaint();
        panelNet.repaint();
    }

    private void setupKeyBindings() {
        // Use InputMap/ActionMap on the RootPane for global shortcuts
        JComponent content = (JComponent) this.getContentPane();

        for (Face face : Face.values()) {
            bindFaceKey(content, face, faceKeys.get(face));
        }

        // Undo
        content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo");
        content.getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panel3D.isAnimating())
                    return;

                Move lastMove = model.getLastMove();
                if (lastMove != null) {
                    Move inverse = model.getInverse(lastMove);
                    panel3D.animateMove(inverse, () -> {
                        model.undo();
                        refreshViews();
                    });
                }
            }
        });
    }

    private void bindFaceKey(JComponent c, Face face, char key) {
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(key);
        // If extended key code fails (returns 0), try uppercase char cast
        if (keyCode == 0)
            keyCode = (int) Character.toUpperCase(key);

        // Move (Clockwise)
        Move move = Move.valueOf(face.name());
        String name = move.toString();
        c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0), name);
        c.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panel3D.isAnimating())
                    return;
                panel3D.animateMove(move, () -> {
                    model.applyMove(move);
                    refreshViews();
                });
            }
        });

        // Prime Move (Counter-Clockwise)
        Move movePrime = Move.valueOf(face.name() + "_PRIME");
        String namePrime = movePrime.toString();
        c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, KeyEvent.SHIFT_DOWN_MASK),
                namePrime);
        c.getActionMap().put(namePrime, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panel3D.isAnimating())
                    return;
                panel3D.animateMove(movePrime, () -> {
                    model.applyMove(movePrime);
                    refreshViews();
                });
            }
        });
    }
}
