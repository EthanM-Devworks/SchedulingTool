package com.example.software_ii_project.DAO;

import com.example.software_ii_project.Appointments;
import com.example.software_ii_project.JDBC;
import com.example.software_ii_project.ReportsByCountry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ReportsDAO extends Appointments {

    public ReportsDAO(int appointmentID, String appointmentTitle, String appointmentDescription, String appointmentLocation, String appointmentType, LocalDateTime appointmentStart, LocalDateTime appointmentEnd, int appointmentCustomerID, int appointmentUserID, int appointmentContactID) {
        super(appointmentID, appointmentTitle, appointmentDescription, appointmentLocation, appointmentType, appointmentStart, appointmentEnd, appointmentCustomerID, appointmentUserID, appointmentContactID);
    }

    /**
     * Creates a report based on how many appointments come from a certain country.
     * @return reportsList
     * @throws SQLException
     */
    public static ObservableList<ReportsByCountry> getCountryReport() throws SQLException {
        ObservableList<ReportsByCountry> reportsList = FXCollections.observableArrayList();
        String query = "select countries.Country, count(*) as countryCount from customers inner join first_level_divisions on customers.Division_ID = first_level_divisions.Division_ID inner join countries on countries.Country_ID = first_level_divisions.Country_ID where  customers.Division_ID = first_level_divisions.Division_ID group by first_level_divisions.Country_ID order by count(*) desc";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String countryName = rs.getString("Country");
            int countryTotal = rs.getInt("countryCount");
            ReportsByCountry reportsByCountry = new ReportsByCountry(countryName, countryTotal);
            reportsList.add(reportsByCountry);
        }
        return reportsList;
    }
}
