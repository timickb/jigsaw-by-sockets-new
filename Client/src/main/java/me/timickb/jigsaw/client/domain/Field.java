package me.timickb.jigsaw.client.domain;

/**
 * Represents a game field.
 */
public class Field {
    public static final int SIZE = 9;
    public static final int CELL_SIZE = 40;

    private final boolean[][] cells;

    /**
     * Creates a game field.
     */
    public Field() {
        cells = new boolean[SIZE][SIZE];
    }

    /**
     * Get field cell value
     * @param row Row index
     * @param col Column index
     * @return Cell value
     */
    public boolean getCell(int row, int col) {
        return cells[row][col];
    }

    /**
     * Set field cell value
     * @param row Row index
     * @param col Column index
     * @param value Cell value
     */
    public void setCell(int row, int col, boolean value) {
        cells[row][col] = value;
    }

    /**
     * Sets all cells to value false.
     */
    public void clear() {
        for (int i = 0; i < SIZE; ++i) {
            for (int j = 0; j < SIZE; ++j) {
                cells[i][j] = false;
            }
        }
    }
}
