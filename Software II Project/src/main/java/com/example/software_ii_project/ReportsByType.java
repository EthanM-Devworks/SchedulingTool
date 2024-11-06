package com.example.software_ii_project;

public class ReportsByType {

    private String appointmentType;
    private int appointmentTotal;

    public ReportsByType(String appointmentType, int appointmentTotal) {
        this.appointmentType = appointmentType;
        this.appointmentTotal = appointmentTotal;
    }

    /**
     * Returns reported appointment type.
     * @return appointmentType
     */
    public String getAppointmentType() {
        return appointmentType;
    }

    /**
     * Returns the amount of appointments that fall under a given type.
     * @return appointmentTotal
     */
    public int getAppointmentTotal() {
        return appointmentTotal;
    }

}
