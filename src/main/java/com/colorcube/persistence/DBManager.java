package com.colorcube.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private static final String DB_URL = "jdbc:sqlite:cube_practice.db";

    public DBManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS saved_progress (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "facelet_string TEXT NOT NULL," +
                    "move_history TEXT," +
                    "elapsed_ms INTEGER DEFAULT 0," +
                    "is_user_filled BOOLEAN DEFAULT 0," +
                    "thumbnail BLOB," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            stmt.execute(sql);

            String metaSql = "CREATE TABLE IF NOT EXISTS metadata (" +
                    "key TEXT PRIMARY KEY," +
                    "value TEXT" +
                    ");";
            stmt.execute(metaSql);

        } catch (SQLException e) {
            e.printStackTrace();
            // In a real app, we might show a dialog here
        }
    }

    public void saveProgress(String name, String facelets, String moveHistory, long elapsedMs, boolean isUserFilled,
            byte[] thumbnail) {
        String sql = "INSERT INTO saved_progress(name, facelet_string, move_history, elapsed_ms, is_user_filled, thumbnail) VALUES(?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, facelets);
            pstmt.setString(3, moveHistory);
            pstmt.setLong(4, elapsedMs);
            pstmt.setBoolean(5, isUserFilled);
            pstmt.setBytes(6, thumbnail);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteProgress(int id) {
        String sql = "DELETE FROM saved_progress WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<SavedSession> loadAllProgress() {
        List<SavedSession> sessions = new ArrayList<>();
        String sql = "SELECT id, name, facelet_string, move_history, elapsed_ms, is_user_filled, created_at FROM saved_progress ORDER BY created_at DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SavedSession s = new SavedSession();
                s.id = rs.getInt("id");
                s.name = rs.getString("name");
                s.faceletString = rs.getString("facelet_string");
                s.moveHistory = rs.getString("move_history");
                s.elapsedMs = rs.getLong("elapsed_ms");
                s.isUserFilled = rs.getBoolean("is_user_filled");
                s.createdAt = rs.getString("created_at");
                sessions.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sessions;
    }

    // Simple DTO for saved session
    public static class SavedSession {
        public int id;
        public String name;
        public String faceletString;
        public String moveHistory;
        public long elapsedMs;
        public boolean isUserFilled;
        public String createdAt;

        @Override
        public String toString() {
            return name + " (" + createdAt + ")";
        }
    }

    public void saveKeyBinding(com.colorcube.model.Face face, char key) {
        String k = "KEY_" + face.name();
        String v = String.valueOf(key);
        String sql = "INSERT OR REPLACE INTO metadata(key, value) VALUES(?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, k);
            pstmt.setString(2, v);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public java.util.Map<com.colorcube.model.Face, Character> loadKeyBindings() {
        java.util.Map<com.colorcube.model.Face, Character> bindings = new java.util.HashMap<>();
        String sql = "SELECT key, value FROM metadata WHERE key LIKE 'KEY_%'";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String k = rs.getString("key");
                String v = rs.getString("value");
                if (v != null && !v.isEmpty()) {
                    try {
                        String faceName = k.substring(4); // Remove "KEY_"
                        com.colorcube.model.Face face = com.colorcube.model.Face.valueOf(faceName);
                        bindings.put(face, v.charAt(0));
                    } catch (IllegalArgumentException e) {
                        // Ignore invalid face names
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bindings;
    }
}
