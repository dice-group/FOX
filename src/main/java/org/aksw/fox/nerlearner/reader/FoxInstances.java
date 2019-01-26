package org.aksw.fox.nerlearner.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.aksw.fox.data.BILOUEncoding;
import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.nerlearner.TokenCategoryMatrix;
import org.aksw.fox.utils.FoxTextUtil;
import org.apache.log4j.Logger;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 *
 * @author rspeck
 *
 */
public class FoxInstances {

  public static Logger LOG = Logger.getLogger(FoxInstances.class);

  protected Set<String> tokens = null;

  /**
   * Gets instances from the given toolResults and oracle.
   *
   * @param input plain text
   * @param toolResults found entities of each tool
   * @param oracel correct training results
   * @return instances object
   */
  public Instances getInstances(//
      final Set<String> tokens, final Map<String, Set<Entity>> toolResults,
      final Map<String, String> oracle) {

    LOG.debug("getInstances...");

    LOG.debug("oracle size: " + oracle.size());
    LOG.debug("tokens size: " + tokens.size());

    if (oracle != null) {
      LOG.debug("Training data:");
      oracle.entrySet().stream().forEach(LOG::debug);
    }

    // clean oracle to token
    final Map<String, String> oracelTokens = cleanTokens(oracle);

    //
    this.tokens = tokens;

    // toolResults to make TokenCategory for each tool
    final Map<String, TokenCategoryMatrix> toolTokenCategoryMatrix;
    toolTokenCategoryMatrix = getTokenCategoryMatrix(toolResults);

    LOG.debug("TokenCategoryMatrix for each tool:");
    toolTokenCategoryMatrix.entrySet().stream().forEach(LOG::debug);

    // declare the feature vector
    final FastVector featureVector;
    featureVector = getFeatureVector(toolTokenCategoryMatrix.keySet(), EntityTypes.AllTypesSet);

    // train data
    final Instances instances;
    {
      final String name = oracle != null ? "train data" : "test data";
      instances = new Instances(name, featureVector, tokens.size());
      instances.setClassIndex(featureVector.size() - 1);
    }

    // fill values

    final List<String> sortedToolNames = new ArrayList<>(toolTokenCategoryMatrix.keySet());
    Collections.sort(sortedToolNames);

    int diffNull = 0;
    for (final String token : tokens) {
      final Instance row = new Instance(instances.numAttributes());

      // one token per row
      int toolIndex = 0;
      for (final String toolname : sortedToolNames) {
        final TokenCategoryMatrix tcm = toolTokenCategoryMatrix.get(toolname);
        int typeIndex = 0;
        final int numOfTypes = EntityTypes.AllTypesList.size();
        for (int index = toolIndex * numOfTypes; index < toolIndex * numOfTypes
            + numOfTypes; index++) {

          final boolean isSet = tcm.getValue(token, EntityTypes.AllTypesList.get(typeIndex++));
          final double v = isSet ? 1.0 : 0.0;

          row.setValue((Attribute) featureVector.elementAt(index), v);
        }
        toolIndex++;
      } // end for tool name

      if (oracle != null) {
        final String value;
        value = oracelTokens.get(token) == null ? BILOUEncoding.O : oracelTokens.get(token);

        // TODO: map from oracel to fox types
        final Attribute att = (Attribute) featureVector.elementAt(instances.numAttributes() - 1);

        row.setValue(att, value);

        if (!value.equals(BILOUEncoding.O)) {
          diffNull++;
        }
      }
      instances.add(row);
    } // end for token

    LOG.info("# instances: " + instances.numInstances());

    LOG.debug("#token with a type: " + diffNull);
    LOG.debug("found all: " + (diffNull == oracelTokens.size()));
    LOG.debug("\n" + instances);

    return instances;
  }

  /**
   * Gets instances from the given toolResults.
   *
   * @param input plain text
   * @param toolResults found entities of each tool
   * @return instances object
   */
  public Instances getInstances(final Set<String> token,
      final Map<String, Set<Entity>> toolResults) {
    return getInstances(token, toolResults, null);
  }

  // uses toolResults to make TokenCategoryMatrix object for each tool
  private Map<String, TokenCategoryMatrix> getTokenCategoryMatrix(
      final Map<String, Set<Entity>> toolResults) {

    final Map<String, TokenCategoryMatrix> toolTokenCategoryMatrix = new HashMap<>();
    final CountDownLatch latch = new CountDownLatch(toolResults.entrySet().size());

    final List<Fiber> fibers = new ArrayList<>();
    for (final Entry<String, Set<Entity>> entry : toolResults.entrySet()) {
      final Fiber fiber = new ThreadFiber();
      fiber.start();
      fiber.execute(() -> {
        toolTokenCategoryMatrix.put(//
            entry.getKey(), //
            new TokenCategoryMatrix(tokens, EntityTypes.AllTypesSet, BILOUEncoding.O,
                entry.getValue(), FoxTextUtil.tokenSpliter)//
        );
        latch.countDown();
      });
      fibers.add(fiber);
    }

    try {
      latch.await(Long.MAX_VALUE, TimeUnit.MINUTES);
    } catch (final InterruptedException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    // shutdown threads
    for (final Fiber fiber : fibers) {
      fiber.dispose();
    }

    return toolTokenCategoryMatrix;
  }

  private FastVector getFeatureVector(final Set<String> toolnames, final Set<String> types) {

    final Set<String> sortedToolNames = new TreeSet<>(toolnames);
    final Set<String> sortedTypes = new TreeSet<>(types);

    // declare the feature vector
    final FastVector fv = new FastVector();

    // declare numeric attribute along with its values
    for (final String sortedToolName : sortedToolNames) {
      for (final String cl : sortedTypes) {
        final String attributeName = sortedToolName + cl;
        fv.addElement(new Attribute(attributeName));
      }
    }

    // declare the class attribute along with its values
    final FastVector attVals = new FastVector();
    for (final String cl : types) {
      attVals.addElement(cl);
    }

    // class att. at last position!
    fv.addElement(new Attribute("class", attVals));
    return fv;
  }

  private Map<String, String> cleanTokens(final Map<String, String> oracle) {

    final Map<String, String> oracelToken = new HashMap<>();
    if (oracle != null) {
      for (final Entry<String, String> e : oracle.entrySet()) {
        for (final String t : FoxTextUtil.getSentenceToken(e.getKey() + ".")) {
          if (!t.trim().isEmpty()) {
            oracelToken.put(t.trim(), e.getValue());
          }
        }
      }
    }
    return oracelToken;
  }
}
