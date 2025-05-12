package org.example.controller;

import org.example.model.GameState;
import org.example.model.MinimaxAI;
import org.example.model.Move;
import org.example.db.DatabaseManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GameController {

    @FXML
    private GridPane gameGrid;

    @FXML
    private Label statusLabel;

    private final int SIZE = 10;
    private boolean[][] mineMap = new boolean[SIZE][SIZE];
    private boolean[][] revealed = new boolean[SIZE][SIZE];
    private Button[][] buttons = new Button[SIZE][SIZE];

    private boolean gameOver = false;
    private boolean playerTurn = true;

    private int playerScore = 0;
    private int aiScore = 0;

    private String felhasznaloNev;
    private String palyaNev;
    private boolean initialized = false;

    @FXML
    public void initialize() {
        if (!initialized) {
            loadMapFromFile("player_map.txt");
            setupBoard();
            statusLabel.setText("Te kezded! Kattints egy mezőre.");
        }
    }

    public void initData(String felhasznaloNev, String palyaNev) {
        this.felhasznaloNev = felhasznaloNev;
        this.palyaNev = palyaNev;
    }

    public void initFullState(org.example.model.GameStateData data) {
        String[] mapRows = data.palyaAdat.split(";");
        String[] revRows = data.revealedAdat.split(";");

        for (int i = 0; i < SIZE && i < mapRows.length; i++) {
            String[] mapCols = mapRows[i].split(",");
            String[] revCols = revRows[i].split(",");
            for (int j = 0; j < SIZE && j < mapCols.length; j++) {
                mineMap[i][j] = mapCols[j].equals("1");
                revealed[i][j] = revCols[j].equals("1");
            }
        }

        this.playerScore = data.playerScore;
        this.aiScore = data.aiScore;

        setupBoard();

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (revealed[i][j]) {
                    Button btn = buttons[i][j];
                    btn.setDisable(true);
                    if (mineMap[i][j]) {
                        btn.setStyle("-fx-background-color: red;");
                    } else {
                        int count = countAdjacentMines(i, j);
                        btn.setText(String.valueOf(count));
                        btn.setStyle("-fx-background-color: lightgray;");
                    }
                }
            }
        }

        if (data.playerScore + data.aiScore == SIZE * SIZE || isGameOverState()) {
            statusLabel.setText("🔒 A játék már véget ért. Nézet módban vagy.");
            gameOver = true;
            disableBoard();
        } else {
            statusLabel.setText("Folytatás betöltve! Te jössz!");
            playerTurn = true;
        }

        initialized = true;
    }

    private boolean isGameOverState() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (revealed[i][j] && mineMap[i][j]) {
                    return true;
                }
            }
        }
        return false;
    }

    private void loadMapFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine();
            for (int i = 0; i < SIZE; i++) {
                String[] row = reader.readLine().split(" ");
                for (int j = 0; j < SIZE; j++) {
                    mineMap[i][j] = row[j].equals("1");
                }
            }
        } catch (IOException e) {
            statusLabel.setText("Nem sikerült betölteni a pályát.");
        }
    }

    private void setupBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Button btn = new Button();
                btn.setPrefSize(40, 40);
                int r = row, c = col;
                btn.setOnAction(e -> handlePlayerMove(btn, r, c));
                gameGrid.add(btn, col, row);
                buttons[row][col] = btn;
            }
        }
    }

    private void handlePlayerMove(Button btn, int row, int col) {
        if (gameOver || revealed[row][col] || !playerTurn) return;

        processMove(btn, row, col, true);
        playerTurn = false;

        if (!gameOver) {
            statusLabel.setText("Gép lép...");
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    if (!gameOver) {
                        handleAIMove();
                    }
                });
            }).start();
        }
    }

    private void handleAIMove() {
        if (gameOver) return;

        int[] move = getBestMove();
        if (move == null) {
            gameOver = true;
            statusLabel.setText("A gép nem talált érvényes lépést. Játék vége.\nEredmény: Játékos " + playerScore + " | Gép " + aiScore);
            disableBoard();
            return;
        }

        int row = move[0], col = move[1];
        Button btn = buttons[row][col];
        processMove(btn, row, col, false);
        playerTurn = true;

        if (!gameOver) {
            statusLabel.setText("Te jössz! Pontok: Játékos: " + playerScore + " | Gép: " + aiScore);
        }
    }

    private void processMove(Button btn, int row, int col, boolean isPlayer) {
        revealed[row][col] = true;
        btn.setDisable(true);

        if (mineMap[row][col]) {
            btn.setStyle("-fx-background-color: red;");
            gameOver = true;
            String who = isPlayer ? "Játékos" : "Gép";
            statusLabel.setText("💥 " + who + " aknára lépett. Vége a játéknak!\nEredmény: Játékos " + playerScore + " | Gép " + aiScore);
            disableBoard();
        } else {
            int count = countAdjacentMines(row, col);
            btn.setText(String.valueOf(count));
            if (isPlayer) {
                btn.setStyle("-fx-background-color: lightgreen;");
                playerScore++;
            } else {
                btn.setStyle("-fx-background-color: lightblue;");
                aiScore++;
            }
        }
    }

    private int[] getBestMove() {
        GameState currentState = new GameState(mineMap, revealed, playerScore, aiScore, gameOver);
        Move best = MinimaxAI.findBestMove(currentState);

        if (best != null) {
            return new int[]{best.row, best.col};
        }

        List<int[]> possibleMoves = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (!revealed[i][j]) {
                    possibleMoves.add(new int[]{i, j});
                }
            }
        }

        if (possibleMoves.isEmpty()) return null;
        return possibleMoves.get(new Random().nextInt(possibleMoves.size()));
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = row + dr;
                int nc = col + dc;
                if (nr >= 0 && nr < SIZE && nc >= 0 && nc < SIZE) {
                    if (mineMap[nr][nc]) count++;
                }
            }
        }
        return count;
    }

    private void disableBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j].setDisable(true);
            }
        }
    }

    @FXML
    private void handleNewGame() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/editor.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            javafx.stage.Stage stage = (javafx.stage.Stage) gameGrid.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Aknakereső AI - Pályaszerkesztő");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveToDatabase() {
        if (felhasznaloNev == null || palyaNev == null) {
            System.out.println("Hiányzó adatok a mentéshez!");
            return;
        }
        DatabaseManager.saveGame(felhasznaloNev, palyaNev, mineMap, revealed, playerScore, aiScore);
    }
}