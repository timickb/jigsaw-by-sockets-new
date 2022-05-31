package me.timickb.jigsaw.server;

import me.timickb.jigsaw.server.services.LoggingService;
import me.timickb.jigsaw.server.exceptions.FigureSpawnerException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static final int DEFAULT_PORT = 5001;
    public static final int DEFAULT_MAX_PLAYERS = 2;
    public static final int DEFAULT_GAME_TIME = 30;

    public static final int MIN_PLAYERS = 1;
    public static final int MAX_PLAYERS = 2;

    public static void main(String[] args) {
        System.out.println("--- Jigsaw Server ---");

        LoggingService logger = new LoggingService("MAIN");

        int serverPort = DEFAULT_PORT;
        int playersCount = DEFAULT_MAX_PLAYERS;
        int gameTime = DEFAULT_GAME_TIME;

        if (args.length > 0) {
            try {
                serverPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.error("Server port must be a string. Using default port.");
            }
        }

        if (args.length > 1) {
            try {
                playersCount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                logger.error("Players count must be a number. Using default value.");
            }
            if (playersCount < MIN_PLAYERS || playersCount > MAX_PLAYERS) {
                logger.error("Players count must be 1 or 2. Using default value");
            }
        }

        if (args.length > 2) {
            try {
                gameTime = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                logger.error("Game time must be a number of seconds. Using default value.");
            }
            if (gameTime < 5) {
                logger.error("Game time must be equal or above than 5 seconds. Using default value");
            }
        }

        try {
            logger.info("Starting server on port %d...".formatted(serverPort));

            GameServer server = new GameServer(serverPort, playersCount, gameTime);
            Thread serverThread = new Thread(server);
            serverThread.start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("> ");
                String command = scanner.nextLine();
                if (command.equals("stop")) {
                    server.stop();
                    break;
                } else if (command.equals("info")) {
                    server.printGameStatus();
                } else {
                    System.out.println("Unknown command.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("I/O error. Shutdown server.");
        } catch (FigureSpawnerException e) {
            e.printStackTrace();
            logger.error("Couldn't create game figure spawner");
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        } finally {
            logger.error("Server stopped.");
        }
    }
}
