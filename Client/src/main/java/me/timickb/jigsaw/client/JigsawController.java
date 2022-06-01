package me.timickb.jigsaw.client;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import me.timickb.jigsaw.client.domain.Field;
import me.timickb.jigsaw.client.domain.Figure;
import me.timickb.jigsaw.client.domain.Game;
import me.timickb.jigsaw.client.domain.LocalGameResult;
import me.timickb.jigsaw.messenger.MessageType;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Responsible for views rendering.
 */
public class JigsawController implements Initializable {
    @FXML
    public Button statsButton, stopButton, restartButton;
    @FXML
    private GridPane fieldView;
    @FXML
    private Pane spawnerPane;
    @FXML
    private Label timeView, pointCountView, myLoginView, infoView, gameStatusLabel;

    private Game game;
    private Group figureView;
    private Client client;
    private int gameTimeLimit;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init();
    }

    public void init() {
        Timeline gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeView.setText("Время: %d / %d сек.".formatted(game.getSeconds(), gameTimeLimit));
            game.incTime();
            // Автоматическое завершение игры
            if (game.getSeconds() >= gameTimeLimit) {
                try {
                    handleGameStop();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }));

        gameTimer.setCycleCount(Animation.INDEFINITE);

        stopButton.setOnMouseClicked(event -> {
            try {
                handleGameStop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        restartButton.setOnMouseClicked(event -> {
            try {
                handleGameRestart();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        stopButton.setVisible(false);
        restartButton.setVisible(false);

        statsButton.setOnMouseClicked(event -> {
            try {
                handleStatsButtonClick();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        game = new Game(gameTimer);
        Platform.runLater(this::connectionDialog);

        figureView = new Group();
        new DraggableMaker().makeDraggable(figureView);
        figureView.setOnMouseReleased(e -> {
            try {
                handlePlaceFigure();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        renderField();
        renderSpawnArea();
    }

    private void handleGameRestart() throws IOException {
        client.leave();
        init();
    }

    public void handleStatsButtonClick() throws IOException {
        client.sendEmptyMessage(MessageType.STATS_REQUEST);
    }

    /**
     * Opens a window which displays top 10 games.
     * @param data Data received from server.
     */
    public void openStatsWindow(String data) {
        try {
            String styleSheet = Objects.requireNonNull(getClass()
                    .getResource(JigsawApplication.STYLE_RESOURCE))
                    .toExternalForm();
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass()
                    .getResource(JigsawApplication.STATS_MARKUP_RESOURCE)));
            StatsController controller = new StatsController();
            controller.setData(data);
            loader.setController(controller);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("ТОП 10 игр");
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(styleSheet);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        Platform.exit();
        if (client != null) {
            try {
                client.leave();
                // client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    /**
     * Вывод диалога для установки соединения с сервером.
     */
    public void connectionDialog() {
        TextInputDialog dialog = new TextInputDialog("127.0.0.1:5001");
        dialog.setContentText("Адрес: ");
        dialog.setTitle("Подключение к серверу игры");
        dialog.setHeaderText("");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            System.out.println(result.get());
            try {
                String host = result.get().split(":")[0];
                int port = Integer.parseInt(result.get().split(":")[1]);
                client = new Client(game, this, host, port);
                new Thread(client).start();
            } catch (IOException | NumberFormatException e) {
                System.out.println("Couldn't connect to server");
                connectionDialog();
            }
        }
    }

    /**
     * Выводит информацию об отключении партнера
     *
     * @param login Логин партнера
     */
    public synchronized void updateInfoLabel(String login) {
        infoView.setText("Игрок " + login + " вышел из игры.");
    }

    /**
     * Displays an alert about disconnecting from the server/game.
     *
     * @param text Reason of disconnecting
     * @param exit True: close application after alert, false: keep opened
     */
    public synchronized void disconnectDialog(String text, boolean exit) {
        gameStatusLabel.setText("");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Вы отключены от игры");
        alert.setHeaderText("Причина: ");
        alert.setContentText(text);
        alert.showAndWait();
        if (exit) {
            Platform.exit();
            System.exit(0);
        }
    }

    /**
     * Starts the game.
     */
    public synchronized void startGame(int gameTimeLimit) {
        this.gameTimeLimit = gameTimeLimit;
        game.start();
        stopButton.setVisible(true);
    }

    public synchronized void authorize(String login) {
        myLoginView.setText(login);
        System.out.println("Authorized as " + login);
    }

    /**
     * Обновляет строку с счетом игроков.
     *
     * @param rivalName Никнейм противника. Если его нет - пустая строка.
     * @param my        Счет данного игрока.
     * @param rival     Счет противника.
     */
    public synchronized void updateScore(String rivalName, int my, int rival) {
        if (rivalName.isEmpty()) {
            pointCountView.setText("Счет: " + my);
            return;
        }
        if (game.isGoingOn()) {
            pointCountView.setText(String.format("Мой счет: %d | Счет %s: %d", my, rivalName, rival));
            return;
        }
        pointCountView.setText(String.format("Счет %s: %d", rivalName, rival));
    }

    public synchronized void callLoginForm() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setContentText("Придумайте логин: ");
        dialog.setHeaderText("Авторизация");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                System.out.println("Logged as: " + result.get());
                client.login(result.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void callErrorMessage(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Сообщение");
        alert.setHeaderText("Ошибка");
        alert.setContentText(text);
        alert.showAndWait();
    }

    public void handleGameStop() throws IOException {
        if (!game.isGoingOn()) {
            return;
        }

        LocalGameResult result = game.end();
        renderField();

        stopButton.setVisible(false);
        timeView.setText("");
        pointCountView.setText(String.format("Ваш результат: %d фигур / %d сек",
                result.score(), result.seconds()));
        gameStatusLabel.setText("Время вышло! Ожидание результатов");
        restartButton.setVisible(true);

        figureView.getChildren().clear();
        client.sendMessage(MessageType.CLIENT_TIMER_READY, Integer.toString(result.seconds()));

        showGameEndDialog(result);
    }

    public void handlePlaceFigure() throws IOException {
        if (game.getCurrentFigure() == null) {
            return;
        }

        Pair<Long, Long> cellData = computeNearestCell(fieldView, figureView);

        int rowIndex = cellData.getKey().intValue();
        int columnIndex = cellData.getValue().intValue();

        if (game.placeFigure(rowIndex, columnIndex)) {
            renderField();
            client.sendMessage(MessageType.FIGURE_PLACED, "");

            pointCountView.setText("Ходы: " + game.getScore());
        } else {
            figureView.setTranslateX(0);
            figureView.setTranslateY(0);
        }
    }

    /**
     * Перерисовывает область, в которой появляются фигуры.
     */
    public void renderSpawnArea() {
        int gap = (int) fieldView.getVgap();
        int areaWidth = Field.CELL_SIZE * 3 + gap * 2;

        spawnerPane.setMinWidth(areaWidth);
        spawnerPane.setMinHeight(areaWidth);

        figureView.getChildren().clear();
        figureView.setTranslateX(0);
        figureView.setTranslateY(0);
        spawnerPane.getChildren().clear();

        for (int i = 0; i < Figure.MAX_SIZE; ++i) {
            for (int j = 0; j < Figure.MAX_SIZE; ++j) {
                Rectangle cell = new Rectangle(Field.CELL_SIZE, Field.CELL_SIZE,
                        Color.TRANSPARENT);
                cell.setTranslateX((Field.CELL_SIZE + gap) * i);
                cell.setTranslateY((Field.CELL_SIZE + gap) * j);
                if (game.getCurrentFigure() != null
                        && game.getCurrentFigure().getCell(i, j)) {
                    cell.setFill(Color.BLUEVIOLET);
                }
                figureView.getChildren().add(cell);
            }
        }
        figureView.setTranslateX(gap * 2);
        figureView.setViewOrder(0);
        fieldView.setViewOrder(1);
        spawnerPane.getChildren().add(figureView);
    }

    /**
     * Перерисовывает игровое поле.
     */
    protected void renderField() {
        for (int i = 0; i < Field.SIZE; i++) {
            for (int j = 0; j < Field.SIZE; ++j) {
                fieldView.getChildren().add(renderFieldCell(i, j));
            }
        }
    }

    /**
     * Рисует ячейку игрового поля.
     * @param row Номер строки.
     * @param col Номер столбца.
     * @return Получившийся квадрат.
     */
    protected Rectangle renderFieldCell(int row, int col) {
        Rectangle cell = new Rectangle();
        cell.setWidth(Field.CELL_SIZE);
        cell.setHeight(Field.CELL_SIZE);
        if (!game.getField().getCell(row, col)) {
            cell.setFill(Color.LIGHTGRAY);
        } else {
            cell.setFill(Color.BLACK);
        }
        GridPane.setConstraints(cell, row, col);

        return cell;
    }

    /**
     * Shows a dialog with game results.
     *
     * @param result Game result object
     */
    protected void showGameEndDialog(LocalGameResult result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Сообщение");
        alert.setHeaderText("Игра завершена");
        alert.setContentText("Количество ходов: %s; Прошло времени: %s секунд"
                .formatted(result.score(), result.seconds()));
        alert.showAndWait();
    }

    /**
     * @param fieldView  Game field view
     * @param figureView Game figure view
     * @return Integer pair: row and column of computed field cell.
     */
    public Pair<Long, Long> computeNearestCell(GridPane fieldView, Group figureView) {
        Bounds fieldInScene = fieldView.localToScene(fieldView.getBoundsInLocal());
        Bounds figureInScene = figureView.localToScene(figureView.getBoundsInLocal());

        double fieldX = fieldInScene.getMinX() + fieldView.getPadding().getLeft();
        double fieldY = fieldInScene.getMinY() + fieldView.getPadding().getTop();

        double figureX = figureInScene.getMinX();
        double figureY = figureInScene.getMinY();

        long columnIndex = Math.round((figureX - fieldX) / (Field.CELL_SIZE + 5));
        long rowIndex = Math.round((figureY - fieldY) / (Field.CELL_SIZE + 5));

        return new Pair<>(columnIndex, rowIndex);
    }

    public Client getClient() {
        return client;
    }
}