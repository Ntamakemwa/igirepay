module org.example.igirepay {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example.igirepay to javafx.fxml;
    exports org.example.igirepay;
    exports org.example.igirepay.lab1.model;
    exports org.example.igirepay.lab1.exception;
}