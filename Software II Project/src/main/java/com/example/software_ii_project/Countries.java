package com.example.software_ii_project;

import java.util.Date;

public class Countries {
    private int countryID;
    private String country;

    public Countries(int countryID, String country) {
        this.countryID = countryID;
        this.country = country;
    }

    /**
     * Returns a country's ID.
     * @return countryID
     */
    public int getCountryId() {
        return countryID;
    }

    /**
     * Returns a country's name.
     * @return country
     */
    public String getCountry() {
        return country;
    }

}
