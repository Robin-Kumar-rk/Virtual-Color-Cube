package com.colorcube.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

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

    private DefaultListModel<DBManager.SavedSession> savedListModel;
    private JList<DBManager.SavedSession> savedList;

    private Map<Face, Character> faceKeys;

    public MainFrame() {
        super("3x3 Color Cube - Virtual Practice");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600); // Reduced height to prevent taskbar overlap
        setLocationRelativeTo(null);

        model = new CubeModel();
        dbManager = new DBManager();

        // Initialize default key bindings
        faceKeys = new HashMap<>();
        faceKeys.put(Face.U, 'u');
        faceKeys.put(Face.D, 'd');
        faceKeys.put(Face.L, 'l');
        faceKeys.put(Face.R, 'r');
        faceKeys.put(Face.F, 'f');
        faceKeys.put(Face.B, 'b');

        initUI();
        refreshSavedList();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Inner SplitPane (3D + Net)
        panel3D = new Cube3DPanel(model);
        panelNet = new CubeNetPanel(model, faceKeys, this::updateKeyBinding);

        JSplitPane innerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel3D, panelNet);
        innerSplitPane.setResizeWeight(0.625);

        // Right: Saved Progress
        JPanel rightPanel = new JPanel(new BorderLayout());
        // rightPanel.setPreferredSize(new Dimension(200, 0)); // Removed fixed size
        // preference
        rightPanel.add(new JLabel("Saved Progress"), BorderLayout.NORTH);

        savedListModel = new DefaultListModel<>();
        savedList = new JList<>(savedListModel) {
            @Override
            protected void processMouseEvent(java.awt.event.MouseEvent e) {
                if (e.getID() == java.awt.event.MouseEvent.MOUSE_PRESSED &&
                        javax.swing.SwingUtilities.isLeftMouseButton(e)) {

                    if (!hasFocus()) {
                        requestFocusInWindow();
                    }

                    int index = locationToIndex(e.getPoint());
                    if (index != -1) {
                        java.awt.Rectangle bounds = getCellBounds(index, index);
                        if (bounds != null && bounds.contains(e.getPoint())) {
                            // Clicked on an item
                            if (isSelectedIndex(index)) {
                                clearSelection();
                            } else {
                                setSelectedIndex(index);
                            }
                        } else {
                            // Clicked in white space (but within component bounds)
                            clearSelection();
                        }
                    } else {
                        clearSelection();
                    }

                    // Consume the event and DO NOT call super to prevent default UI handling
                    e.consume();
                    return;
                }
                super.processMouseEvent(e);
            }
        };
        savedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rightPanel.add(new JScrollPane(savedList), BorderLayout.CENTER);

        JPanel loadPanel = new JPanel(new FlowLayout());
        JButton btnLoad = new JButton("Load");
        btnLoad.addActionListener(e -> doLoad());
        loadPanel.add(btnLoad);

        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(e -> doDelete());
        loadPanel.add(btnDelete);

        rightPanel.add(loadPanel, BorderLayout.SOUTH);

        // Outer SplitPane (Inner + Right Panel)
        JSplitPane outerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, innerSplitPane, rightPanel);
        outerSplitPane.setResizeWeight(0.8); // Give most space to the cube view
        add(outerSplitPane, BorderLayout.CENTER);

        // Set initial divider locations for 50% / 30% / 20% split of 1200px width
        // Inner split: 600px (50%) for 3D, rest for Net
        // Outer split: 960px (80%) for Inner, 240px (20%) for Saved
        // Note: These might need to be wrapped in invokeLater if not realized,
        // but setting proportional location or int location often works if size is
        // known.
        // Let's try setting them directly.
        innerSplitPane.setDividerLocation(600);
        outerSplitPane.setDividerLocation(960);

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

        add(toolbar, BorderLayout.NORTH);

        // Key Bindings
        setupKeyBindings();
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
            dbManager.saveProgress(name, model.getFaceletString(), "", 0, false, null);
            refreshSavedList();
            JOptionPane.showMessageDialog(this, "Saved successfully!");
        }
    }

    private void doLoad() {
        if (panel3D.isAnimating())
            return;
        DBManager.SavedSession selected = savedList.getSelectedValue();
        if (selected != null) {
            model.setFacelets(selected.faceletString);
            // In a real app we would restore move history too
            refreshViews();
        }
    }

    private void doDelete() {
        if (panel3D.isAnimating())
            return;
        DBManager.SavedSession selected = savedList.getSelectedValue();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete session '" + selected.name + "'?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                dbManager.deleteProgress(selected.id);
                refreshSavedList();
            }
        }
    }

    private void refreshSavedList() {
        savedListModel.clear();
        List<DBManager.SavedSession> sessions = dbManager.loadAllProgress();
        for (DBManager.SavedSession s : sessions) {
            savedListModel.addElement(s);
        }
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
