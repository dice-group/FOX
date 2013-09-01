package org.aksw.fox.nerlearner;

import org.aksw.fox.nerlearner.classifier.ClassVoteClassifier;
import org.aksw.fox.nerlearner.classifier.ResultVoteClassifier;

import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.core.SelectedTag;

public class FoxClassifierFactory {
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

    public static Classifier getJ48() {
        return new J48();
    }
    /**
     */
    // public Classifier setClassifierStackingC(String[] prefix) {
    // Stacking stacking = new Stacking();
    // // stacking.setMetaClassifier(stacking);
    //
    // stacking.setClassifiers(new Classifier[] {
    // setClassifierMultilayerPerceptron() });
    // this.classifier = stacking;
    // return stacking;
    // }

    /**
     */

    // public Classifier setClassifierADTree() {
    // ADTree adtree = new ADTree();
    //
    // this.classifier = adtree;
    // return adtree;
    // }
}
