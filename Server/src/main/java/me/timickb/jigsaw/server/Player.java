package me.timickb.jigsaw.server;

import me.timickb.jigsaw.messenger.Message;
import me.timickb.jigsaw.messenger.MessageType;
import me.timickb.jigsaw.messenger.Messenger;
import me.timickb.jigsaw.server.domain.Figure;
import me.timickb.jigsaw.server.exceptions.FigureSpawnerException;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

/**
 * Represents the connection instance between
 * game client and server.
 */
public class Player implements Runnable {
    private final int id;
    private final Socket socket;
    private final GameServer server;
    private final Messenger messenger;
    private final Queue<Figure> figureQueue;

    private String login;
    private int gameScore;
    private int lastFigurePlacedMoment;

    /**
     * Creates new player (game client)
     *
     * @param socket Socket given by server
     */
    public Player(Socket socket, GameServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.id = server.getNextPlayerId();
        this.login = "";
        this.gameScore = 0;
        this.figureQueue = new ArrayDeque<>();
        messenger = new Messenger(socket);
    }

    @Override
    public void run() {
        try {
            System.out.printf("[Player %d] I'm connected.\n", id);

            requestLogin(messenger);

            // Communication loop
            while (socket.isConnected()) {
                Message message = messenger.readMessage();

                switch (message.type()) {
                    case FIGURE_PLACED -> handleFigurePlaced();
                    case LEAVE -> handlePlayerLeave();
                }
            }

        } catch (IOException e) {
            server.removePlayer(id);
            System.out.printf("[Player %d] I'm disconnected\n", id);
        } catch (FigureSpawnerException e) {
            e.printStackTrace();
        }
    }

    private void handlePlayerLeave() throws IOException {
        server.removePlayer(id);
        // Send leave information to rival.
        Optional<Player> rivalOpt = server.getRival(id);
        if (rivalOpt.isPresent()) {
            rivalOpt.get().sendMessage(MessageType.SOMEONE_LEFT, login);
        }
    }

    private void handleFigurePlaced() throws IOException, FigureSpawnerException {
        ++gameScore;
        lastFigurePlacedMoment = server.getCurrentGameTime();

        sendScoreUpdate();

        Optional<Player> rivalOpt = server.getRival(id);
        if (rivalOpt.isPresent()) {
            rivalOpt.get().sendScoreUpdate();
        }
        sendNextFigure();
    }

    public void sendScoreUpdate() throws IOException {
        Optional<Player> rival = server.getRival(id);
        int rivalScore = rival.map(Player::getGameScore).orElse(-1);
        String data = String.format("%d#%d", gameScore, rivalScore);
        messenger.sendMessage(MessageType.SCORE_UPDATED, data);
    }

    private void requestLogin(Messenger messenger) throws IOException, FigureSpawnerException {
        Message answer;

        // Запрашиваем ввести логин до тех пор, пока не получим валидный ответ
        while (true) {
            messenger.sendMessage(MessageType.LOGIN, "");

            System.out.printf("[Player %d] Login request was sent\n", id);

            answer = messenger.readMessage();
            if (answer.type() == MessageType.LOGIN && !answer.data().isEmpty()
                    && answer.data().length() <= GameServer.MAX_LOGIN_LENGTH) {
                // Login successfully
                this.login = answer.data();
                messenger.sendMessage(MessageType.AUTHORIZED, this.login);

                System.out.printf("[Player %d] Authorized\n", this.id);

                // Если достаточно игроков набрано - запускаем игру.
                if (server.getOnlinePlayersCount() == server.getRequiredPlayersCount()) {
                    server.startGame();
                }

                break;
            }

            messenger.sendMessage(MessageType.ERROR, "Incorrect query");
        }
    }

    public void sendMessage(MessageType type, String data) throws IOException {
        messenger.sendMessage(type, data);
    }

    public void sendNextFigure() throws IOException, FigureSpawnerException {
        if (figureQueue.isEmpty()) {
            synchronized (server) {
                Figure next = server.getFigureSpawner().getNext();

                addFigureToQueue(next);

                Optional<Player> rival = server.getRival(id);
                rival.ifPresent(player -> player.addFigureToQueue(next));
            }
        }
        messenger.sendMessage(MessageType.NEW_FIGURE, Objects.requireNonNull(figureQueue.poll()).toString());
    }

    public synchronized void disconnect(String reason) throws IOException {
        messenger.sendMessage(MessageType.DISCONNECT, reason);
        System.out.printf("[Player %d] Disconnected: %s\n", id, reason);
        messenger.close();
        socket.close();
    }

    public void sendGameResult(Player winner) throws IOException {
        String text = String.format("Game over. The winner is %s with score %d and time %ds.",
                winner.getLogin(), winner.getGameScore(), winner.getLastFigurePlacedMoment());
        messenger.sendMessage(MessageType.GAME_OVER, text);
    }

    @Override
    public String toString() {
        return String.format("[id %d] %s, host: %s", id, login,
                socket.getInetAddress().toString());
    }

    public int getGameScore() {
        return gameScore;
    }

    public String getLogin() {
        return login;
    }

    public void addFigureToQueue(Figure figure) {
        figureQueue.add(figure);
    }

    public int getLastFigurePlacedMoment() {
        return lastFigurePlacedMoment;
    }
}