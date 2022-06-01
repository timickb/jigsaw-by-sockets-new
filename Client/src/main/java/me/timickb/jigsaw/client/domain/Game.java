package me.timickb.jigsaw.client.domain;


import javafx.animation.Timeline;

/**
 * Represents the Jigsaw game.
 */
public class Game {
    private final Field field;
    private final Timeline timer;
    private Figure currentFigure;
    private boolean goingOn;
    private int score;
    private int seconds;

    public Game(Timeline timer) {
        field = new Field();
        this.timer = timer;
    }

    public int getScore() {
        return score;
    }

    /**
     * Starts the game.
     */
    public void start() {
        if (goingOn) return;

        goingOn = true;
        score = 0;
        seconds = 0;
        timer.play();
    }

    public void updateFigure(Figure figure) {
        currentFigure = figure;
    }

    /**
     * Checks the possibility to place currentFigure
     * with top-left cell in (fieldRpw, fieldCol).
     *
     * @param fieldRow Top-left field cell row index
     * @param fieldCol Top-left field cell column index
     * @return Possibility of placement.
     */
    public boolean placeFigure(int fieldRow, int fieldCol) {
        // Control that all cells were placed.
        int cellsPlaced = 0;
        // Check possibility
        for (int i = 0; i < Figure.MAX_SIZE; ++i) {
            for (int j = 0; j < Figure.MAX_SIZE; ++j) {
                if (currentFigure.getCell(i, j) && isBoundsValid(fieldRow + i, fieldCol + j)) {
                    if (field.getCell(fieldRow + i, fieldCol + j)) {
                        return false;
                    } else {
                        ++cellsPlaced;
                    }
                }
            }
        }

        if (cellsPlaced != currentFigure.getCellsCount()) {
            return false;
        }

        // Place
        for (int i = 0; i < Figure.MAX_SIZE; ++i) {
            for (int j = 0; j < Figure.MAX_SIZE; ++j) {
                if (currentFigure.getCell(i, j) && isBoundsValid(fieldRow + i, fieldCol + j)) {
                    field.setCell(fieldRow + i, fieldCol + j, true);
                }
            }
        }
        ++score;
        return true;
    }

    private boolean isBoundsValid(int row, int col) {
        return row >= 0 && col >= 0 && row < Field.SIZE && col < Field.SIZE;
    }

    /**
     * End the game.
     *
     * @return The game result.
     */
    public GameResult end() {
        if (!goingOn) return null;

        timer.stop();
        field.clear();
        goingOn = false;

        return new GameResult(score, seconds);
    }

    public boolean isGoingOn() {
        return goingOn;
    }

    public Figure getCurrentFigure() {
        return currentFigure;
    }

    public Field getField() {
        return field;
    }

    public void incTime() {
        ++seconds;
    }

    public int getSeconds() {
        return seconds;
    }
}