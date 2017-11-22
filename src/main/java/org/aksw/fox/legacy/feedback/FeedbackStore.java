package org.aksw.fox.legacy.feedback;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

// data helper
class TextEntry {
  public Integer id = null;
  public String time = "";
  public String text = "";
  public String url = "";
  public String gender = "";
  public String language = "";
}


// data helper
class FeedbackEntry {
  public Integer textid = null;
  public String entity_uri = "";
  public String surface_form = "";
  public Integer offset = null;
  public String feedback = null;
  public String systems = "";
  public String annotation = "";
  public String manual = "";
  public String time = "";

  @Override
  public String toString() {
    String rtn = "{";
    rtn += "textid:" + textid + " ";
    rtn += "entity_uri:" + entity_uri + " ";
    rtn += "offset:" + offset + " ";
    rtn += "annotation:" + annotation + "}";

    return rtn;
  }
}


@Deprecated
public class FeedbackStore extends FeedbackDB {

  public String errorMessage = "";

  public FeedbackStore() {
    createTable();
  }

  private long getTime() {
    return System.currentTimeMillis() / 1000L;
  }

  private int getTextID(final String text) throws SQLException {
    int id = -1;
    final String sql = "select id from " + textTable + " where text=?";
    final PreparedStatement prep = connection.prepareStatement(sql);
    prep.setString(1, text);
    final ResultSet resultSet = prep.executeQuery();
    if (resultSet.next()) {
      id = resultSet.getInt("id");
    }
    return id;
  }

  /**
   * 
   * Inserts text and feedback from parameter.
   * 
   * @param parameter
   * @return true if inserted
   */
  public boolean insert(final TextEntry textEntry, final List<FeedbackEntry> feedbackEntries) {
    boolean done = false;
    if (connect()) {
      try {
        logger.info("inserting ...");
        final Long time = getTime();
        final String text = textEntry.text;
        int textid = getTextID(text);
        String sql = "";

        if (textid < 0) {
          logger.info("inserting text ...");

          sql = "insert into " + textTable
              + " (time, text, gender, url, language ) values ( ? ,?, ? ,? ,?  )";
          final PreparedStatement prep = connection.prepareStatement(sql);

          prep.setString(1, time.toString());
          prep.setString(2, text);
          prep.setString(3, textEntry.gender);
          prep.setString(4, textEntry.url);
          prep.setString(5, textEntry.language);
          prep.execute();
          prep.close();
          textid = getTextID(text);
        } else {
          logger.info("known text, nothing todo ...");
        }

        if (textid >= 0) {
          logger.info("inserting feedback ...");

          for (final FeedbackEntry fe : feedbackEntries) {
            try {
              fe.textid = textid;
              insert(time, fe);
            } catch (final SQLException e) {
              logger.warn("\n", e);
              errorMessage = e.getLocalizedMessage();
              break;
            }
          }
          if (errorMessage.isEmpty()) {
            done = true;
          }

          if (!done) {
            logger.warn("Couldn't execute statement!");
          } else {
            logger.info("Inserted a new row into " + feedbackTable + " table");
          }
        }
      } catch (final SQLException e) {
        logger.warn("\n", e);
        errorMessage = e.getLocalizedMessage();
      } finally {
        disconnect();
      }
    }
    return done;
  }

  private void insert(final Long time, final FeedbackEntry fe) throws SQLException {

    logger.info(fe);

    final String sql =
        "insert into " + feedbackTable + " ( textid, entity_uri, surface_form, offset, feedback,"
            + " systems, annotation, time) values" + " ( ?, ?, ?, ?, ?, ?, ?, ?)";

    final PreparedStatement prep = connection.prepareStatement(sql);
    prep.setInt(1, fe.textid);
    prep.setString(2, fe.entity_uri);
    prep.setString(3, fe.surface_form);
    prep.setInt(4, fe.offset);
    prep.setString(5, fe.feedback);
    prep.setString(6, fe.systems);
    prep.setString(7, fe.annotation);
    prep.setString(8, time.toString());
    prep.execute();
    prep.close();
  }
}
