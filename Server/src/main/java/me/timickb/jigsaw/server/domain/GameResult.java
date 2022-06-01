package me.timickb.jigsaw.server.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

public record GameResult(int id, String player, int stepsCount, int seconds, int endDate) {
    @Override
    public String toString() {
        return "[%d] (%s) Steps: %d, time: %ds, date: %d"
                .formatted(id, player, stepsCount, seconds, endDate);
    }

    public String getMessengerRepresentation() {
        return "%s;%d;%d;%d".formatted(player, stepsCount, seconds, endDate);
    }
}
