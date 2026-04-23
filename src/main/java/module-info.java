module com.example.apitestapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.apitestapp to javafx.fxml;
    exports com.example.apitestapp;
}