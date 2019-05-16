package org.aksw.fox.data.decode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.encode.BILOUEncoding;
import org.aksw.fox.nerlearner.TokenManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import weka.core.Instances;

/**
 * BILOUDecoding decodes {@link Entity} instances.
 *
 * @author rspeck
 *
 */
public class BILOUDecoding {

  protected Logger LOG = LogManager.getLogger(BILOUDecoding.class);

  protected IDecoding decoding = null;

  /**
   *
   * @param decoding
   */
  public BILOUDecoding(final IDecoding decoding) {
    this.decoding = decoding;
  }

  /**
   *
   * @param instances
   * @param i
   * @return
   */
  private String getCategory(final Instances instances, final int i) {
    return instances.classAttribute()
        .value(Double.valueOf(instances.instance(i).classValue()).intValue());
  }

  /**
   *
   * @param tokenManager
   * @param instances
   * @param labeledToken
   * @return
   */
  public List<Entity> instancesToEntities(//
      final TokenManager tokenManager, final Instances instances, final Set<String> labeledToken) {

    LOG.debug("instancesToEntities ... with " + instances.numInstances() + " instances");

    if (labeledToken.size() != instances.numInstances()) {
      throw new UnsupportedOperationException("token size != instance size");
    }

    final Map<String, String> labeledEntityToken = new LinkedHashMap<>();

    int i = 0;
    for (final String label : labeledToken) {

      final String category = getCategory(instances, i);
      LOG.info(label + " : " + category);
      if (BILOUEncoding.AllTypesSet.contains(category) && category != BILOUEncoding.O) {
        labeledEntityToken.put(label, category);
      }
      i++;
    }

    LOG.info("labeledEntityToken: " + labeledEntityToken);

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
          results.add(//
              new Entity(token, category, Entity.DEFAULT_RELEVANCE, "fox", index)//
          );
        }
      }

      // remember last label
      previousLabel = label;
    }
    LOG.info("result size: " + results.size());

    return decoding.decode(results);
  }
}
