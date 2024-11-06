package com.example.software_ii_project;

public class Customers {
    private int customerId;
    private String customerName;
    private String customerAddress;
    private String customerPostal;
    private String customerPhone;
    private int customerDivisionID;
    private String customerDivisionName;

    public Customers(int customerId, String customerName, String customerAddress, String customerPostal, String customerPhone, int customerDivisionID, String customerDivisionName) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerPostal = customerPostal;
        this.customerPhone = customerPhone;
        this.customerDivisionID = customerDivisionID;
        this.customerDivisionName = customerDivisionName;
    }

    /**
     * Returns a customer's ID.
     * @return customerId
     */
    public int getCustomerId() {
        return customerId;
    }

    /**
     * Returns a customer's name.
     * @return customerName
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Returns a customer's address.
     * @return customerAddress
     */
    public String getCustomerAddress() {
        return customerAddress;
    }

    /**
     * Returns a customer's postal code.
     * @return customerPostal
     */
    public String getCustomerPostal() {
        return customerPostal;
    }

    /**
     * Returns a customer's phone number.
     * @return customerPhone
     */
    public String getCustomerPhone() {
        return customerPhone;
    }

    /**
     * Returns a customer's related division ID.
     * @return customerDivisionID
     */
    public int getCustomerDivisionID() {
        return customerDivisionID;
    }

    /**
     * Returns a customer's related division name.
     * @return customerDivisionName
     */
    public String getCustomerDivisionName() {
        return customerDivisionName;
    }
}
