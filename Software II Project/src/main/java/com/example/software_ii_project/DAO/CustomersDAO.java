package com.example.software_ii_project.DAO;

import com.example.software_ii_project.Customers;
import com.example.software_ii_project.JDBC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomersDAO {

    /**
     * Obtains a list of all customers from the SQL server.
     * @return customersList
     * @throws SQLException
     */
    public static ObservableList<Customers> getAllCustomers() throws SQLException {
        String query = "SELECT customers.Customer_ID, customers.Customer_Name, customers.Address, customers.Postal_Code, customers.Phone, customers.Division_ID, first_level_divisions.Division from customers INNER JOIN  first_level_divisions ON customers.Division_ID = first_level_divisions.Division_ID";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        ObservableList<Customers> customersList = FXCollections.observableArrayList();

        while (rs.next()) {
            int customerID = rs.getInt("Customer_ID");
            String customerName = rs.getString("Customer_Name");
            String address = rs.getString("Address");
            String postalCode = rs.getString("Postal_Code");
            String phone = rs.getString("Phone");
            int divisionID = rs.getInt("Division_ID");
            String divisionName = rs.getString("Division");
            Customers customer = new Customers(customerID, customerName, address, postalCode, phone, divisionID, divisionName);
            customersList.add(customer);
        }
        return customersList;
    }
}
