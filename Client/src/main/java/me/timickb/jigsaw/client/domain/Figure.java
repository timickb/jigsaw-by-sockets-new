package me.timickb.jigsaw.client.domain;

import me.timickb.jigsaw.client.domain.enums.FigureReflection;
import me.timickb.jigsaw.client.domain.enums.FigureRotation;

/**
 * Represents a game figure.
 */
public class Figure {
    public static final int MAX_SIZE = 3;

    private boolean[][] cells;

    public Figure() {
        cells = new boolean[MAX_SIZE][MAX_SIZE];
    }

    public Figure(boolean[][] cells) {
        this.cells = cells;
    }

    public void setCells(boolean[][] cells) {
        for (int i = 0; i < MAX_SIZE; ++i) {
            System.arraycopy(cells[i], 0, this.cells[i], 0, MAX_SIZE);
        }
    }

    /**
     * Counts filled cells.
     *
     * @return Count of filled cells.
     */
    public int getCellsCount() {
        int count = 0;
        for (int i = 0; i < MAX_SIZE; ++i) {
            for (int j = 0; j < MAX_SIZE; ++j) {
                if (cells[i][j]) {
                    ++count;
                }
            }
        }
        return count;
    }


    /**
     * @return Source cell array.
     */
    public boolean[][] getCells() {
        return cells;
    }


    /**
     * @param row    Row index
     * @param col    Column index
     * @param active Cell status
     */
    public void setCell(int row, int col, boolean active) {
        cells[row][col] = active;
    }

    /**
     * @param row Row index
     * @param col Cell index
     * @return Cell status
     */
    public boolean getCell(int row, int col) {
        return cells[row][col];
    }

    /**
     * Rotates the figure.
     *
     * @param rotation Rotation direction
     * @param times    Number of turns
     */
    public void rotate(FigureRotation rotation, int times) {
        for (int round = 0; round < times; ++round) {
            boolean[][] newCells = new boolean[MAX_SIZE][MAX_SIZE];

            if (rotation == FigureRotation.CLOCKWISE) {
                for (int i = 0; i < MAX_SIZE; ++i) {
                    for (int j = 0; j < MAX_SIZE; ++j) {
                        newCells[i][j] = cells[MAX_SIZE - j - 1][i];
                    }
                }
            }
            if (rotation == FigureRotation.ANTICLOCKWISE) {
                for (int i = 0; i < MAX_SIZE; ++i) {
                    for (int j = 0; j < MAX_SIZE; ++j) {
                        newCells[i][j] = cells[j][MAX_SIZE - i - 1];
                    }
                }
            }

            this.cells = newCells;
        }
    }

    /**
     * Geometrically reflects the figure
     *
     * @param reflection Reflection type
     */
    public void reflect(FigureReflection reflection) {
        boolean[][] newCells = new boolean[MAX_SIZE][MAX_SIZE];

        if (reflection == FigureReflection.HORIZONTAL) {
            for (int i = 0; i < MAX_SIZE; ++i) {
                for (int j = 0; j < MAX_SIZE; ++j) {
                    newCells[i][j] = cells[i][MAX_SIZE - j - 1];
                }
            }
        }
        if (reflection == FigureReflection.VERTICAL) {
            for (int i = 0; i < MAX_SIZE; ++i) {
                System.arraycopy(cells[MAX_SIZE - i - 1], 0, newCells[i], 0, MAX_SIZE);
            }
        }

        this.cells = newCells;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < MAX_SIZE; ++i) {
            for (int j = 0; j < MAX_SIZE; ++j) {
                result.append(cells[i][j] ? "1 " : "0 ");
            }
            result.append('\n');
        }
        return result.toString();
    }
}
