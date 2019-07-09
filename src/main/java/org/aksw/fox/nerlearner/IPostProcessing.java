package org.aksw.fox.nerlearner;

import java.util.List;
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
   * Gets labeled tool results.
   *
   * @return labeled tool results
   */
  Map<String, List<Entity>> getLabeledToolResults();

  /**
   * Map of label to class that is used as oracle.
   *
   * @param oracle
   * @return labeled oracle
   */
  Map<String, String> getLabeledMap(Map<String, String> oracle);

}
