package com.example.software_ii_project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    /**
     * Prepares the program, then opens the log-in form.
     * @param stage
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("log-in-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 225, 200);
        stage.setTitle("Log In");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {

        JDBC.openConnection();
        launch();
        JDBC.closeConnection();
    }
}