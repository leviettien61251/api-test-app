module com.example.apitestapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires java.sql;
    requires okhttp3;
    requires static lombok;

    opens com.example.apitestapp to javafx.fxml;
    opens com.example.apitestapp.controllers to javafx.fxml, javafx.base;

    exports com.example.apitestapp;
    exports com.example.apitestapp.controllers;
}
