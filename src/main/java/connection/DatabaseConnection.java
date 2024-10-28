package connection;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;

public class DatabaseConnection {
    private final Connection connect;

    public DatabaseConnection() throws SQLException {
        try {
            URL resource = DatabaseConnection.class.getClassLoader().getResource("Currencies.db");
            String path;
            try {
                path = new File(resource.toURI()).getAbsolutePath();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            String driverName = "org.sqlite.JDBC";
            Class.forName(driverName);
            connect = DriverManager.getConnection("jdbc:sqlite:" + path);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return connect;
    }

}