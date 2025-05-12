package org.example.model;

public class MinimaxAI {

    private static final int MAX_DEPTH = 3;

    public static Move findBestMove(GameState state) {
        int bestValue = Integer.MIN_VALUE;
        Move bestMove = null;

        for (Move move : state.getAvailableMoves()) {
            GameState nextState = state.simulateMove(move, true);
            int value = minimax(nextState, MAX_DEPTH - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        System.out.println("AI lépés kiválasztva: " + (bestMove == null ? "nincs érvényes lépés" : bestMove.row + "," + bestMove.col));


        return bestMove;
    }

    private static int minimax(GameState state, int depth, boolean isMaximizing, int alpha, int beta) {
        if (depth == 0 || state.isGameOver) {
            return state.evaluate();
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : state.getAvailableMoves()) {
                GameState next = state.simulateMove(move, true);
                int eval = minimax(next, depth - 1, false, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : state.getAvailableMoves()) {
                GameState next = state.simulateMove(move, false);
                int eval = minimax(next, depth - 1, true, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }
}