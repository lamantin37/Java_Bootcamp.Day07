package edu.school21.app;

import com.sun.org.apache.xpath.internal.operations.Or;
import edu.school21.DatabaseManager.DatabaseManager;
import edu.school21.annotation.*;
import edu.school21.models.*;

import com.zaxxer.hikari.HikariDataSource;

import javax.annotation.processing.RoundEnvironment;
import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;


public class Program {

    public static final String DB_URL = "jdbc:h2:~/test";
    public static final String DB_USERNAME = "sa";
    public static final String DB_PASSWORD = "";

    public static void main(String[] args) throws SQLException {
        Connection connection = DatabaseManager.getConnection();
        System.out.println("Processing ormManager...");

        System.out.println("Connection: " + connection);
        OrmManager ormManager = new OrmManager(connection);

        User user = new User();

        System.out.println("Locking for user...");
        Long id = 1L;
        User user1 = ormManager.findById(id, User.class);

        System.out.println("Updating user...");
        ormManager.update(user);

        System.out.println("Saving new user...");
        ormManager.save(new User());
        System.out.println("Finishing program...");
    }
}
