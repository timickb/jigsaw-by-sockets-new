package me.timickb.jigsaw.server.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

public record GameResult(int id, String player, int stepsCount, int seconds, Date endDate) {
    @Override
    public String toString() {
        return "[%d] (%s) Steps: %d, time: %ds, date: %s"
                .formatted(id, player, stepsCount, seconds, endDate.toString());
    }

    public String getMessengerRepresentation() {
        return "%s;%d;%d;%d".formatted(player, stepsCount, seconds, endDate.getTime());
    }
}
