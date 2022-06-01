package me.timickb.jigsaw.server;

import me.timickb.jigsaw.messenger.MessageType;
import me.timickb.jigsaw.server.domain.FigureSpawner;
import me.timickb.jigsaw.server.domain.FigureSpawnerCreator;
import me.timickb.jigsaw.server.domain.GameResult;
import me.timickb.jigsaw.server.services.Database;
import me.timickb.jigsaw.server.services.LoggingService;
import me.timickb.jigsaw.server.exceptions.FigureSpawnerException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

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
    private Timer gameTimer;


    public GameServer(int port, int playersCount, int gameTimeLimit) throws IOException, FigureSpawnerException, SQLException {
        this.serverSocket = new ServerSocket(port);
        this.figureSpawner = new FigureSpawnerCreator().createFromDefaultFiles();
        this.requiredPlayersCount = playersCount;
        this.gameTimeLimit = gameTimeLimit;
        this.currentGameTime = 0;
        this.database = new Database();
        this.logger = new LoggingService("SERVER");
        this.gameTimer = new Timer();
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
        } catch (IOException ignored) {
        }
    }

    public void startGame() throws IOException, FigureSpawnerException {
        if (getOnlinePlayersCount() < requiredPlayersCount) {
            logger.info("Couldn't start game: not enough players connected.");
            return;
        }

        gameGoingOn = true;
        logger.info("Game started!");

        String secondPlayerName = secondPlayer != null ? secondPlayer.getLogin() : "";

        firstPlayer.sendMessage(MessageType.GAME_STARTED,
                String.format("%s#%d", secondPlayerName, gameTimeLimit));
        firstPlayer.sendNextFigure();
        if (secondPlayer != null) {
            secondPlayer.sendMessage(MessageType.GAME_STARTED,
                    String.format("%s#%d", firstPlayer.getLogin(), gameTimeLimit));
            secondPlayer.sendNextFigure();
        }

        gameTimer = new Timer();
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                ++currentGameTime;

                if (currentGameTime % 10 == 0) {
                    logger.info("Game continuing %ds.".formatted(currentGameTime));
                }

                if (currentGameTime >= gameTimeLimit) {
                    try {
                        endGame();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.cancel();
                }
            }
        }, 0L, 1000L);
    }

    public void endGame() throws IOException {
        if (!gameGoingOn) {
            logger.error("Game isn't running.");
            return;
        }
        logger.info("Game over!");
        gameTimer.cancel();

        savePlayerResult(firstPlayer);
        savePlayerResult(secondPlayer);

        Player winner = firstPlayer;

        if (firstPlayer != null) firstPlayer.sendGameResult(Objects.requireNonNull(winner));
        if (secondPlayer != null) secondPlayer.sendGameResult(Objects.requireNonNull(winner));

        gameGoingOn = false;
    }

    /**
     * @param player Writes player's result to the database.
     */
    private void savePlayerResult(Player player) {
        if (player == null) {
            return;
        }
        database.addRecord(new GameResult(0,
                player.getLogin(),
                player.getGameScore(),
                player.getLastFigurePlacedMoment(),
                new Date(System.currentTimeMillis())));
    }

    /**
     * Close all entities and stop the server.
     *
     * @throws IOException I/O error.
     */
    public void stop() throws IOException {
        logger.info("Stopping server...");
        if (gameGoingOn) {
            endGame();
            logger.info("Game ended");
        }
        database.close();
        logger.info("Database closed.");
        serverSocket.close();
        logger.info("Server socket closed.");
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
     *
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
            return Optional.ofNullable(secondPlayer);
        }
        if (id == 2) {
            return Optional.ofNullable(firstPlayer);
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
     * Asks database to clear stats table.
     * @throws SQLException Impossible to perform sql query.
     */
    public void clearStatsTable() throws SQLException {
        database.clearTable();
    }

    /**
     * @return Game status flag.
     */
    public boolean isGameGoingOn() {
        return gameGoingOn;
    }
}
