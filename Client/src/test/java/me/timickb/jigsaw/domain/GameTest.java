package me.timickb.jigsaw.domain;

import javafx.animation.Timeline;
import me.timickb.jigsaw.client.domain.Game;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Time;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void start() {
        Game game = new Game(new Timeline());
        game.start();
        Assertions.assertTrue(game.isGoingOn());
    }

    @Test
    void updateFigure() {
        Game game = new Game(new Timeline());
    }

    @Test
    void placeFigure() {
    }

    @Test
    void end() {
        Game game = new Game(new Timeline());
        game.start();
        game.end();
        Assertions.assertFalse(game.isGoingOn());
    }

    @Test
    void incTime() {
        Game game = new Game(new Timeline());
        game.incTime();
        Assertions.assertEquals(game.getSeconds(), 1);
    }
}