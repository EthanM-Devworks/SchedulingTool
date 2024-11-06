package com.example.software_ii_project;

public class ReportsByCountry {

    private String countryName;
    private int countryTotal;

    public ReportsByCountry(String countryName, int countryTotal) {
        this.countryName = countryName;
        this.countryTotal = countryTotal;
    }

    /**
     * Returns the reported country's name.
     * @return countryName
     */
    public String getCountryName() {
        return countryName;
    }

    /**
     * Returns the amount of appointments a country has.
     * @return countryTotal
     */
    public int getCountryTotal() {
        return countryTotal;
    }

}
