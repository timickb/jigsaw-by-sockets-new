package me.timickb.jigsaw.server.domain;

/**
 * Represents a game figure.
 */
public record Figure(boolean[][] cells) {
    public static final int MAX_SIZE = 3;


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < MAX_SIZE; ++i) {
            for (int j = 0; j < MAX_SIZE; ++j) {
                result.append(cells[i][j] ? "1#" : "0#");
            }
            result.append("&");
        }
        return result.toString();
    }
}