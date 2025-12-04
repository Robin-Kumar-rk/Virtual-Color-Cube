package com.colorcube;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.colorcube.ui.MainFrame;

public class Main {
    public static void main(String[] args) {
        // Set up the UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Use system look and feel for a native look
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Initialize and show the main application window
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
