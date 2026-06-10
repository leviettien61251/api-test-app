module com.example.apitestapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires java.sql;
    requires okhttp3;
    requires static lombok;
    requires com.google.gson;
    requires java.compiler;



    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;




    opens com.example.apitestapp to javafx.fxml;
    opens com.example.apitestapp.controllers to javafx.fxml, javafx.base;
    exports com.example.apitestapp;
    exports com.example.apitestapp.controllers;
    opens com.example.apitestapp.models.entity to com.google.gson, javafx.base;
    opens com.example.apitestapp.models.dto to com.google.gson, javafx.base;
    opens com.example.apitestapp.models.view to com.google.gson, javafx.base;
}
