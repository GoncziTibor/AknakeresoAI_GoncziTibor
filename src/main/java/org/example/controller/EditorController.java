package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.example.db.DatabaseManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class EditorController {

    @FXML
    private GridPane grid;

    @FXML
    private TextField usernameField;

    private final int SIZE = 10;
    private boolean[][] mineMap = new boolean[SIZE][SIZE];

    @FXML
    public void initialize() {
        usernameField.setText("");
        mineMap = new boolean[SIZE][SIZE];
        grid.getChildren().clear();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Button cell = new Button();
                cell.setPrefSize(40, 40);
                int r = row, c = col;
                cell.setOnAction(e -> toggleMine(cell, r, c));
                grid.add(cell, col, row);
            }
        }
    }

    private void toggleMine(Button cell, int row, int col) {
        mineMap[row][col] = !mineMap[row][col];
        cell.setStyle(mineMap[row][col] ? "-fx-background-color: red;" : "");
    }

    @FXML
    private void handleSave() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            System.out.println("Felhasználónév szükséges!");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(username + "_map.txt"))) {
            writer.write(SIZE + " " + SIZE + "\n");
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    writer.write((mineMap[i][j] ? "1" : "0") + " ");
                }
                writer.newLine();
            }
            System.out.println("Pálya elmentve: " + username + "_map.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoad() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            System.out.println("Felhasználónév szükséges a betöltéshez!");
            return;
        }

        String[] data = DatabaseManager.loadMapFromDatabase(username);
        if (data == null) {
            System.out.println("Nem található pálya az adatbázisban ezzel a névvel.");
            return;
        }

        String[] rows = data[0].split(";");
        for (int i = 0; i < rows.length && i < SIZE; i++) {
            String[] cells = rows[i].split(",");
            for (int j = 0; j < cells.length && j < SIZE; j++) {
                mineMap[i][j] = cells[j].equals("1");
                Button button = getButtonAt(i, j);
                button.setStyle(mineMap[i][j] ? "-fx-background-color: red;" : "");
            }
        }

        System.out.println("Pálya betöltve az adatbázisból: " + data[1]);
    }

    @FXML
    private void startGame() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            System.out.println("Felhasználónév szükséges a játékhoz!");
            return;
        }

        handleSave();

        try {
            Files.copy(
                    Path.of(username + "_map.txt"),
                    Path.of("player_map.txt"),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Nem sikerült átmásolni a pályát.");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/game.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());

            GameController controller = loader.getController();
            controller.initData(username, username + "_map");

            org.example.model.GameStateData state = DatabaseManager.loadFullGameFromDatabase(username);
            if (state != null) {
                controller.initFullState(state);
            }

            javafx.stage.Stage currentStage = (javafx.stage.Stage) grid.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("Játék");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Button getButtonAt(int row, int col) {
        for (javafx.scene.Node node : grid.getChildren()) {
            Integer r = GridPane.getRowIndex(node);
            Integer c = GridPane.getColumnIndex(node);
            if (r != null && c != null && r == row && c == col && node instanceof Button) {
                return (Button) node;
            }
        }
        return null;
    }
}