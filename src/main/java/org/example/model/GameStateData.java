package org.example.model;

public class GameStateData {
    public final String palyaAdat;
    public final String revealedAdat;
    public final int playerScore;
    public final int aiScore;

    public GameStateData(String palyaAdat, String revealedAdat, int playerScore, int aiScore) {
        this.palyaAdat = palyaAdat;
        this.revealedAdat = revealedAdat;
        this.playerScore = playerScore;
        this.aiScore = aiScore;
    }
}