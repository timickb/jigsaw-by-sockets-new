package me.timickb.jigsaw.server;

import me.timickb.jigsaw.server.domain.GameResult;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database implements Closeable {
    private final Connection connection;

    public Database() throws SQLException {
        this.connection = DriverManager
                .getConnection("jdbc:derby://");
    }

    public void addRecord(GameResult record) throws SQLException {
        String query = "INSERT INTO APP.STATS (player, steps_count, seconds, end_date) VALUES " +
                "(%s, %d, %d, %s)".formatted(
                        record.player(),
                        record.stepsCount(),
                        record.seconds(),
                        record.endDate().toString());

        connection.prepareStatement(query).execute();
    }

    public List<GameResult> getTable() throws SQLException {
        List<GameResult> table = new ArrayList<>();
        try (PreparedStatement statement = connection
                .prepareStatement("SELECT * from APP.STATS")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    GameResult row = new GameResult(
                            resultSet.getInt("id"),
                            resultSet.getString("player"),
                            resultSet.getInt("steps_count"),
                            resultSet.getInt("seconds"),
                            resultSet.getDate("end_date")
                    );
                    table.add(row);
                }
            }

        }
        return table;
    }

    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOException();
        }
    }
}
