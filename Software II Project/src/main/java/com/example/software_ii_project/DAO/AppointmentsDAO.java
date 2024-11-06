package com.example.software_ii_project.DAO;

import com.example.software_ii_project.Appointments;
import com.example.software_ii_project.JDBC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class AppointmentsDAO {

    /**
     * Obtains a list of every Appointment from the SQL server.
     * @return appointmentsList
     * @throws SQLException
     */
    public static ObservableList<Appointments> getAllAppointments() throws SQLException {
        ObservableList<Appointments> appointmentsList = FXCollections.observableArrayList();
        String query = "SELECT * from appointments";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int appointmentID = rs.getInt("Appointment_ID");
            String appointmentTitle = rs.getString("Title");
            String appointmentDescription = rs.getString("Description");
            String appointmentLocation = rs.getString("Location");
            String appointmentType = rs.getString("Type");
            LocalDateTime appointmentStart = rs.getTimestamp("Start").toInstant().atZone(ZoneId.of(ZoneId.systemDefault().getId())).toLocalDateTime();
            LocalDateTime appointmentEnd = rs.getTimestamp("End").toInstant().atZone(ZoneId.of(ZoneId.systemDefault().getId())).toLocalDateTime();
            int customerID = rs.getInt("Customer_ID");
            int userID = rs.getInt("User_ID");
            int contactID = rs.getInt("Contact_ID");
            Appointments appointment = new Appointments(appointmentID, appointmentTitle, appointmentDescription, appointmentLocation, appointmentType, appointmentStart, appointmentEnd, customerID, userID, contactID);
            appointmentsList.add(appointment);
        }

        return appointmentsList;
    }

    /**
     * Deletes an Appointment from the SQL server using a given appointment ID.
     * @param appointmentID
     * @param connection
     * @return result
     * @throws SQLException
     */
    public static int deleteAppointment(int appointmentID, Connection connection) throws SQLException {
        String query = "DELETE FROM appointments WHERE Appointment_ID=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, appointmentID);
        int result = ps.executeUpdate();
        ps.close();
        return result;
    }

    /**
     * Deletes an Appointment from the SQL server where Customer_ID is equal to given int.
     * @param customerID
     * @param connection
     * @return result
     * @throws SQLException
     */
    public static int deleteAppointmentByCustomer(int customerID, Connection connection) throws SQLException {
        String query = "DELETE FROM appointments WHERE Customer_ID=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, customerID);
        int result = ps.executeUpdate();
        ps.close();
        return result;
    }
}
