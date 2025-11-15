package core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;

public class DB {
    // JDBC connection string for Apache Derby database
    // "create=true" means it will create the DB folder if it doesn't exist
    private static final String DB_URL = "jdbc:derby:oakdonutsdb;create=true;user=oakdonutsdb;password=oakdonutsdb";

    // Folder inside resources where Flyway will look for migration .sql files
    private static final String MIGRATION_DIR = "db.migrations";

    // Only one DB connection is allowed at a time
    private static DB instance = null;

    // The active database connection
    private final Connection mConnection;

    private DB() throws SQLException {
        migrateDb(); // Create tables if not present
        mConnection = DriverManager.getConnection(DB_URL);
    }

    public static DB getInstance() {
        if (instance == null) {
            try {
                instance = new DB();
            } catch (SQLException ex) {
                System.err.println("Database Error: " + ex.toString());
                System.exit(1);
            }
        }
        return instance;
    }

    public int executeUpdate(String sql) throws SQLException {
        return mConnection.createStatement().executeUpdate(sql);
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return mConnection.createStatement().executeQuery(sql);
    }

    public PreparedStatement getPreparedStatement(String sql) throws SQLException {
        return mConnection.prepareStatement(sql);
    }

    private void migrateDb() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(DB_URL, null, null);//Set the database
        flyway.setLocations(MIGRATION_DIR);//It will run these SQL scripts under Migration Directory
        flyway.migrate();
    }
}