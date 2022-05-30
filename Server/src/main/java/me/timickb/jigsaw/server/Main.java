package me.timickb.jigsaw.server;

import me.timickb.jigsaw.server.exceptions.FigureSpawnerException;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class Main {
    public static final int DEFAULT_PORT = 5001;
    public static final int DEFAULT_MAX_PLAYERS = 2;
    public static final int DEFAULT_GAME_TIME = 30;

    public static final int MIN_PLAYERS = 1;
    public static final int MAX_PLAYERS = 2;

    public static void main(String[] args) {
        System.out.println("--- Jigsaw Server ---");

        URL url = Main.class.getClassLoader()
                .getResource("figures/f1.txt");

        int serverPort = DEFAULT_PORT;
        int playersCount = DEFAULT_MAX_PLAYERS;
        int gameTime = DEFAULT_GAME_TIME;

        if (args.length > 0) {
            try {
                serverPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Server port must be a string. Using default port.");
            }
        }

        if (args.length > 1) {
            try {
                playersCount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Players count must be a number. Using default value.");
            }
            if (playersCount < MIN_PLAYERS || playersCount > MAX_PLAYERS) {
                System.out.println("[ERROR] Players count must be 1 or 2. Using default value");
            }
        }

        if (args.length > 2) {
            try {
                gameTime = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Game time must be a number of seconds. Using default value.");
            }
            if (gameTime < 5) {
                System.out.println("[ERROR] Game time must be equal or above than 5 seconds." +
                        "Using default value");
            }
        }

        try {
            System.out.println("Starting on port " + serverPort + "...");

            GameServer server = new GameServer(serverPort, playersCount, gameTime);
            Thread serverThread = new Thread(server);
            serverThread.start();

            System.out.println("Server started.");

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
            System.out.println("[FATAL] I/O error. Shutdown server.");
        } catch (FigureSpawnerException e) {
            e.printStackTrace();
            System.out.println("[FATAL] Couldn't create game figure spawner");
        } finally {
            System.out.println("Server stopped.");
        }
    }
}
