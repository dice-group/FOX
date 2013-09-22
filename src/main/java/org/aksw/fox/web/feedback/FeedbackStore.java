package org.aksw.fox.web.feedback;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sqlite.SQLiteConfig;

public class FeedbackStore {

    static {
        loadDriver();
    }

    private static Logger logger = Logger.getLogger(FeedbackStore.class);
    public static String dbName = "foxFeedback";
    public static String textTable = "text";
    public static String feedbackTable = "feedback";
    public static int queryTimeout = 30; // sec.

    private String db = dbName.concat(".db");
    private Connection connection = null;
    private Statement statement = null;

    /**
     * Test method.
     * 
     */
    public static void main(String[] args) {
        FeedbackStore fb = new FeedbackStore();
        fb.select();
    }

    public FeedbackStore() {
        createTable();
    }

    private long getTime() {
        return System.currentTimeMillis() / 1000L;
    }

    private int getTextID(String text) throws SQLException {
        int id = 0;
        ResultSet resultSet = statement.executeQuery("select id from " + textTable + " where text='" + text + "'");
        if (resultSet.next()) {
            id = resultSet.getInt("id");
        }
        return id;
    }

    public void insert(Map<String, String> parameter) {

        if (connect())
            try {
                long time = getTime();
                String text = parameter.get("text");
                int textid = getTextID(text);

                if (textid < 1) {
                    statement.executeUpdate(
                            "insert into " + textTable +
                                    " (time, text) values" +
                                    " ('" + time + "', '" + text + "')"
                            );
                    textid = getTextID(text);
                }

                if (textid > 0) {
                    statement.executeUpdate(
                            "insert into " + feedbackTable +
                                    " (" +
                                    "textid, " +
                                    "entityUri, " +
                                    "surfaceForm, " +
                                    "offset, " +
                                    "feedback, " +
                                    "context, " +
                                    "feedbackType, " +
                                    "system, " +
                                    "annotation,  " +
                                    "time  " +
                                    ") values ( '" +
                                    textid + "', '" +
                                    parameter.get("entityUri") + "', '" +
                                    parameter.get("surfaceForm") + "', '" +
                                    parameter.get("offset") + "', '" +
                                    parameter.get("feedback") + "', '" +
                                    parameter.get("context") + "', '" +
                                    parameter.get("feedbackType") + "', '" +
                                    parameter.get("system") + "', '" +
                                    parameter.get("annotation") + "', '" +
                                    time +
                                    "')"
                            );
                }
            } catch (SQLException e) {
                logger.error("\n", e);
            } finally {
                disconnect();
            }
    }

    // TODO
    public void select() {
        if (connect())
            try {
                ResultSet rs = statement.executeQuery("select * from " + textTable);

                while (rs.next()) {
                    logger.info("id = " + rs.getInt("id") + " text = " + rs.getString("text"));
                }
                rs = statement.executeQuery("select * from " + feedbackTable);
                while (rs.next()) {
                    logger.info("textid = " + rs.getInt("textid") + " entityUri = " + rs.getString("entityUri"));
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

    protected boolean connect() {
        SQLiteConfig config = new SQLiteConfig();
        // config.setEncoding(SQLiteConfig.Encoding.UTF8);
        return connect(config);
    }

    protected boolean connect(SQLiteConfig config) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:".concat(db), config.toProperties());
            statement = connection.createStatement();
            statement.setQueryTimeout(queryTimeout);
        } catch (SQLException e) {
            logger.error("\n", e);
            statement = null;
        }
        return statement == null ? false : true;
    }

    /**
     * Creates the tables: {@link #textTable} and {@link #feedbackTable} if not
     * exists.
     * 
     */
    public void createTable() {
        if (connect())
            try {
                statement.executeUpdate(
                        "create table if not exists " + textTable + " (" +
                                "id integer primary key not null, " +
                                "time text not null, " +
                                "text text not null " +
                                ")"
                        );
                statement.executeUpdate(
                        "create table if not exists " + feedbackTable + " (" +
                                "id integer primary key autoincrement, " +
                                "textid integer not null," +
                                "entityUri text not null," +
                                "surfaceForm text not null, " +
                                "offset integer not null," +
                                "feedback text not null," +
                                "context text not null," +
                                "feedbackType text not null," +
                                "system text not null," +
                                "annotation text not null," +
                                "time text not null" +
                                ")"
                        );
            } catch (SQLException e) {
                logger.error("\n", e);
            } finally {
                disconnect();
            }
    }

    /**
     * Loads org.sqlite.JDBC diver.
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