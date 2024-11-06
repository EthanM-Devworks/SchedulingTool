package com.example.software_ii_project.DAO;

import com.example.software_ii_project.JDBC;
import com.example.software_ii_project.Users;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersDAO extends Users {

    public UsersDAO(int userId, String userName, String userPassword) {
        super(userId, userName, userPassword);
    }

    public static String currentUserName = "";

    /**
     * Compares a given username to its password in the SQL server, and if successful, returns said user's ID.
     * @param username
     * @param password
     * @return userID
     */
    public static int userValidate(String username, String password) {
        try {
            String query = "SELECT * FROM users WHERE user_name = '" + username +"' AND password = '" + password + "'";

            PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getString("User_Name").equals(username)) {
                if (rs.getString("Password").equals(password)) {
                    return rs.getInt("User_ID");
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Obtains a list of all users from the SQL server.
     * @return usersList
     * @throws SQLException
     */
    public static ObservableList<Users> getAllUsers() throws SQLException {
        ObservableList<Users> usersList = FXCollections.observableArrayList();
        String query = "SELECT * from users";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int userID = rs.getInt("User_ID");
            String userName = rs.getString("User_Name");
            String userPassword = rs.getString("Password");
            Users user = new Users(userID, userName, userPassword);
            usersList.add(user);
        }
        return usersList;
    }

    /**
     * Returns the username of the currently signed-in user, mostly used for logging purposes.
     * @return currentUserName
     */
    public static String getCurrentUserName() {
        return currentUserName;
    }

    /**
     * Sets currentUserName to the username of the currently signed-in user.
     * @param inputName
     */
    public static void setCurrentUserName(String inputName) {
        currentUserName = inputName;
    }
}
