module com.example.apitestapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires java.sql;
    requires jdk.httpserver;
    requires okhttp3;


    opens com.example.apitestapp to javafx.fxml;
    opens com.example.apitestapp.controllers to javafx.fxml;
    exports com.example.apitestapp;
}