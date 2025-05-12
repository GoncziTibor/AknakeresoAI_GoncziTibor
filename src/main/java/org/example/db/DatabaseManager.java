package org.example.db;

import org.example.model.GameStateData;

import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/aknakereso";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void saveGame(String felhasznalo, String palyaNev, boolean[][] mineMap, boolean[][] revealedMap, int jatekosPont, int aiPont) {
        StringBuilder mapBuilder = new StringBuilder();
        StringBuilder revealedBuilder = new StringBuilder();

        for (int i = 0; i < mineMap.length; i++) {
            for (int j = 0; j < mineMap[i].length; j++) {
                mapBuilder.append(mineMap[i][j] ? "1" : "0").append(",");
                revealedBuilder.append(revealedMap[i][j] ? "1" : "0").append(",");
            }
            mapBuilder.append(";");
            revealedBuilder.append(";");
        }

        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO jatek (felhasznalo, palya_nev, palya_adat, revealed_adat, jatekos_pont, ai_pont) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, felhasznalo);
                stmt.setString(2, palyaNev);
                stmt.setString(3, mapBuilder.toString());
                stmt.setString(4, revealedBuilder.toString());
                stmt.setInt(5, jatekosPont);
                stmt.setInt(6, aiPont);
                stmt.executeUpdate();
                System.out.println("Teljes játék mentve az adatbázisba.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String[] loadMapFromDatabase(String felhasznalo) {
        String query = "SELECT palya_adat, palya_nev FROM jatek WHERE felhasznalo = ? ORDER BY datum DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, felhasznalo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String mapData = rs.getString("palya_adat");
                String palyaNev = rs.getString("palya_nev");
                return new String[]{mapData, palyaNev};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static GameStateData loadFullGameFromDatabase(String felhasznalo) {
        String query = "SELECT palya_adat, revealed_adat, jatekos_pont, ai_pont FROM jatek WHERE felhasznalo = ? ORDER BY datum DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, felhasznalo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String palya = rs.getString("palya_adat");
                String revealed = rs.getString("revealed_adat");
                int player = rs.getInt("jatekos_pont");
                int ai = rs.getInt("ai_pont");

                return new GameStateData(palya, revealed, player, ai);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}