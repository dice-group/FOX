package org.aksw.fox.nerlearner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.BILOUEncoding;
import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.utils.FoxTextUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import weka.core.Instances;

/**
 *
 *
 * @author rspeck
 *
 */
public class PostProcessing implements IPostProcessing {

  public static Logger LOG = LogManager.getLogger(PostProcessing.class);

  protected Map<String, Set<Entity>> toolResults = null;
  protected TokenManager tokenManager = null;

  /**
   *
   * PostProcessing.
   *
   * @param input sentences
   * @param toolResults tool name to result set
   */
  public PostProcessing(//
      final TokenManager tokenManager, final Map<String, Set<Entity>> toolResults) {

    LOG.info("PostProcessing ...");

    this.tokenManager = tokenManager;

    // check entities and try to repair entities
    for (final Set<Entity> entites : toolResults.values()) {
      tokenManager.repairEntities(entites);
    }

    this.toolResults = toolResults;

    if (LOG.isDebugEnabled()) {
      LOG.debug(toolResults);
    }
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
  public Map<String, String> getLabeledMap(final Map<String, String> map) {
    Map<String, String> rtn = new HashMap<>();

    // 1. label MWU
    final List<Entry<String, String>> tokenEntities = new ArrayList<>();
    for (final Entry<String, String> mapEntry : map.entrySet()) {
      if (mapEntry.getKey().contains(" ")) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(rtn);
        }
        rtn = labeledEntry(mapEntry, rtn);
        if (LOG.isDebugEnabled()) {
          LOG.debug(rtn);
        }
      } else {
        tokenEntities.add(mapEntry);
      }
    }

    // 2. remember used labels of MWU
    final Set<String> usedLabels = new HashSet<>();
    for (final String label : rtn.keySet()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(label);
      }
      Collections.addAll(usedLabels, FoxTextUtil.getSentencesToken(label));
      if (LOG.isDebugEnabled()) {
        LOG.debug(usedLabels);
      }

    }

    // 3. label token (non MWU)
    for (final Entry<String, String> mapEntry : tokenEntities) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(rtn);
      }
      rtn = labeledEntry(mapEntry, rtn);
      if (LOG.isDebugEnabled()) {
        LOG.debug(rtn);
      }
    }

    // 4. remove labels used in mwu
    final List<String> remove = new ArrayList<>();
    for (final String labeledtoken : rtn.keySet()) {
      if (usedLabels.contains(labeledtoken)) {
        remove.add(labeledtoken);
      }
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(remove);
    }
    for (final String r : remove) {
      rtn.remove(r);
    }

    return rtn;
  }

  /**
   *
   * @return
   */
  @Override
  public Map<String, Set<Entity>> getLabeledToolResults() {
    final Map<String, Set<Entity>> rtn = new HashMap<>();

    if (LOG.isDebugEnabled()) {
      LOG.debug(toolResults);
    }

    // for each tool
    for (final Entry<String, Set<Entity>> entry : toolResults.entrySet()) {

      // entities to map
      Map<String, String> resutlsMap = new HashMap<>();
      for (final Entity entity : entry.getValue()) {
        resutlsMap.put(entity.getText(), entity.getType());
      }

      if (LOG.isTraceEnabled()) {
        LOG.trace(resutlsMap);
      }

      // label map
      resutlsMap = getLabeledMap(resutlsMap);

      if (LOG.isTraceEnabled()) {
        LOG.trace(resutlsMap);
      }

      // label to entity
      final Set<Entity> labeledEntities = new HashSet<>();
      for (final Entry<String, String> e : resutlsMap.entrySet()) {
        labeledEntities
            .add(new Entity(e.getKey(), e.getValue(), Entity.DEFAULT_RELEVANCE, entry.getKey()));
      }

      // add to labeled result map
      final String toolName = entry.getKey();
      rtn.put(toolName, labeledEntities);

    }
    return rtn;
  }

  @Override
  public Map<String, Set<Entity>> getToolResults() {
    return toolResults;
  }

  /**
   * Labeled instances to final entities.
   */
  @Override
  public Set<Entity> instancesToEntities(final Instances instances) {
    LOG.info("instancesToEntities ...");

    // get token in input order
    final Set<String> labeledToken = getLabeledInput();

    // check size
    if (labeledToken.size() != instances.numInstances()) {
      LOG.error("\ntoken size != instance data");
    }

    // fill labeledEntityToken
    final Map<String, String> labeledEntityToken = new LinkedHashMap<>();
    int i = 0;
    for (final String label : labeledToken) {
      final String category = instances.classAttribute()
          .value(Double.valueOf(instances.instance(i).classValue()).intValue());

      if (EntityTypes.AllTypesList.contains(category) && category != BILOUEncoding.O) {
        labeledEntityToken.put(label, category);
      }
      i++;
    }

    // labeledEntityToken to mwu
    final List<Entity> results = new ArrayList<>();

    String previousLabel = "";
    for (final Entry<String, String> entry : labeledEntityToken.entrySet()) {

      final String label = entry.getKey();
      final String category = entry.getValue();

      final String token = tokenManager.getToken(tokenManager.getLabelIndex(label));
      final int labelIndex = tokenManager.getLabelIndex(label);

      // test previous index
      boolean testIndex = false;
      if (results.size() > 0) {
        final int previousIndex = tokenManager.getLabelIndex(previousLabel);
        final int previousTokenLen = tokenManager.getToken(previousIndex).length();

        testIndex = labelIndex == previousIndex + previousTokenLen + " ".length();
      }

      // check previous index and entity category
      if (testIndex && results.get(results.size() - 1).getType().equals(category)) {
        results.get(results.size() - 1).addText(token);
      } else {
        int index = -1;
        try {
          index = Integer.valueOf(label.split(TokenManager.SEP)[1]);
        } catch (final Exception e) {
          LOG.error("\n label: " + label, e);
        }
        if (index > -1) {
          final Entity entity = new Entity(token, category, Entity.DEFAULT_RELEVANCE, "fox");
          entity.addIndicies(index);
          results.add(entity);
        }
      }

      // remember last label
      previousLabel = label;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("result: " + results.toString());
    }
    LOG.info("result size: " + results.size());

    // result set
    // Set<Entity> set = new HashSet<>(results);
    final Set<Entity> set = new HashSet<>(results);
    for (final Entity e : set) {
      for (final Entity ee : results) {
        // word and class equals?
        // merge indices
        if (e.getText().equals(ee.getText()) && e.getType().equals(ee.getType())) {
          e.addAllIndicies(ee.getIndices());
        } else if (e.getText().equals(ee.getText()) && !e.getType().equals(ee.getType())) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("one word 2 classes: " + e.getText());
          }
        }

      }
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("set: " + set.toString());
    }
    LOG.info("result set size: " + set.size());
    return set;
  }

  // label an entity
  protected Map<String, String> labeledEntry(final Entry<String, String> entity,
      final Map<String, String> labeledMap) {

    // token of an entity
    final String[] entityToken = FoxTextUtil.getToken(entity.getKey());
    if (LOG.isDebugEnabled()) {
      LOG.debug(entity);
      LOG.debug(Arrays.asList(entityToken));
    }

    // all entity occurrence
    final Set<Integer> occurrence =
        FoxTextUtil.getIndices(entity.getKey(), tokenManager.getTokenInput());
    if (occurrence.size() == 0) {
      LOG.error("entity not found:" + entity.getKey());
    } else {
      for (Integer index : occurrence) {
        // for each occurrence token length
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < entityToken.length; i++) {

          final String label = tokenManager.getLabel(index);
          if (LOG.isDebugEnabled()) {
            LOG.debug(label);
          }
          if (label != null) {
            sb.append(label);
            if (entityToken[entityToken.length - 1] != label) {
              sb.append(" ");
            }
          }
          index += entityToken[i].length() + 1;
        }
        // add labeled entity
        labeledMap.put(sb.toString().trim(), entity.getValue());
      }
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(labeledMap);
    }
    return labeledMap;
  }
}
