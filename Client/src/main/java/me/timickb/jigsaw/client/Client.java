package me.timickb.jigsaw.client;

import javafx.application.Platform;
import me.timickb.jigsaw.client.domain.Figure;
import me.timickb.jigsaw.client.domain.Game;
import me.timickb.jigsaw.messenger.Message;
import me.timickb.jigsaw.messenger.MessageType;
import me.timickb.jigsaw.messenger.Messenger;

import java.io.IOException;
import java.net.Socket;

public class Client implements Runnable {
    private final Socket socket;
    private final Messenger messenger;
    private final JigsawController uiController;
    private final Game game;

    private String rivalName;

    public Client(Game game, JigsawController uiController, String host, int port) throws IOException {
        socket = new Socket(host, port);
        messenger = new Messenger(socket);
        this.uiController = uiController;
        this.game = game;
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected()) {
                Message message = messenger.readMessage();
                if (message.data() == null) {
                    continue;
                }
                Platform.runLater(() -> {
                    switch (message.type()) {
                        case LOGIN -> uiController.callLoginForm();
                        case ERROR -> uiController.callErrorMessage(message.data());
                        case AUTHORIZED -> authorize(message.data());
                        case NEW_FIGURE -> handleNewFigure(message.data());
                        case GAME_STARTED -> startGame(message.data());
                        case SCORE_UPDATED -> updateScore(message.data());
                        case DISCONNECT, GAME_OVER ->
                                uiController.disconnectDialog(message.data(), false);
                        case SOMEONE_LEFT -> uiController.updateInfoLabel(message.data());
                        case STATS_RESPONSE -> uiController.openStatsWindow(message.data());
                    }
                });
            }
        } catch (IOException e) {
            Platform.runLater(() -> uiController
                    .disconnectDialog("Потеряно соединение с сервером", true));
        }
    }

    private void authorize(String data) {
        uiController.authorize(data);
    }

    /**
     * Меняет UI под начало игры.
     *
     * @param data Никнейм противника. Если противника нет - пустая строка.
     */
    private void startGame(String data) {
        this.rivalName = data.split("#")[0];
        int gameTimeLimit = Integer.parseInt(data.split("#")[1]);

        uiController.startGame(gameTimeLimit);
        uiController.updateScore(rivalName, 0, 0);
    }

    // Парсит фигуру, которая пришла с сервера,
    // и передает ее в UI.
    private void handleNewFigure(String data) {
        if (!game.isGoingOn()) {
            return;
        }
        Figure figure = new Figure(parseArray(data));
        game.updateFigure(figure);
        uiController.renderSpawnArea();
    }

    private void updateScore(String data) {
        int myScore = Integer.parseInt(data.split("#")[0]);
        int rivalScore = Integer.parseInt(data.split("#")[1]);
        uiController.updateScore(rivalName, myScore, rivalScore);
    }

    public void login(String login) throws IOException {
        messenger.sendMessage(MessageType.LOGIN, login);
        System.out.println("Message sent");
    }

    public void leave() throws IOException {
        messenger.sendMessage(MessageType.LEAVE, "");
    }

    public void sendMessage(MessageType type, String data) throws IOException {
        messenger.sendMessage(type, data);
    }

    public void sendEmptyMessage(MessageType type) throws IOException {
        messenger.sendMessage(type);
    }

    private boolean[][] parseArray(String data) {
        boolean[][] result = new boolean[Figure.MAX_SIZE][Figure.MAX_SIZE];
        String[] rows = data.split("&");
        for (int i = 0; i < 3; ++i) {
            String[] values = rows[i].split("#");
            for (int j = 0; j < 3; ++j) {
                if (values[j].equals("1")) {
                    result[i][j] = true;
                }
            }
        }
        return result;
    }
}