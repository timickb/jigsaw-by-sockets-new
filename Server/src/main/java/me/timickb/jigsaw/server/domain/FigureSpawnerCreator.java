package me.timickb.jigsaw.server.domain;

import me.timickb.jigsaw.server.exceptions.FigureFormatException;
import me.timickb.jigsaw.server.exceptions.FigureSpawnerException;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * FigureSpawner creation manager.
 */
public class FigureSpawnerCreator {
    /**
     * Reads default figures from resource directory
     * and adds it to new spawner.
     *
     * @return Generated spawner.
     */
    public FigureSpawner createFromDefaultFiles() throws FigureSpawnerException {
        FigureSpawner spawner = new FigureSpawner();

        try {
            int amount = Objects.requireNonNull(new File(Objects.requireNonNull(getClass()
                            .getResource("/figures"))
                    .getFile()).listFiles()).length;

            for (int i = 1; i <= amount; ++i) {
                try {
                    spawner.addFigureFromFile(new File(Objects.requireNonNull(getClass()
                            .getResource("/figures/f%d.txt".formatted(i))).toURI()));
                } catch (FigureFormatException | URISyntaxException e) {
                    e.printStackTrace();
                    throw new FigureSpawnerException();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return spawner;
    }
}