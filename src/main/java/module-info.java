module org.example.igirepay {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens org.example.igirepay to javafx.fxml;
    opens org.example.igirepay.lab3.ui.javafx to javafx.fxml;
    exports org.example.igirepay;
    exports org.example.igirepay.lab1.model;
    exports org.example.igirepay.lab1.exception;
    exports org.example.igirepay.lab2.db;
    exports org.example.igirepay.lab2.dao;
    exports org.example.igirepay.lab3.service;
    exports org.example.igirepay.lab3.auth;
    exports org.example.igirepay.lab3.reports;
    exports org.example.igirepay.lab3.ui.javafx;
}
