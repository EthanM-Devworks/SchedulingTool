package com.example.software_ii_project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JDBC {

    private static PreparedStatement preparedStatement;
    private static final String protocol = "jdbc";
    private static final String vendor = ":mysql:";
    private static final String location = "//localhost:3306/";
    private static final String databaseName = "client_schedule";
    private static final String jdbcUrl = protocol + vendor + location + databaseName + "?connectionTimeZone = SERVER";
    private static final String driver = "com.mysql.cj.jdbc.Driver";
    private static final String userName = "sqlUser";
    private static String password = "Passw0rd!";
    public static Connection connection;

    /**
     * Opens the connection to the MySQL server, returns the connection while it's at it.
     * @return connection
     */
    public static Connection openConnection()
    {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(jdbcUrl, userName, password);
            System.out.println("Connection successful!");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("Error:" + e.getMessage());
        }
        return connection;
    }

    /**
     * Closes the connection to the MySQL server.
     */
    public static void closeConnection() {
        try {
            connection.close();
            System.out.println("Connection closed!");
        }
        catch(Exception e)
        {
            System.out.println("Error:" + e.getMessage());
        }
    }

    /**
     * Returns the connection to the MySQL server.
     * @return connection
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Sets the connection's current PreparedStatement to given value query.
     * @param connection
     * @param query
     * @throws SQLException
     */
    public static void setPreparedStatement(Connection connection, String query) throws SQLException {
        preparedStatement = connection.prepareStatement(query);
    }

    /**
     * Returns the connection's current PreparedStatement.
     * @return preparedStatement
     */
    public static PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }
}
