package org.aksw.fox.nerlearner;

import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;

public interface IPostProcessing {
  /**
   * Set of labels that is used to classify.
   *
   * @return
   */
  Set<String> getLabeledInput();

  /**
   * Gets cleaned tool results.
   *
   * @return tool results
   */
  Map<String, Set<Entity>> getToolResults();

  /**
   * Gets labeled tool results.
   *
   * @return labeled tool results
   */
  Map<String, Set<Entity>> getLabeledToolResults();

  /**
   * Map of label to class that is used as oracle.
   *
   * @param oracle
   * @return labeled oracle
   */
  Map<String, String> getLabeledMap(Map<String, String> oracle);

}
