package me.timickb.jigsaw.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import me.timickb.jigsaw.client.domain.GameResult;

import java.net.URL;
import java.util.List;
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
            for (int colId = 0; colId < items.length; ++colId) {
                Label cell = new Label(items[colId]);
                if (colId == 0 || colId == 3) {
                    cell.getStyleClass().add("table-label-long");
                } else {
                    cell.getStyleClass().add("table-label-short");
                }
                statsTable.add(cell, colId, rowId + 1);
            }
        }
    }
}
