package org.aksw.fox.nerlearner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.utils.FoxTextUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

abstract class AProcessing {

  public static Logger LOG = LogManager.getLogger(AProcessing.class);
  protected Map<String, Set<Entity>> toolResults = null;
  protected TokenManager tokenManager = null;
}


/**
 * TODO: split to pre- and post- processing
 *
 * @author rspeck
 *
 */
public class PostProcessing extends AProcessing implements IPostProcessing {

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
        rtn = labeledEntry(mapEntry, rtn);
      } else {
        tokenEntities.add(mapEntry);
      }
    }

    // 2. remember used labels of MWU
    final Set<String> usedLabels = new HashSet<>();
    for (final String label : rtn.keySet()) {
      Collections.addAll(usedLabels, FoxTextUtil.getSentencesToken(label));
    }

    // 3. label token (non MWU)
    for (final Entry<String, String> mapEntry : tokenEntities) {
      rtn = labeledEntry(mapEntry, rtn);
    }

    // 4. remove labels used in mwu
    final List<String> remove = new ArrayList<>();
    for (final String labeledtoken : rtn.keySet()) {
      if (usedLabels.contains(labeledtoken)) {
        remove.add(labeledtoken);
      }
    }

    // remove elements
    remove.stream().forEach(rtn::remove);

    return rtn;
  }

  /**
   *
   * @return
   */
  @Override
  public Map<String, Set<Entity>> getLabeledToolResults() {

    final Map<String, Set<Entity>> rtn = new HashMap<>();

    // for each tool
    for (final Entry<String, Set<Entity>> entry : toolResults.entrySet()) {
      // entities to map
      Map<String, String> resutlsMap = new HashMap<>();
      for (final Entity entity : entry.getValue()) {
        resutlsMap.put(entity.getText(), entity.getType());
      }

      // label map
      resutlsMap = getLabeledMap(resutlsMap);

      // label to entity
      final Set<Entity> labeledEntities = new HashSet<>();
      for (final Entry<String, String> e : resutlsMap.entrySet()) {
        labeledEntities.add(//
            new Entity(e.getKey(), e.getValue(), Entity.DEFAULT_RELEVANCE, entry.getKey())//
        );
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

  // label an entity
  protected Map<String, String> labeledEntry(//
      final Entry<String, String> entity, final Map<String, String> labeledMap) {

    // token of an entity
    final String[] entityToken = FoxTextUtil.getToken(entity.getKey());

    // all entity occurrence
    final Set<Integer> occurrence;
    occurrence = FoxTextUtil.getIndices(entity.getKey(), tokenManager.getTokenInput());
    if (occurrence.size() == 0) {
      LOG.error("entity not found:" + entity.getKey());
    } else {
      for (Integer index : occurrence) {
        // for each occurrence token length
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < entityToken.length; i++) {

          final String label = tokenManager.getLabel(index);
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
    return labeledMap;
  }
}
