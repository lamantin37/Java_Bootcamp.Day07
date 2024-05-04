package edu.school21.DatabaseManager;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static Connection connection;

    public static final String DB_URL = "jdbc:h2:~/test";
    public static final String DB_USERNAME = "sa";
    public static final String DB_PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = configureDataSource().getConnection();
        }
        return connection;
    }

    public static HikariDataSource configureDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(DB_URL);
        ds.setUsername(DB_USERNAME);
        ds.setPassword(DB_PASSWORD);
        ds.setDriverClassName("org.h2.Driver");
        return ds;
    }
}

