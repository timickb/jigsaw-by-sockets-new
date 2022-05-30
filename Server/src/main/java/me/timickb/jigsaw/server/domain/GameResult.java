package me.timickb.jigsaw.server.domain;

import java.util.Date;

public record GameResult(int id, String player, int stepsCount, int seconds, Date endDate) {
    @Override
    public String toString() {
        return "[%d] (%s) Steps: %d, time: %ds, date: %s"
                .formatted(id, player, stepsCount, seconds, endDate.toString());
    }
}
