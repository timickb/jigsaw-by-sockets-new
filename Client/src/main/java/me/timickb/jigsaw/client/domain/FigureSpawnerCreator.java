package me.timickb.jigsaw.client.domain;


import me.timickb.jigsaw.client.exceptions.FigureFormatException;

import java.io.File;
import java.util.Objects;

/**
 * FigureSpawner creation manager.
 */
public class FigureSpawnerCreator {
    /** Reads default figures from resource directory
     * and adds it to new spawner.
     * @return Generated spawner.
     */
    public FigureSpawner createFromDefaultFiles() {
        FigureSpawner spawner = new FigureSpawner();

        try {
            int amount = Objects.requireNonNull(new File(Objects.requireNonNull(getClass().getClassLoader()
                            .getResource("default-figures"))
                    .getFile()).listFiles()).length;

            for (int i = 1; i <= amount; ++i) {
                try {
                    spawner.addFigureFromFile(new File(Objects.requireNonNull(getClass().getClassLoader()
                            .getResource("default-figures/f%d.txt".formatted(i))).getFile()));
                } catch (FigureFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return spawner;
    }

}
