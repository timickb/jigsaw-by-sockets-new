package me.timickb.jigsaw.messenger;

import java.io.*;
import java.net.Socket;

/**
 * Client-Server communication wrapper.
 */
public class Messenger implements Closeable {
    private final BufferedReader reader;
    private final BufferedWriter writer;

    public Messenger(Socket socket) throws IOException {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    /**
     * Sends formatted empty TCP message to the other side of socket.
     * @param type Message type
     * @throws IOException I/O error.
     */
    public void sendMessage(MessageType type) throws IOException {
        sendMessage(type, "");
    }

    /**
     * Sends formatted TCP message to the other side of socket.
     * @param type Message type
     * @param content Message text
     * @throws IOException I/O error
     */
    public void sendMessage(MessageType type, String content) throws IOException {
        String data = type.name() + "%" + content;
        writer.write(data);
        writer.newLine();
        writer.flush();
    }

    /**
     * Waits for next message from the other side of socket.
     * @return Message object.
     * @throws IOException I/O error.
     */
    public Message readMessage() throws IOException {
        String data = reader.readLine();

        String typeRaw = data.split("%")[0];
        String content;
        try {
            content = data.split("%")[1];
        } catch (IndexOutOfBoundsException e) {
            content = "";
        }

        return new Message(MessageType.valueOf(typeRaw), content);
    }

    @Override
    public void close() throws IOException {
        reader.close();
        writer.close();
    }
}
