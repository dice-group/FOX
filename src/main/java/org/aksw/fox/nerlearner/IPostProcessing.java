package org.aksw.fox.nerlearner;

import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;

import weka.core.Instances;

public interface IPostProcessing {
    /**
     * Set of labels that is used to classify.
     * 
     * @return
     */
    public Set<String> getLabeledInput();

    /**
     * Gets cleaned tool results.
     * 
     * @return tool results
     */
    public Map<String, Set<Entity>> getToolResults();

    /**
     * Gets labeled tool results.
     * 
     * @return labeled tool results
     */
    public Map<String, Set<Entity>> getLabeledToolResults();

    /**
     * Map of label to class that is used as oracle.
     * 
     * @param oracle
     * @return labeled oracle
     */
    public Map<String, String> getLabeledMap(Map<String, String> oracle);

    /**
     * 
     * @param instances
     * @return
     */
    public Set<Entity> instancesToEntities(Instances instances);
}
