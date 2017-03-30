package org.aksw.fox.tools.ner;

import java.util.List;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ITool;

/**
 * An interface for the use of a ner tool.
 *
 * The method {@link #retrieve(String input)} can be used to get the result list of {@link Entity}
 * objects.
 *
 * @author rspeck
 *
 */
public interface INER extends ITool {

  /**
   * Retrieves Entity objects from the give input String with sentences as plain text.
   *
   * @param input sentences as plain text
   * @return list entities
   */
  public List<Entity> retrieve(String input);

  /**
   * Sets the input with sentences as plain text.
   *
   * @param input
   */
  public void setInput(String input);

  /**
   * Returns results. Uses the {@link #retrieve(String input)} method to get the result list.
   *
   * @return results
   */
  public List<Entity> getResults();
}
