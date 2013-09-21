package org.aksw.fox.web.feedback;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.sqlite.SQLiteConfig;

public class FeedbackStore {

    static {
        loadDriver();
    }

    private static Logger logger = Logger.getLogger(FeedbackStore.class);
    public static String dbName = "foxFeedback";
    public static String tableName = "feedback";
    public static int queryTimeout = 30; // sec.

    private String db = dbName.concat(".db");
    private Connection connection = null;

    /**
     * Test method.
     * 
     */
    public static void main(String[] args) {
        FeedbackStore fb = new FeedbackStore();
        fb.insert("test");
        fb.select();
    }

    public FeedbackStore() {
        createTable();
    }

    // TODO check input
    public void insert(String input) {
        Statement statement = connect();
        long time = System.currentTimeMillis() / 1000L;

        try {
            statement.executeUpdate(
                    "insert into " + tableName +
                            " (time, input) values" +
                            " ('" + time + "', '" + input + "')"
                    );
        } catch (SQLException e) {
            logger.error("\n", e);
        } finally {
            disconnect();
        }
    }

    // TODO
    public void select() {
        Statement statement = connect();
        try {
            ResultSet rs = statement.executeQuery("select * from " + tableName);
            while (rs.next()) {
                logger.info("time = " + rs.getInt("time"));
                logger.info("input = " + rs.getString("input"));
            }
        } catch (SQLException e) {
            logger.error("\n", e);
        } finally {
            disconnect();
        }
    }

    protected void disconnect() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            logger.error("\n", e);
        }
    }

    protected Statement connect() {
        SQLiteConfig config = new SQLiteConfig();
        // config.setEncoding(SQLiteConfig.Encoding.UTF8);
        return connect(config);
    }

    protected Statement connect(SQLiteConfig config) {
        Statement statement = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:".concat(db), config.toProperties());
            statement = connection.createStatement();
            statement.setQueryTimeout(queryTimeout);
        } catch (SQLException e) {
            logger.error("\n", e);
            statement = null;
        }
        return statement;
    }

    /**
     * Creates a table with the name: {@link #tableName} if not exists.
     * 
     */
    public void createTable() {
        Statement statement = connect();
        try {
            statement.executeUpdate(
                    "create table if not exists " + tableName + " (" +
                            "id integer primary key autoincrement, " +
                            "time text not null, " +
                            "input text   not null" +
                            ")"
                    );
        } catch (SQLException e) {
            logger.error("\n", e);
        } finally {
            disconnect();
        }
    }

    /**
     * Loads org.sqlite.JDBC diver installation.
     * 
     * @return true if diver was found.
     */
    public static boolean loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        } catch (ClassNotFoundException e) {
            logger.error("\n", e);
            return false;
        }
    }
}