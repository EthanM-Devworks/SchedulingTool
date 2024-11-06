package com.example.software_ii_project;

import java.util.Date;

public class Users {
    private int userId;
    private String userName;
    private String password;

    public Users(int userId, String userName, String password) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
    }

    /**
     * Returns the user's ID.
     * @return userID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Returns the user's username.
     * @return userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns the user's password.
     * @return password
     */
    public String getPassword() {
        return password;
    }

}
