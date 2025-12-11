package com.colorcube.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.colorcube.model.Face;

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

    public void saveProgress(String name, String facelets) {
        String sql = "INSERT INTO saved_progress(name, facelet_string) VALUES(?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, facelets);

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
        String sql = "SELECT id, name, facelet_string, created_at FROM saved_progress ORDER BY created_at DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SavedSession s = new SavedSession();
                s.id = rs.getInt("id");
                s.name = rs.getString("name");
                s.faceletString = rs.getString("facelet_string");
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
        public String createdAt;

        @Override
        public String toString() {
            return name + " (" + createdAt + ")";
        }
    }

    public void saveKeyBinding(Face face, char key) {
        String k = face.toString();
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

    public Map<Face, Character> loadKeyBindings() {
        Map<Face, Character> bindings = new HashMap<>();
        String sql = "SELECT key, value FROM metadata";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String k = rs.getString("key");
                String v = rs.getString("value");
                if (v != null && !v.isEmpty()) {
                    try {
                        Face face = Face.valueOf(k);
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
