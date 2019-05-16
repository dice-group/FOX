package org.aksw.fox.tools.re;

import java.util.List;
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
  Set<Relation> extract();

  /**
   * Sets the input.
   *
   * @param text
   * @param entities entities in the text with types, labels and indices.
   */
  void setInput(String input, List<Entity> entities);

  /**
   * Returns results.
   *
   * @return results
   */
  Set<Relation> getResults();

}
