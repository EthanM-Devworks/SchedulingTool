package com.example.software_ii_project;

public class ReportsByMonth {

    public String monthName;
    public int monthTotal;

    public ReportsByMonth(String monthName, int monthTotal) {
        this.monthName = monthName;
        this. monthTotal = monthTotal;
    }

    /**
     * Returns reported month's name.
     * @return monthName
     */
    public String getMonthName() {
        return monthName;
    }

    /**
     * Returns the amount of appointments a given month has.
     * @return monthTotal
     */
    public int getMonthTotal() {
        return monthTotal;
    }
}
