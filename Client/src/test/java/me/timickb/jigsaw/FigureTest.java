package me.timickb.jigsaw;

import me.timickb.jigsaw.client.domain.Figure;
import me.timickb.jigsaw.client.domain.enums.FigureReflection;
import me.timickb.jigsaw.client.domain.enums.FigureRotation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FigureTest {
    boolean checkEqual(boolean[][] cells1, boolean[][] cells2) {
        for (int i = 0; i < Figure.MAX_SIZE; ++i) {
            for (int j = 0; j < Figure.MAX_SIZE; ++j) {
                if (cells1[i][j] != cells2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Test
    void rotateClockwiseTest() {
        Figure figure = new Figure();

        boolean[][] cells = {
                {true, true, false},
                {false, true, false},
                {false, true, false}
        };

        boolean[][] rotatedClockwise = {
                {false, false, true},
                {true, true, true},
                {false, false, false}
        };

        figure.setCells(cells);
        figure.rotate(FigureRotation.CLOCKWISE, 1);

        Assertions.assertTrue(checkEqual(rotatedClockwise, figure.getCells()));
    }

    @Test
    void rotateAnticlockwiseTest() {
        Figure figure = new Figure();

        boolean[][] cells = {
                {true, true, false},
                {false, true, false},
                {false, true, false}
        };

        boolean[][] rotatedAntiClockwise = {
                {false, false, false},
                {true, true, true},
                {true, false, false}
        };

        figure.setCells(cells);
        figure.rotate(FigureRotation.ANTICLOCKWISE, 1);

        Assertions.assertTrue(checkEqual(rotatedAntiClockwise, figure.getCells()));
    }

    @Test
    void reflectVerticalTest() {
        Figure figure = new Figure();

        boolean[][] cells = {
                {true, true, false},
                {false, true, false},
                {false, true, false}
        };

        boolean[][] reflected = {
                {false, true, false},
                {false, true, false},
                {true, true, false}
        };

        figure.setCells(cells);
        figure.reflect(FigureReflection.VERTICAL);

        Assertions.assertTrue(checkEqual(reflected, figure.getCells()));
    }

    @Test
    void reflectHorizontalTest() {
        Figure figure = new Figure();

        boolean[][] cells = {
                {true, true, false},
                {false, true, false},
                {false, true, false}
        };

        boolean[][] reflected = {
                {false, true, true},
                {false, true, false},
                {false, true, false}
        };

        figure.setCells(cells);
        figure.reflect(FigureReflection.HORIZONTAL);

        Assertions.assertTrue(checkEqual(reflected, figure.getCells()));
    }
}
