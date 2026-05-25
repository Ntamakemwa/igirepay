module org.example.igirepay {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens org.example.igirepay to javafx.fxml;
    exports org.example.igirepay;
    exports org.example.igirepay.lab1.model;
    exports org.example.igirepay.lab1.exception;
    exports org.example.igirepay.lab2.db;
    exports org.example.igirepay.lab2.dao;
}