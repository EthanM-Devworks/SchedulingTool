package com.example.software_ii_project.DAO;

import com.example.software_ii_project.FirstLevelDivisions;
import com.example.software_ii_project.JDBC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FirstLevelDivisionsDAO extends FirstLevelDivisions {

    public FirstLevelDivisionsDAO(int divisionID, String division, int countryID) {
        super(divisionID, division, countryID);
    }

    /**
     * Obtains a list of all first-level divisions from the SQL server.
     * @return firstLevelDivisionsList
     * @throws SQLException
     */
    public static ObservableList<FirstLevelDivisionsDAO> getAllFirstLevelDivisions() throws SQLException {
        ObservableList<FirstLevelDivisionsDAO> firstLevelDivisionsList = FXCollections.observableArrayList();
        String query = "SELECT * from first_level_divisions";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int divisionID = rs.getInt("Division_ID");
            String division = rs.getString("Division");
            int countryID = rs.getInt("COUNTRY_ID");
            FirstLevelDivisionsDAO firstLevelDivision = new FirstLevelDivisionsDAO(divisionID, division, countryID);
            firstLevelDivisionsList.add(firstLevelDivision);
        }
        return firstLevelDivisionsList;
    }
}
