package com.example.software_ii_project;

import java.util.Date;

public class FirstLevelDivisions {
    private int divisionID;
    private String division;
    public int countryID;

    public FirstLevelDivisions(int divisionID, String division, int countryID) {
        this.divisionID = divisionID;
        this.division = division;
        this.countryID = countryID;
    }

    /**
     * Returns a division's ID.
     * @return divisionID
     */
    public int getDivisionId() {
        return divisionID;
    }

    /**
     * Returns a division's name.
     * @return division
     */
    public String getDivision() {
        return division;
    }

    /**
     * Returns a division's related country ID.
     * @return countryID
     */
    public int getCountryId() {
        return countryID;
    }
}
