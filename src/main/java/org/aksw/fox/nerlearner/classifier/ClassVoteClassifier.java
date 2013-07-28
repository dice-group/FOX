package org.aksw.fox.nerlearner.classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
/**
 * @author rspeck
 * 
 */
public class ClassVoteClassifier extends Classifier {

    private static final long serialVersionUID = 708839473310297945L;
    public static Logger logger = Logger.getLogger(ClassVoteClassifier.class);;

    protected String attributePrefix = "";
    public static Map<String, Double> attributeF1 = new HashMap<>();
    public static Map<String, String> catTool = new HashMap<>();

    public ClassVoteClassifier(String attributePrefix) {
        this.attributePrefix = attributePrefix;
    }

    @Override
    public double classifyInstance(Instance instance) {
        // find the 4 cols we need
        // return this one that is > 0
        int indexCat = -1;
        int cols = instance.numValues() - 1;
        for (int i = 0; i < cols; i++)
            if (instance.attribute(i).name().startsWith(attributePrefix))
                if (instance.value(i) > 0) {
                    String cat = instance.attribute(i).name().replace(attributePrefix, "");
                    if (catTool.get(cat).equals(attributePrefix)) {
                        indexCat = instance.classAttribute().indexOfValue(cat);
                        break;
                    }
                }

        if (indexCat != -1) {
            return Double.valueOf(indexCat + "");
        } else {
            return Double.valueOf(new Integer(instance.classAttribute().indexOfValue("NULL")).toString());
        }
    }

    @Override
    public void buildClassifier(Instances instances) throws Exception {
        getCapabilities().testWithFail(instances);
        logger.info("buildClassifier ...");

        /* 1. find f1 to cat */

        // done by each classifier (attributePrefix)
        for (int i = 0; i < instances.classAttribute().numValues(); i++) {
            String category = instances.classAttribute().value(i);
            double f1 = calcF1Score(attributePrefix, category, instances);
            attributeF1.put(attributePrefix + category, f1);
        }

        // DEBUG
        if (attributeF1.size() == (instances.numAttributes() - 1))
            for (Entry<String, Double> e : attributeF1.entrySet())
                logger.debug(e.getKey() + " \t" + e.getValue());
        // DEBUG

        /* 2. we are done, so find tool for cat */

        // do it when finished 1.
        if (attributeF1.size() == (instances.numAttributes() - 1)) {
            // do it just by one tool
            if (catTool.size() == 0) {

                // revert attributeF1
                Map<Double, String> f1Attribute = new HashMap<>();
                for (Entry<String, Double> e : attributeF1.entrySet())
                    f1Attribute.put(e.getValue(), e.getKey());

                // sort
                List<Double> values = new ArrayList<>(attributeF1.values());
                Collections.sort(values, Collections.reverseOrder());

                // from max to min
                for (Double value : values) {
                    String att = f1Attribute.get(value);
                    for (int i = 0; i < instances.classAttribute().numValues(); i++) {
                        String category = instances.classAttribute().value(i);
                        if (att.endsWith(category))
                            if (catTool.get(category) == null)
                                catTool.put(category, att.replace(category, ""));
                    }
                }
            }

            // don't serialize this
            attributeF1 = null;
            // DEBUG
            logger.debug(catTool.toString());
            // DEBUG
        }
    }

    private double calcF1Score(String attributePrefix, String category, Instances instances) {

        double precision = 1.0;
        double recall = 1.0;
        double f1 = 0.0;

        int tp = 0;
        int fp = 0;
        int tn = 0;
        @SuppressWarnings("unused")
        int fn = 0;

        int toolSize = (instances.numAttributes() - 1) / instances.classAttribute().numValues();

        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            int cols = instance.numValues() - 1;

            double oracleValue = instance.value(cols);

            for (int col = 0; col < cols; col++) {
                if (instance.attribute(col).name().startsWith(attributePrefix) && instance.attribute(col).name().endsWith(category)) {

                    double toolValue = instance.value(col);
                    double classValue = Double.valueOf(col % toolSize + "");

                    if (toolValue > 0 && oracleValue == classValue) {
                        tp++;
                    } else if (toolValue <= 0 && oracleValue == classValue) {
                        fp++;
                    } else if (toolValue > 0 && oracleValue != classValue) {
                        tn++;
                    } else if (toolValue <= 0 && oracleValue != classValue) {
                        fn++;
                    }
                }
            }
        }
        if ((tp + fp) > 0) {
            precision = tp / ((tp + fp) * 1.0);
        }
        if ((tp + tn) > 0) {
            recall = tp / ((tp + tn) * 1.0);
        }
        if (precision + recall > 0) {
            f1 = 2 * ((precision * recall) / (precision + recall));
        }
        return f1;
    }
}