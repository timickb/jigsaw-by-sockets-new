package me.timickb.jigsaw.client.domain;

/**
 * Represents the game result.
 * Contains information about reached score
 * and game time.
 */
public record LocalGameResult(int score, int seconds) {
}
