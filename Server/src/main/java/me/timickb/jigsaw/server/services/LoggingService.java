package me.timickb.jigsaw.server.services;

import java.text.SimpleDateFormat;

@SuppressWarnings("ClassCanBeRecord")
public class LoggingService {
    private final String source;

    public LoggingService(String source) {
        this.source = source;
    }

    private void doLog(String level, String message) {
        String datetime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
                .format(new java.util.Date());
        System.out.printf("[%s] [%s] (%s) %s%n", level, datetime, source, message);
        System.out.print("> ");
    }

    public void info(String message) {
        doLog("INFO", message);
    }

    public void debug(String message) {
        doLog("DEBUG", message);
    }

    public void warn(String message) {
        doLog("WARNING", message);
    }

    public void error(String message) {
        doLog("ERROR", message);
    }
}
