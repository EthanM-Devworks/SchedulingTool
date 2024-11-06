package com.example.software_ii_project;

import java.time.LocalDateTime;

public class Appointments {
    private int appointmentID;
    private String appointmentTitle;
    private String appointmentDescription;
    private String appointmentLocation;
    private String appointmentType;
    private LocalDateTime appointmentStart;
    private LocalDateTime appointmentEnd;
    private int appointmentCustomerID;
    private int appointmentUserID;
    private int appointmentContactID;

    public Appointments(int appointmentID, String appointmentTitle, String appointmentDescription, String appointmentLocation, String appointmentType, LocalDateTime appointmentStart, LocalDateTime appointmentEnd, int appointmentCustomerID, int appointmentUserID, int appointmentContactID) {
        this.appointmentID = appointmentID;
        this.appointmentTitle = appointmentTitle;
        this.appointmentDescription = appointmentDescription;
        this.appointmentLocation = appointmentLocation;
        this.appointmentType = appointmentType;
        this.appointmentStart = appointmentStart;
        this.appointmentEnd = appointmentEnd;
        this.appointmentCustomerID = appointmentCustomerID;
        this.appointmentUserID = appointmentUserID;
        this.appointmentContactID = appointmentContactID;
    }

    /**
     * Returns appointment ID.
     * @return appointmentID
     */
    public int getAppointmentID() {
        return appointmentID;
    }

    /**
     * Returns appointment title.
     * @return appointmentTitle
     */
    public String getAppointmentTitle() {
        return appointmentTitle;
    }

    /**
     * Returns appointment description.
     * @return appointmentDescription
     */
    public String getAppointmentDescription() {
        return appointmentDescription;
    }

    /**
     * Returns appointment location.
     * @return appointmentLocation
     */
    public String getAppointmentLocation() {
        return appointmentLocation;
    }

    /**
     * Returns appointment type.
     * @return appointmentType
     */
    public String getAppointmentType() {
        return appointmentType;
    }

    /**
     * Returns appointment start time/date.
     * @return appointmentStart
     */
    public LocalDateTime getAppointmentStart() {
        return appointmentStart;
    }

    /**
     * Returns appointment end time/date.
     * @return appointmentEnd
     */
    public LocalDateTime getAppointmentEnd() {
        return appointmentEnd;
    }

    /**
     * Returns appointment's related customer ID.
     * @return appointmentCustomerID
     */
    public int getAppointmentCustomerID() {
        return appointmentCustomerID;
    }

    /**
     * Returns appointment's related user ID.
     * @return appointmentUserID
     */
    public int getAppointmentUserID() {
        return appointmentUserID;
    }

    /**
     * Returns appointment's related contact ID.
     * @return appointmentContactID
     */
    public int getAppointmentContactID() {
        return appointmentContactID;
    }
}
