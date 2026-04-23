module com.example.apitestapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;


    opens com.example.apitestapp to javafx.fxml;
    exports com.example.apitestapp;
}