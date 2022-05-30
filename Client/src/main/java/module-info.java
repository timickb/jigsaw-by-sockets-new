module me.timickb.jigsaw {
    requires javafx.controls;
    requires javafx.fxml;
    requires me.timickb.jigsaw.messenger;
    requires me.timickb.jigsaw.server;

    opens me.timickb.jigsaw.client to javafx.fxml;
    exports me.timickb.jigsaw.client;
    exports me.timickb.jigsaw.client.domain;
    exports me.timickb.jigsaw.client.domain.enums;
    exports me.timickb.jigsaw.client.exceptions;
    opens me.timickb.jigsaw.client.domain to javafx.fxml;
}