package org.aksw.fox.tools.re;

import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.aksw.fox.tools.ITool;

/**
 * Interface for the relation extraction tools.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public interface IRE extends ITool {
  /***
   * Extracts relations from the given text.
   *
   * @return relations
   */
  public Set<Relation> extract();

  /**
   * Sets the input.
   *
   * @param text the whole text with multiple sentences as clean as possible.
   * @param entities entities in the text with types, labels and indices.
   */
  public void setInput(String input, Set<Entity> entities);

  /**
   * Returns results.
   *
   * @return results
   */
  public Set<Relation> getResults();

}
