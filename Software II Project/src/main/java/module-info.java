module com.example.software_ii_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.software_ii_project to javafx.fxml;
    exports com.example.software_ii_project;
}