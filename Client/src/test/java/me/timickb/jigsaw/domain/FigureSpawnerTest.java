package me.timickb.jigsaw.domain;

import me.timickb.jigsaw.client.domain.Figure;
import me.timickb.jigsaw.client.domain.FigureSpawner;
import me.timickb.jigsaw.client.exceptions.FigureFormatException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

class FigureSpawnerTest {

    @Test
    void addFigureFromFileTest() {
        File file = new File("src/test/resources/test_figure_1.txt");
        Assertions.assertDoesNotThrow(() -> {
            Figure figure = new FigureSpawner().addFigureFromFile(file);
            Assertions.assertEquals(figure.toString(), "1 1 0 \n0 1 0 \n0 1 0 \n");
        });
    }

    @Test
    void getNextTest() throws FigureFormatException {
        FigureSpawner spawner = new FigureSpawner();
        for (int i = 1; i <= 5; ++i) {
            spawner.addFigureFromFile(new File("src/test/resources/test_figure_%d.txt"
                    .formatted(i)));
        }
        Assertions.assertDoesNotThrow(() -> {
            Figure figure = spawner.getNext();
        });

    }

    @Test
    void addFigureFromFileExceptionTest() {
        Assertions.assertThrows(FigureFormatException.class, () -> {
           new FigureSpawner()
                   .addFigureFromFile(new File("src/test/resources/test_figure_invalid.txt"));
        });
    }
}