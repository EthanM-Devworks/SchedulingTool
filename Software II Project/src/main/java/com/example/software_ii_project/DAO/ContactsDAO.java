package com.example.software_ii_project.DAO;

import com.example.software_ii_project.Contacts;
import com.example.software_ii_project.JDBC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContactsDAO {

    /**
     * Obtains a list of every Contact from the SQL server.
     * @return contactsList
     * @throws SQLException
     */
    public static ObservableList<Contacts> getAllContacts() throws SQLException {
        ObservableList<Contacts> contactsList = FXCollections.observableArrayList();
        String query = "SELECT * FROM contacts";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int contactID = rs.getInt("Contact_ID");
            String contactName = rs.getString("Contact_Name");
            String email = rs.getString("Email");
            Contacts contact = new Contacts(contactID, contactName, email);
            contactsList.add(contact);
        }
        return contactsList;
    }

    /**
     * Obtains a given contact's ID from the SQL server.
     * @param contactID
     * @return contactID
     * @throws SQLException
     */
    public static String getContactIDFromServer(String contactID) throws SQLException {
        PreparedStatement ps = JDBC.getConnection().prepareStatement("SELECT * FROM contacts WHERE Contact_Name = ?");
        ps.setString(1, contactID);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            contactID = rs.getString("Contact_ID");
        }
        return contactID;
    }
}
