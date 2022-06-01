package me.timickb.jigsaw.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class StatsController implements Initializable {
    private String data;
    @FXML
    private GridPane statsTable;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        renderData();
    }

    public void setData(String data) {
        this.data = data;
    }

    private void renderData() {

        String[] rows = data.split("@");
        for (int rowId = 0; rowId < rows.length; ++rowId) {
            String[] items = rows[rowId].split(";");

            Date parsed = new java.util.Date((long) (Integer.parseInt(items[3])) * 1000);

            Label player = new Label(items[0]);
            Label score = new Label(items[1]);
            Label seconds = new Label(items[2]);
            Label date = new Label(
                    new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(parsed));

            player.getStyleClass().add("table-label-long");
            date.getStyleClass().add("table-label-long");
            score.getStyleClass().add("table-label-short");
            seconds.getStyleClass().add("table-label-short");

            statsTable.add(player, 0, rowId + 1);
            statsTable.add(score, 1, rowId + 1);
            statsTable.add(seconds, 2, rowId + 1);
            statsTable.add(date, 3, rowId + 1);
        }
    }
}
