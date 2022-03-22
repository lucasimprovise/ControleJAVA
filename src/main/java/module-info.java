module com.example.projetjavafx2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires json.simple;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens com.example.projetjavafx2 to javafx.fxml;
    exports com.example.projetjavafx2;
}