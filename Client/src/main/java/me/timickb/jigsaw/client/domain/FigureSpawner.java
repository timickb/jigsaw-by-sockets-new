package me.timickb.jigsaw.client.domain;

import me.timickb.jigsaw.client.exceptions.FigureFormatException;
import me.timickb.jigsaw.client.exceptions.FigureSpawnerException;

import java.io.*;
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
     * Create spawner
     * @param random Random generator object
     */
    public FigureSpawner(Random random) {
        this.random = random;
        figures = new ArrayList<>();
    }

    /**
     * Put a figure object to the spawner list.
     * @param figure Figure object
     */
    public void addFigure(Figure figure) {
        figures.add(figure);
    }

    /**
     * Reads figure description from file and puts
     * it to the spawner list.
     * @param file File to read
     * @return Readed figure object
     * @throws FigureSpawnerException Exception thrown if file format
     * is incorrect.
     */
    public Figure addFigureFromFile(File file) throws FigureFormatException {
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
            return figure;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Removes figure from the spawner list.
     * @param figure Pointer to figure
     */
    public void removeFigure(Figure figure) {
        figures.remove(figure);
    }

    /**
     * Returns next figure for player with random value.
     *
     * @return figure
     * @throws FigureSpawnerException
     */
    public Figure getNext() throws FigureSpawnerException {
        if (figures.isEmpty()) {
            throw new FigureSpawnerException();
        }
        int nextId = random.nextInt(figures.size());
        return figures.get(nextId);
    }

    /**
     * Returns a figure by its id.
     * @param id Identifier (type) of figure
     * @return figure
     */
    public Figure getById(int id) {
        return figures.get(id);
    }
}
