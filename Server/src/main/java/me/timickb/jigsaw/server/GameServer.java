package me.timickb.jigsaw.server;

import me.timickb.jigsaw.server.domain.FigureSpawner;
import me.timickb.jigsaw.server.domain.FigureSpawnerCreator;
import me.timickb.jigsaw.server.exceptions.FigureSpawnerException;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.Optional;

public class GameServer implements Runnable {
    public static final int MAX_LOGIN_LENGTH = 20;

    private final ServerSocket serverSocket;
    private final FigureSpawner figureSpawner;
    private final Database database;

    private Player firstPlayer;
    private Player secondPlayer;

    private final int gameTimeLimit;
    private final int requiredPlayersCount;
    private int currentGameTime;

    public GameServer(int port, int playersCount, int gameTimeLimit) throws IOException, FigureSpawnerException, SQLException {
        this.serverSocket = new ServerSocket(port);
        this.figureSpawner = new FigureSpawnerCreator().createFromDefaultFiles();
        this.requiredPlayersCount = playersCount;
        this.gameTimeLimit = gameTimeLimit;
        this.currentGameTime = 0;
        this.database = new Database();
    }

    @Override
    public void run() {

    }

    public void startGame() {
        // TODO
    }

    public void stop() throws IOException {
        database.close();
        serverSocket.close();
    }

    public void printGameStatus() {
        // TODO
    }

    /**
     * @return 1: First's player place was empty; 2: Second's player
     * place was empty; -1: No empty places left.
     */
    public int getNextPlayerId() {
        if (firstPlayer == null) {
            return 1;
        }
        if (secondPlayer == null) {
            return 2;
        }
        return -1;
    }

    /**
     * @return FigureSpawner instance.
     */
    public FigureSpawner getFigureSpawner() {
        return figureSpawner;
    }

    public void removePlayer(int id) {
        if (id == 1) {
            firstPlayer = null;
        }
        if (id == 2) {
            secondPlayer = null;
        }
    }

    /**
     * Rival instance for specified player
     * @param id Player id
     * @return Optional with rival instance.
     */
    public Optional<Player> getRival(int id) {
        if (id == 1) {
            return Optional.ofNullable(firstPlayer);
        }
        if (id == 2) {
            return Optional.ofNullable(secondPlayer);
        }
        return Optional.empty();
    }

    /**
     * @return Online players amount.
     */
    public int getOnlinePlayersCount() {
        int result = 0;
        if (firstPlayer != null) ++result;
        if (secondPlayer != null) ++result;
        return result;
    }

    /**
     * @return Current game duration in seconds.
     */
    public int getCurrentGameTime() {
        return currentGameTime;
    }

    public int getRequiredPlayersCount() {
        return requiredPlayersCount;
    }
}
