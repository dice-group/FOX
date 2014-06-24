package org.aksw.fox.nerlearner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.utils.FoxTextUtil;
import org.apache.log4j.Logger;

import weka.core.Instances;

/**
 * 
 * 
 * @author rspeck
 * 
 */
public class PostProcessing implements IPostProcessing {

    public static Logger               logger       = Logger.getLogger(PostProcessing.class);

    protected Map<String, Set<Entity>> toolResults  = null;

    protected TokenManager             tokenManager = null;

    /**
     * 
     * PostProcessing.
     * 
     * @param input
     *            sentences
     * @param toolResults
     *            tool name to result set
     */
    public PostProcessing(TokenManager tokenManager, Map<String, Set<Entity>> toolResults) {
        logger.info("PostProcessing ...");

        this.tokenManager = tokenManager;

        // check entities and try to repair entities
        for (Set<Entity> entites : toolResults.values())
            tokenManager.repairEntities(entites);

        this.toolResults = toolResults;
    }

    /**
     * 
     * @return
     */
    @Override
    public Set<String> getLabeledInput() {
        return tokenManager.getLabeledInput();
    }

    /**
     * Label oracle.
     * 
     * @param map
     * @return
     */
    @Override
    public Map<String, String> getLabeledMap(Map<String, String> map) {

        Map<String, String> labeledMap = new HashMap<>();

        // 1. label MWU
        // remember token entities
        List<Entry<String, String>> tokenEntry = new ArrayList<>();

        for (Entry<String, String> entry : map.entrySet())
            // mwu entities 1st
            if (entry.getKey().contains(" "))
                labeledMap = labeledEntry(entry, labeledMap);
            else
                tokenEntry.add(entry);

        // 2. remember used labels of MWU
        Set<String> usedLabels = new HashSet<>();
        for (String label : labeledMap.keySet())
            Collections.addAll(usedLabels, FoxTextUtil.getSentencesToken(label));

        // 3. label token (non MWU)
        for (Entry<String, String> entry : tokenEntry)
            labeledMap = labeledEntry(entry, labeledMap);

        // 4. remove labels used in mwu
        List<String> remove = new ArrayList<>();
        for (String labeledtoken : labeledMap.keySet())
            if (usedLabels.contains(labeledtoken))
                remove.add(labeledtoken);

        for (String r : remove)
            labeledMap.remove(r);

        return labeledMap;
    }

    /**
     * 
     * @return
     */
    @Override
    public Map<String, Set<Entity>> getLabeledToolResults() {
        Map<String, Set<Entity>> labeledToolResults = new HashMap<>();

        for (Entry<String, Set<Entity>> entry : toolResults.entrySet()) {

            // entities to map
            Map<String, String> resutlsMap = new HashMap<>();
            for (Entity entity : entry.getValue())
                resutlsMap.put(entity.getText(), entity.getType());

            // label map
            resutlsMap = getLabeledMap(resutlsMap);

            // label to entity
            Set<Entity> labeledEntities = new HashSet<>();
            for (Entry<String, String> e : resutlsMap.entrySet()) {
                labeledEntities.add(new Entity(e.getKey(), e.getValue(), Entity.DEFAULT_RELEVANCE, entry.getKey()));
            }

            // add to labeled result map
            String toolName = entry.getKey();
            labeledToolResults.put(toolName, labeledEntities);

        }
        return labeledToolResults;
    }

    @Override
    public Map<String, Set<Entity>> getToolResults() {
        return toolResults;
    }

    /**
     * Labeled instances to final entities.
     */
    @Override
    public Set<Entity> instancesToEntities(Instances instances) {
        logger.info("instancesToEntities ...");

        // get token in input order
        Set<String> labeledToken = getLabeledInput();

        // check size
        if (labeledToken.size() != instances.numInstances())
            logger.error("\ntoken size != instance data");

        // fill labeledEntityToken
        Map<String, String> labeledEntityToken = new LinkedHashMap<>();
        int i = 0;
        for (String label : labeledToken) {
            String category = instances.classAttribute().value(Double.valueOf(instances.instance(i).classValue()).intValue());
            // logger.info(category);
            if (EntityClassMap.entityClasses.contains(category) && category != EntityClassMap.getNullCategory())
                labeledEntityToken.put(label, category);
            i++;
        }

        // labeledEntityToken to mwu
        List<Entity> results = new ArrayList<>();

        String previousLabel = "";
        for (Entry<String, String> entry : labeledEntityToken.entrySet()) {

            String label = entry.getKey();
            String category = entry.getValue();

            String token = tokenManager.getToken(tokenManager.getLabelIndex(label));
            int labelIndex = tokenManager.getLabelIndex(label);

            // test previous index
            boolean testIndex = false;
            if (results.size() > 0) {
                int previousIndex = tokenManager.getLabelIndex(previousLabel);
                int previousTokenLen = tokenManager.getToken(previousIndex).length();

                testIndex = labelIndex == previousIndex + previousTokenLen + " ".length();
            }

            // check previous index and entity category
            if (testIndex && results.get(results.size() - 1).getType().equals(category)) {
                results.get(results.size() - 1).addText(token);
            } else {
                int index = -1;
                try {
                    index = Integer.valueOf(label.split(TokenManager.SEP)[1]);
                } catch (Exception e) {
                    logger.error("\n label: " + label, e);
                }
                if (index > -1) {
                    Entity entity = new Entity(token, category, Entity.DEFAULT_RELEVANCE, "fox");
                    entity.addIndicies(index);
                    results.add(entity);
                }
            }

            // remember last label
            previousLabel = label;
        }
        logger.info("result: " + results.toString());
        logger.info("result size: " + results.size());

        // result set
        // Set<Entity> set = new HashSet<>(results);
        Set<Entity> set = new HashSet<>(results);
        for (Entity e : set)
            for (Entity ee : results) {
                // word and class equals?
                // merge indices
                if (e.getText().equals(ee.getText()) && e.getType().equals(ee.getType())) {
                    e.addAllIndicies(ee.getIndices());
                } else if (e.getText().equals(ee.getText()) && !e.getType().equals(ee.getType())) {
                    logger.debug("one word 2 classes: " + e.getText());
                }

            }
        logger.info("set: " + set.toString());
        logger.info("result set size: " + set.size());
        return set;
    }

    // label an entity
    protected Map<String, String> labeledEntry(Entry<String, String> entity, Map<String, String> labeledMap) {

        // token of an entity
        String[] entityToken = FoxTextUtil.getToken(entity.getKey());

        // all entity occurrence
        Set<Integer> occurrence = FoxTextUtil.getIndices(entity.getKey(), tokenManager.getTokenInput());
        if (occurrence.size() == 0) {
            logger.error("entity not found:" + entity.getKey());
        }

        for (Integer index : occurrence) {
            // for each occurrence token length
            StringBuffer labeledToken = new StringBuffer();

            for (int i = 0; i < entityToken.length; i++) {

                String label = tokenManager.getLabel(index);
                if (label != null) {
                    labeledToken.append(label);

                    if (entityToken[entityToken.length - 1] != label)
                        labeledToken.append(" ");
                }

                index += entityToken[i].length() + 1;
            }

            // add labeled entity
            labeledMap.put(labeledToken.toString().trim(), entity.getValue());
        }
        return labeledMap;
    }
}
