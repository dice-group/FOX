package org.aksw.fox.legacy.feedback;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.sqlite.SQLiteConfig;

@Deprecated
public class FeedbackDB {

  public static Logger logger = Logger.getLogger(FeedbackDB.class);

  public static String dbName = "foxFeedback";
  public static String textTable = "text";
  public static String feedbackTable = "feedback";
  public static int queryTimeout = 30;

  protected String db = dbName.concat(".db");
  protected Connection connection = null;
  protected Statement statement = null;

  static {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (final ClassNotFoundException e) {
      logger.error("\n", e);
    }
  }

  /**
   * Creates the tables: {@link #textTable} and {@link #feedbackTable} if not exists.
   * 
   */
  public void createTable() {
    if (connect()) {
      try {
        statement.executeUpdate("create table if not exists " + textTable + " ("
            + "id integer primary key not null, " + "time text not null, " + "text text not null, "
            + "gender text, " + "url text, " + "language text" + ")");
        statement.executeUpdate("create table if not exists " + feedbackTable + " (" +
        // "id integer primary key autoincrement, " +
            "textid integer not null," + "entity_uri text not null,"
            + "surface_form text not null, " + "offset integer not null,"
            + "feedback integer not null," + "systems text not null," + "annotation text not null,"
            + "time text not null, " + "FOREIGN KEY(textid) REFERENCES " + textTable + "(id),"
            + "PRIMARY KEY (entity_uri, textid, offset, annotation)" + ")");
      } catch (final SQLException e) {
        logger.error("\n", e);
      } finally {
        disconnect();
      }
    }
  }

  /**
   * Disconnect DB.
   */
  protected void disconnect() {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (final SQLException e) {
      logger.error("\n", e);
    }
  }

  /**
   * Connect DB.
   */
  protected boolean connect() {
    final SQLiteConfig config = new SQLiteConfig();
    // config.setEncoding(SQLiteConfig.Encoding.UTF8);
    return connect(config);
  }

  /**
   * Connect DB.
   * 
   * @param config
   * @return true if connected
   */
  protected boolean connect(final SQLiteConfig config) {
    try {
      connection = DriverManager.getConnection("jdbc:sqlite:".concat(db), config.toProperties());
      statement = connection.createStatement();
      statement.setQueryTimeout(queryTimeout);
    } catch (final SQLException e) {
      logger.error("\n", e);
      statement = null;
    }
    return statement == null ? false : true;
  }
}
