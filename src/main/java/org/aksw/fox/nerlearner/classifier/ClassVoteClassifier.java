package org.aksw.fox.nerlearner.classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.fox.data.BILOUEncoding;
import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * It's a weka Classifier and a wrapper class for all NER classifiers.
 *
 * @author rspeck
 *
 */
public class ClassVoteClassifier extends Classifier {

  private static final long serialVersionUID = 708839473310297945L;
  public static Logger logger = Logger.getLogger(ClassVoteClassifier.class);;

  // NER tool names
  protected String attributePrefix = "";
  public static Map<String, Double> attributeF1 = new HashMap<>();
  public static Map<String, String> catTool = new HashMap<>();

  public ClassVoteClassifier(final String attributePrefix) {
    this.attributePrefix = attributePrefix;
  }

  @Override
  public double classifyInstance(final Instance instance) {
    int indexCat = -1;
    for (int i = 0; i < instance.numValues() - 1; i++) {
      if (instance.attribute(i).name().startsWith(attributePrefix)) {
        if (instance.value(i) > 0) {
          final String cat = instance.attribute(i).name().replace(attributePrefix, "");
          if (catTool.get(cat).equals(attributePrefix)) {
            indexCat = instance.classAttribute().indexOfValue(cat);
            break;
          }
        }
      }
    }

    if (indexCat != -1) {
      return Double.valueOf(indexCat);
    } else {
      return Double.valueOf(instance.classAttribute().indexOfValue(BILOUEncoding.O));
    }
  }

  @Override
  public void buildClassifier(final Instances instances) throws Exception {
    getCapabilities().testWithFail(instances);
    logger.info("buildClassifier ...");

    // f1
    for (int i = 0; i < instances.classAttribute().numValues(); i++) {
      final String category = instances.classAttribute().value(i);
      final double f1 = calcF1Score(attributePrefix, category, instances);
      attributeF1.put(attributePrefix + category, f1);
    }

    // after last build
    if (attributeF1.size() == instances.numAttributes() - 1) {
      catTool.clear();
      final List<Double> sortedValues = new ArrayList<>(attributeF1.values());
      Collections.sort(sortedValues, Collections.reverseOrder());
      for (final Double sortedValue : sortedValues) {
        for (final Entry<String, Double> entry : attributeF1.entrySet()) {
          if (entry.getValue() == sortedValue) {
            logger.info(entry.getKey() + " \t" + entry.getValue());
            for (int i = 0; i < instances.classAttribute().numValues(); i++) {
              final String category = instances.classAttribute().value(i);
              if (entry.getKey().endsWith(category)) {
                if (catTool.get(category) == null) {
                  catTool.put(category, entry.getKey().replace(category, ""));
                }
              }
            }
          }
        }
      }

      // don't serialize this
      attributeF1 = new HashMap<>();
      // DEBUG
      logger.info(catTool.toString());
      // DEBUG
    }
  }

  private double calcF1Score(final String attributePrefix, final String category,
      final Instances instances) {

    int tp = 0, fp = 0, tn = 0, fn = 0;

    final int categoryIndex = instances.classAttribute().indexOfValue(category);

    // each instance
    for (int i = 0; i < instances.numInstances(); i++) {
      final Instance instance = instances.instance(i);

      final int oracleIndex =
          Double.valueOf(instance.value(instances.numAttributes() - 1)).intValue();
      final double toolValue = instance.value(instances.attribute(attributePrefix + category));

      if (toolValue > 0 && oracleIndex == categoryIndex) {
        tp++;
      } else if (toolValue <= 0 && oracleIndex == categoryIndex) {
        fp++;
      } else if (toolValue > 0 && oracleIndex != categoryIndex) {
        tn++;
      } else if (toolValue <= 0 && oracleIndex != categoryIndex) {
        fn++;
      }
    }

    logger.info(attributePrefix + " " + category + ": ");
    logger.info("tp: " + tp + " fp: " + fp + " tn: " + tn + " fn: " + fn);

    double precision = 0.0, recall = 0.0, f1 = 0.0;
    if (tp + fp > 0) {
      precision = tp / Double.valueOf(tp + fp);
    }
    if (tp + tn > 0) {
      recall = tp / Double.valueOf(tp + tn);
    }
    if (precision + recall > 0) {
      f1 = 2 * (Double.valueOf(precision * recall) / Double.valueOf(precision + recall));
    }

    logger.info("precision: " + precision + " recall: " + recall + " f1: " + f1);

    return f1;
  }
}
