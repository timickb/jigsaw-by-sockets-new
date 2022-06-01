package me.timickb.jigsaw.server.services;

import me.timickb.jigsaw.server.domain.GameResult;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents database connection instance.
 */
public class Database implements Closeable {
    public static final String GET_TABLE_QUERY =
            "SELECT * FROM APP.STATS ORDER BY end_date DESC";
    public static final String INSERT_ROW_QUERY =
            "INSERT INTO stats(%s, %s, %s, %s) VALUES (?, ?, ?, ?)";

    public static final String CLEAR_TABLE_QUERY =
            "DELETE FROM stats WHERE seconds >= 0";

    public static final String ID_COLUMN = "id";
    public static final String PLAYER_COLUMN = "player";
    public static final String STEPS_COLUMN = "steps_count";
    public static final String SECONDS_COLUMN = "seconds";
    public static final String DATE_COLUMN = "end_date";

    private final Connection connection;
    private final LoggingService logger;

    private String getConnectionString() {
        URL dbUrl = getClass().getClassLoader().getResource("database");
        if (dbUrl == null) {
            throw new NullPointerException();
        }
        return "jdbc:derby:" + dbUrl.getPath().substring(1);
    }

    /**
     * Creates database instance and establishes connection.
     * @throws SQLException Thrown when its impossible to establish
     * database connection.
     */
    public Database() throws SQLException {
        this.connection = DriverManager.getConnection(getConnectionString());
        this.logger = new LoggingService("DATABASE");

        if (connection != null) {
            logger.info("Database connection established.");
        }

        // addRecord(new GameResult(1, "aa", 1, 1, new Date(System.currentTimeMillis())));

        List<GameResult> list = getTable();
        for (GameResult item : list) {
            System.out.println(item);
        }
    }

    /**
     * Adds new row to the stats table.
     * @param record GameResult instance.
     */
    public void addRecord(GameResult record)  {
        if (connection == null) {
            logger.error("Cannot execute insert query: connection is null");
            return;
        }

        String query = INSERT_ROW_QUERY.formatted(
                PLAYER_COLUMN,
                STEPS_COLUMN,
                SECONDS_COLUMN,
                DATE_COLUMN);

        try {
            PreparedStatement st = connection.prepareStatement(query);
            st.setInt(2, record.stepsCount());
            st.setInt(3, record.seconds());
            st.setDate(4, new Date(0));
            st.setString(1, record.player());
            st.executeUpdate();
            connection.commit();
            logger.info("New game result (player: %s) added to database.".formatted(record.player()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get stats table data.
     * @return List of GameResult instances.
     * @throws SQLException Thrown when it is impossible to execute
     * a query to database.
     */
    public List<GameResult> getTable() throws SQLException {
        List<GameResult> table = new ArrayList<>();

        if (connection == null) {
            logger.error("Cannot execute select query: connection is null.");
            return table;
        }

        try (PreparedStatement statement = connection
                .prepareStatement(GET_TABLE_QUERY)) {
            try (ResultSet row = statement.executeQuery()) {
                while (row.next()) {
                    GameResult item = new GameResult(
                            row.getInt(ID_COLUMN),
                            row.getString(PLAYER_COLUMN),
                            row.getInt(STEPS_COLUMN),
                            row.getInt(SECONDS_COLUMN),
                            row.getDate(DATE_COLUMN)
                    );
                    table.add(item);
                }
            }

        }

        table.sort(
                Comparator.comparing(GameResult::seconds)
                        .thenComparing(GameResult::stepsCount)
                        .thenComparing(GameResult::player)
        );

        return table.stream().limit(10).collect(Collectors.toList());
    }

    /**
     * Removes all data from the stats table.
     * @throws SQLException  Thrown when it is impossible to execute
     *      * a query to database.
     */
    public void clearTable() throws SQLException {
        if (connection == null) {
            logger.error("Unable to drop table: connection is null");
            return;
        }
        connection.prepareStatement(CLEAR_TABLE_QUERY).executeUpdate();
        logger.info("Stats table cleared.");
    }

    @Override
    public void close() throws IOException {
        if (connection == null) {
            logger.error("Unable to close database: connection is null");
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOException();
        }
    }
}
