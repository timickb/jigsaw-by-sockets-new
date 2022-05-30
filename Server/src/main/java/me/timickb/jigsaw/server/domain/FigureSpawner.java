package me.timickb.jigsaw.server.domain;

import me.timickb.jigsaw.server.exceptions.FigureFormatException;
import me.timickb.jigsaw.server.exceptions.FigureSpawnerException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FigureSpawner {
    private final Random random;
    private final List<Figure> figures;

    /**
     * Create spawner
     */
    public FigureSpawner() {
        random = new Random(System.currentTimeMillis());
        figures = new ArrayList<>();
    }

    /**
     * Reads figure description from file and puts
     * it to the spawner list.
     * @param file File to read
     * is incorrect.
     */
    public void addFigureFromFile(File file) throws FigureFormatException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            boolean[][] schema = new boolean[Figure.MAX_SIZE][Figure.MAX_SIZE];
            String line;

            int row = 0;
            while ((line = reader.readLine()) != null) {
                if (row >= Figure.MAX_SIZE) {
                    throw new FigureFormatException();
                }
                String[] values = line.split(" ");
                if (values.length != Figure.MAX_SIZE) {
                    throw new FigureFormatException();
                }
                for (int col = 0; col < Figure.MAX_SIZE; ++col) {
                    if (values[col].equals("1")) {
                        schema[row][col] = true;
                    }
                }
                ++row;
            }
            if (row < Figure.MAX_SIZE) {
                throw new FigureFormatException();
            }
            Figure figure = new Figure(schema);
            figures.add(figure);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns next figure for player with random value.
     *
     * @return figure
     */
    public Figure getNext() throws FigureSpawnerException {
        if (figures.isEmpty()) {
            throw new FigureSpawnerException();
        }
        int nextId = random.nextInt(figures.size());
        return figures.get(nextId);
    }
}
