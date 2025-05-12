package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public static final int SIZE = 10;

    public boolean[][] mineMap;
    public boolean[][] revealed;
    public int playerScore;
    public int aiScore;
    public boolean isGameOver;

    public GameState(boolean[][] mineMap, boolean[][] revealed, int playerScore, int aiScore, boolean isGameOver) {
        this.mineMap = deepCopy(mineMap);
        this.revealed = deepCopy(revealed);
        this.playerScore = playerScore;
        this.aiScore = aiScore;
        this.isGameOver = isGameOver;
    }

    public List<Move> getAvailableMoves() {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (!revealed[i][j]) {
                    moves.add(new Move(i, j));
                }
            }
        }
        return moves;
    }

    public GameState simulateMove(Move move, boolean isAI) {
        boolean[][] newRevealed = deepCopy(revealed);
        boolean[][] newMineMap = mineMap;
        int newPlayerScore = playerScore;
        int newAiScore = aiScore;

        newRevealed[move.row][move.col] = true;

        if (mineMap[move.row][move.col]) {
            return new GameState(newMineMap, newRevealed, newPlayerScore, newAiScore, true);
        } else {
            int reward = countAdjacentMines(move.row, move.col, mineMap);
            if (isAI) newAiScore += reward;
            else newPlayerScore += reward;

            return new GameState(newMineMap, newRevealed, newPlayerScore, newAiScore, false);
        }
    }

    public int evaluate() {
        if (isGameOver) {
            return Integer.MIN_VALUE;
        }
        return aiScore - playerScore;
    }

    private boolean[][] deepCopy(boolean[][] original) {
        boolean[][] copy = new boolean[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    private int countAdjacentMines(int row, int col, boolean[][] map) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = row + dr;
                int nc = col + dc;
                if (nr >= 0 && nr < SIZE && nc >= 0 && nc < SIZE) {
                    if (map[nr][nc]) count++;
                }
            }
        }
        return count;
    }
}