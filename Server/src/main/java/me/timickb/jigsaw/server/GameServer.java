package me.timickb.jigsaw.server;

import me.timickb.jigsaw.server.domain.FigureSpawner;
import me.timickb.jigsaw.server.domain.FigureSpawnerCreator;
import me.timickb.jigsaw.server.services.LoggingService;
import me.timickb.jigsaw.server.exceptions.FigureSpawnerException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class GameServer implements Runnable {
    public static final int MAX_LOGIN_LENGTH = 20;

    private final ServerSocket serverSocket;
    private final FigureSpawner figureSpawner;
    private final Database database;
    private final int gameTimeLimit;
    private final int requiredPlayersCount;
    private final LoggingService logger;

    private Player firstPlayer;
    private Player secondPlayer;
    private int currentGameTime;
    private boolean gameGoingOn;

    public GameServer(int port, int playersCount, int gameTimeLimit) throws IOException, FigureSpawnerException, SQLException {
        this.serverSocket = new ServerSocket(port);
        this.figureSpawner = new FigureSpawnerCreator().createFromDefaultFiles();
        this.requiredPlayersCount = playersCount;
        this.gameTimeLimit = gameTimeLimit;
        this.currentGameTime = 0;
        this.database = new Database();
        this.logger = new LoggingService("SERVER");
    }

    @Override
    public void run() {
        logger.info("Server started.");
        try {
            logger.info("Waiting for connections...");

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                Player player = new Player(socket, this);
                new Thread(player).start();

                if (gameGoingOn) {
                    player.disconnect("Game is already going.");
                    continue;
                }

                if (firstPlayer == null) firstPlayer = player;
                else secondPlayer = player;

            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("An error occurred while accepting connection.");
        }
    }

    public void startGame() {
        if (getOnlinePlayersCount() < requiredPlayersCount) {
            logger.info("Couldn't start game: not enough players connected.");
            return;
        }

        gameGoingOn = true;
        logger.info("Game started!");
    }

    public void endGame() throws IOException {
        if (!gameGoingOn) {
            logger.error("Game isn't running.");
            return;
        }
        logger.info("Game over!");

        Player winner = null;
        int maxScore = 0;
        if (firstPlayer != null && firstPlayer.getGameScore() > maxScore) {
            winner = firstPlayer;
            maxScore = firstPlayer.getGameScore();
        }
        if (secondPlayer != null && secondPlayer.getGameScore() >= maxScore) {
            if ((secondPlayer.getGameScore() != maxScore
                    || secondPlayer.getLastFigurePlacedMoment() > gameTimeLimit)
                    && (secondPlayer.getGameScore() <= maxScore)) {
            } else {
                winner = secondPlayer;
            }
        }
        Objects.requireNonNull(firstPlayer).sendGameResult(Objects.requireNonNull(winner));
        Objects.requireNonNull(secondPlayer).sendGameResult(Objects.requireNonNull(winner));

        gameGoingOn = false;
    }

    /**
     * Close all entities and stop the server.
     * @throws IOException
     */
    public void stop() throws IOException {
        if (gameGoingOn) {
            endGame();
        }
        database.close();
        serverSocket.close();
    }

    /**
     * Prints console information for
     * the "info" command.
     */
    public void printGameStatus() {
        System.out.print("Game status: ");
        if (gameGoingOn) {
            System.out.println("started");
        } else {
            System.out.println("waiting for players");
        }
        System.out.println();
        System.out.println("There are " + getOnlinePlayersCount() + " players:");
        System.out.println();

        if (firstPlayer != null) System.out.println(firstPlayer);
        if (secondPlayer != null) System.out.println(secondPlayer);
    }

    /**
     * Generates an ID for next player connected.
     * @return 1: First's player place was empty; 2: Second's player
     * place was empty; -1: No empty places left.
     */
    public int getNextPlayerId() {
        if (firstPlayer == null) {
            return 1;
        }
        if (secondPlayer == null && requiredPlayersCount > 1) {
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
     *
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

    /**
     * @return Amount of players required in the game.
     */
    public int getRequiredPlayersCount() {
        return requiredPlayersCount;
    }

    /**
     * @return Database client instance.
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * @return Game status flag.
     */
    public boolean isGameGoingOn() {
        return gameGoingOn;
    }
}
