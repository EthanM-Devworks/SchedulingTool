package com.example.software_ii_project.DAO;

import com.example.software_ii_project.Countries;
import com.example.software_ii_project.JDBC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CountriesDAO extends Countries {

    public CountriesDAO(int countryID, String country) {
        super(countryID, country);
    }

    /**
     *
     * @return countriesList
     * @throws SQLException
     */
    public static ObservableList<CountriesDAO> getAllCountries() throws SQLException {
        ObservableList<CountriesDAO> countriesList = FXCollections.observableArrayList();
        String query = "SELECT Country_ID, Country from countries";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int countryID = rs.getInt("Country_ID");
            String country = rs.getString("Country");
            CountriesDAO newCountry = new CountriesDAO(countryID, country);
            countriesList.add(newCountry);
        }
        return countriesList;
    }
}
