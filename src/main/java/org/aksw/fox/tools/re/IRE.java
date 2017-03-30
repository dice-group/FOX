package org.aksw.fox.tools.re;

import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.aksw.fox.tools.ITool;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public interface IRE extends ITool {
  /***
   * Extracts relations from the given text.
   *
   * @param text
   * @param entities
   *
   * @return relations
   */
  public Set<Relation> extract(String text, List<Entity> entities);

  /**
   * Sets the input.
   *
   * @param input
   */
  public void setInput(String input, List<Entity> entities);

  /**
   * Returns results.
   *
   * @return results
   */
  public Set<Relation> getResults();

}
