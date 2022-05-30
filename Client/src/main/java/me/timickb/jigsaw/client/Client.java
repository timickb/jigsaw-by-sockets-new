package me.timickb.jigsaw.client;

import me.timickb.jigsaw.client.domain.Game;
import me.timickb.jigsaw.messenger.Messenger;

import java.io.IOException;
import java.net.Socket;

public class Client implements Runnable {
    private final Socket socket;
    private final Messenger messenger;
    private final JigsawController uiController;
    private final Game game;

    public Client(Game game, JigsawController uiController, String host, int port) throws IOException {
        socket = new Socket(host, port);
        messenger = new Messenger(socket);
        this.uiController = uiController;
        this.game = game;
    }

    @Override
    public void run() {

    }
}
