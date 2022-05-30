package me.timickb.jigsaw.server;

import me.timickb.jigsaw.server.domain.FigureSpawner;
import me.timickb.jigsaw.server.domain.FigureSpawnerCreator;
import me.timickb.jigsaw.server.exceptions.FigureSpawnerException;

import java.io.IOException;
import java.net.ServerSocket;

public class GameServer implements Runnable {
    private final ServerSocket serverSocket;
    private final FigureSpawner figureSpawner;

    private Player firstPlayer;
    private Player secondPlayer;

    private int gameTimeLimit;
    private int playersCount;
    private int currentGameTime;

    public GameServer(int port, int playersCount, int gameTimeLimit) throws IOException, FigureSpawnerException {
        this.serverSocket = new ServerSocket(port);
        this.figureSpawner = new FigureSpawnerCreator().createFromDefaultFiles();
        this.playersCount = playersCount;
        this.gameTimeLimit = gameTimeLimit;
        this.currentGameTime = 0;
    }

    @Override
    public void run() {

    }

    public void stop() {}

    public void printGameStatus() {}

    public int getNextPlayerId() {
        if (firstPlayer == null) {
            return 1;
        }
        if (secondPlayer == null) {
            return 2;
        }
        return 0;
    }
}
