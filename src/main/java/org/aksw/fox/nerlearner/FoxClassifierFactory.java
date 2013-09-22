package org.aksw.fox.nerlearner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.aksw.fox.nerlearner.classifier.ClassVoteClassifier;
import org.aksw.fox.nerlearner.classifier.ResultVoteClassifier;
import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.meta.Vote;
import weka.core.SelectedTag;

public class FoxClassifierFactory {
    public static Logger logger = Logger.getLogger(FoxClassifierFactory.class);

    /**
     * Sets ResultVote as classifier with a combination rule.
     * 
     * @param prefix
     *            tool attribute prefixes
     */
    public static Classifier getClassifierResultVote(String[] prefix) {
        return getClassifierVote("result", prefix, new SelectedTag(Vote.AVERAGE_RULE, Vote.TAGS_RULES));
    }

    /**
     * Sets ClassVote as classifier with a combination rule.
     * 
     * @param prefix
     *            tool attribute prefixes
     */
    public static Classifier getClassifierClassVote(String[] prefix) {
        return getClassifierVote("class", prefix, new SelectedTag(Vote.MAX_RULE, Vote.TAGS_RULES));
    }

    private static Classifier getClassifierVote(String type, String[] prefix, SelectedTag rule) {

        Classifier[] classifier = new Classifier[prefix.length];
        for (int i = 0; i < prefix.length; i++) {
            switch (type) {
            case "class":
                classifier[i] = new ClassVoteClassifier(prefix[i]);
                break;
            case "result":
                classifier[i] = new ResultVoteClassifier(prefix[i]);
                break;
            }
        }

        Vote vote = new Vote();
        vote.setClassifiers(classifier);
        vote.setCombinationRule(rule);

        return vote;
    }

    /**
     * Sets MultilayerPerceptron as classifier.
     */
    public static Classifier getClassifierMultilayerPerceptron() {
        MultilayerPerceptron multilayerPerceptron = new MultilayerPerceptron();
        /*
         * 'a' = (attribs + classes) / 2, 'i' = attribs, 'o' = classes , 't' =
         * attribs + classes so "a, 6", which would give you (attribs + classes)
         * / 2 nodes in the first hidden layer and 6 in the second.
         */

        // mp.setHiddenLayers(hidden);
        return multilayerPerceptron;
    }

    public static Classifier get(String wekaClassifier, String[] options) {
        Classifier classifier = get(wekaClassifier);
        if (classifier != null && classifier instanceof Classifier)
            try {
                classifier.setOptions(options);
            } catch (Exception e) {
                logger.error("\n", e);
            }
        return classifier;
    }

    public static Classifier get(String wekaClassifier) {
        Class<?> clazz = null;
        Classifier classifier = null;
        try {
            clazz = Class.forName(wekaClassifier);
            if (clazz != null) {
                Constructor<?> constructor = clazz.getConstructor();
                classifier = (Classifier) constructor.newInstance();
            }
        } catch (ClassNotFoundException e) {
            logger.error("\n", e);
        } catch (NoSuchMethodException | SecurityException e) {
            logger.error("\n", e);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.error("\n", e);
        }

        return classifier;
    }
}
