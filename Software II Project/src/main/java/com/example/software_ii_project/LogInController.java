package com.example.software_ii_project;

import com.example.software_ii_project.DAO.AppointmentsDAO;
import com.example.software_ii_project.DAO.UsersDAO;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LogInController implements Initializable {
    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Text logInText;

    @FXML
    private Text locationLabel;

    @FXML
    private Button submitButton;

    @FXML
    private Text watermarkText;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss z");


    /**
     * Handles the log-in button's actions, including:
     * - Validating log-in credentials, throwing an error if they don't match.
     * - Logging successful log-ins.
     * - Reminding the user if they have an appointment 15 minutes before or after log-in.
     * - Setting currentUserName in UsersDAO to the username of the recently logged-in user.
     * - Sending the user to the main view.
     * @param event
     * @throws SQLException
     * @throws IOException
     * @throws Exception
     */
    public void handleSubmitButtonAction(ActionEvent event) throws SQLException, IOException, Exception {
        try {
            ResourceBundle rb = ResourceBundle.getBundle("login", Locale.getDefault());

            boolean needsReminder = false;
            ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
            LocalDateTime timeMinus15Minutes = LocalDateTime.now().minusMinutes(15);
            LocalDateTime timePlus15Minutes = LocalDateTime.now().plusMinutes(15);
            int appointmentID = 0;
            LocalDateTime shownTime = null;

            System.out.println("Current time is: " + LocalDateTime.now());

            int userID = UsersDAO.userValidate(usernameField.getText(), passwordField.getText());

            FileWriter logger = new FileWriter("login_history.txt", true);
            PrintWriter outputFile = new PrintWriter(logger);

            if (userID > 0) {
                for(Appointments a: allAppointments) {
                    System.out.println("Checking appointment " + a.getAppointmentID() + " with start time " + a.getAppointmentStart());
                    if ((a.getAppointmentStart().isAfter(timeMinus15Minutes) || a.getAppointmentStart().isEqual(timeMinus15Minutes)) && (a.getAppointmentStart().isBefore(timePlus15Minutes) || a.getAppointmentStart().isEqual(timePlus15Minutes)) && a.getAppointmentUserID() == userID) {
                        appointmentID = a.getAppointmentID();
                        shownTime = a.getAppointmentStart();
                        needsReminder = true;
                        System.out.println("Appointment within 15 minutes detected for user " + userID);
                    }
                }

                if (needsReminder) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Meeting ID #" + appointmentID + " starts within 15 minutes at " + shownTime + ".");
                    alert.showAndWait();
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "You have no appointments within 15 minutes.");
                    alert.showAndWait();
                }

                UsersDAO.setCurrentUserName(usernameField.getText());
                outputFile.print(usernameField.getText() + " successfully logged in at " + Timestamp.valueOf(LocalDateTime.now()) + "\n");
                outputFile.close();
                Parent root = (Parent) FXMLLoader.load(this.getClass().getResource("main-view.fxml"));
                Stage stage = new Stage();
                stage.setTitle("NightOwl Scheduler");
                stage.setScene(new Scene(root, 1200.0, 800.0));
                stage.show();

                Stage thisStage = (Stage) submitButton.getScene().getWindow();
                thisStage.close();
            }
            else if (userID < 0) {
                outputFile.print("Username: " + usernameField.getText() + ", Password: " + passwordField.getText() + " failed log-in at " + Timestamp.valueOf(LocalDateTime.now()) + "\n");
                outputFile.close();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(rb.getString("Error"));
                alert.setContentText(rb.getString("ErrorMessage"));
                alert.show();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks the user's system language, and translates the log-in form to french if need be.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try{

            Locale locale = Locale.getDefault();
            Locale.setDefault(locale);

            locationLabel.setText(ZoneId.systemDefault().toString());

            rb = ResourceBundle.getBundle("login", Locale.getDefault());
            logInText.setText(rb.getString("Login"));
            usernameField.setPromptText(rb.getString("Username"));
            passwordField.setPromptText(rb.getString("Password"));
            submitButton.setText(rb.getString("Submit"));
            watermarkText.setText(rb.getString("Watermark"));
        }
        catch(MissingResourceException e) {
            System.out.println("Resource file not found: " + e);
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
}
