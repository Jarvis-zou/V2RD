module com.example.v2rd2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.example.v2rd2 to javafx.fxml;
    exports com.example.v2rd2;
}