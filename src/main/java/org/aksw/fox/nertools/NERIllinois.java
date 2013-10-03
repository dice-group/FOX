package org.aksw.fox.nertools;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import lbj.NETaggerLevel1;
import lbj.NETaggerLevel2;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.utils.FoxCfg;

import LBJ2.classify.Classifier;
import LBJ2.parse.LinkedVector;
import LbjTagger.BracketFileManager;
import LbjTagger.NETester;
import LbjTagger.NEWord;
import LbjTagger.Parameters;
import LbjTagger.ParametersForLbjCode;

public class NERIllinois extends AbstractNER {
    public static boolean inUse = false;
    public String file = "data/illinois/Config/allLayer1.config";

    public NERIllinois() {
        Parameters.readConfigAndLoadExternalData(file);
        ParametersForLbjCode.forceNewSentenceOnLineBreaks = false;
    }

    public static String bilou2bio(String prediction) {
        if (ParametersForLbjCode.taggingScheme.equalsIgnoreCase(ParametersForLbjCode.BILOU)) {
            if (prediction.startsWith("U-"))
                prediction = "B-" + prediction.substring(2);
            if (prediction.startsWith("L-"))
                prediction = "I-" + prediction.substring(2);
        }
        return prediction;
    }

    // NERTester.clearPredictions(...)
    // public static void clearPredictions(Vector<LinkedVector> data) {
    // for (int k = 0; k < data.size(); k++) {
    // for (int i = 0; i < data.elementAt(k).size(); i++) {
    // ((NEWord) data.elementAt(k).get(i)).neTypeLevel1 = null;
    // ((NEWord) data.elementAt(k).get(i)).neTypeLevel2 = null;
    // }
    // }
    // }

    // NERTester.annotateBothLevels(...)
    /*
     * 
     * use taggerLevel2=null if you want to use only one level of inference
     */
    // public static void annotateBothLevels(Vector<LinkedVector> data,
    // SparseNetworkLearner taggerLevel1, SparseNetworkLearner taggerLevel2) {
    // clearPredictions(data);
    // NETaggerLevel1.isTraining = false;
    // NETaggerLevel2.isTraining = false;
    // for (int k = 0; k < data.size(); k++) {
    // for (int i = 0; i < data.elementAt(k).size(); ++i)
    // {
    // NEWord w = (NEWord) data.elementAt(k).get(i);
    // w.neTypeLevel1 = taggerLevel1.discreteValue(w);
    // }
    // }
    //
    // if (taggerLevel2 != null &&
    // (Parameters.featuresToUse.containsKey("PatternFeatures") ||
    // Parameters.featuresToUse.containsKey("PredictionsLevel1"))) {
    // // annotate with patterns
    // if (Parameters.featuresToUse.containsKey("PatternFeatures"))
    // PatternExtractor.annotate(data, false, false);
    // if (Parameters.featuresToUse.containsKey("PredictionsLevel1")) {
    // GlobalFeatures.aggregateLevel1Predictions(data);
    // GlobalFeatures.aggregateEntityLevelPredictions(data);
    // }
    // for (int k = 0; k < data.size(); k++)
    // for (int i = 0; i < data.elementAt(k).size(); ++i) {
    // ((NEWord) data.elementAt(k).get(i)).neTypeLevel2 =
    // taggerLevel2.discreteValue(data.elementAt(k).get(i));
    // }
    // }
    // else
    // {
    // for (int k = 0; k < data.size(); k++)
    // for (int i = 0; i < data.elementAt(k).size(); i++) {
    // NEWord w = (NEWord) data.elementAt(k).get(i);
    // w.neTypeLevel2 = w.neTypeLevel1;
    // }
    // }
    //
    // if (Parameters.taggingScheme.equalsIgnoreCase(Parameters.BILOU)) {
    // Bio2Bilou.bilou2BioPredictionsLevel1(data);
    // Bio2Bilou.Bilou2BioPredictionsLevel2(data);
    // }
    // }

    @Override
    public Set<Entity> retrieve(String input) {
        logger.info("retrieve ...");
        Set<Entity> set = new HashSet<>();
        try {
            Vector<LinkedVector> data = BracketFileManager.parseText(input);

            NETaggerLevel1 tagger1 = new NETaggerLevel1();
            tagger1 = (NETaggerLevel1) Classifier.binaryRead(ParametersForLbjCode.pathToModelFile + ".level1");
            NETaggerLevel2 tagger2 = new NETaggerLevel2();
            tagger2 = (NETaggerLevel2) Classifier.binaryRead(ParametersForLbjCode.pathToModelFile + ".level2");

            while (inUse) {
                try {
                    Thread.currentThread();
                    Thread.sleep(500);
                    logger.warn("\n\nWaiting ...");
                } catch (InterruptedException e) {
                    logger.error("\n", e);
                }
            }
            inUse = true;
            NETester.annotateBothLevels(data, tagger1, tagger2);
            inUse = false;

            for (int i = 0; i < data.size(); i++) {
                LinkedVector vector = data.elementAt(i);
                // DEBUG
                // for (int j = 0; j < vector.size(); j++) {
                // NEWord w = (NEWord) vector.get(j);
                // logger.debug(w.shapePredLoc);
                // logger.debug(w.shapePredOrg);
                // logger.debug(w.shapePredPer);
                // }
                // DEBUG

                boolean open = false;
                String[] predictions = new String[vector.size()];
                String[] words = new String[vector.size()];
                for (int j = 0; j < vector.size(); j++) {
                    predictions[j] = bilou2bio(((NEWord) vector.get(j)).neTypeLevel2);
                    words[j] = ((NEWord) vector.get(j)).form;
                    // DEBUG
                    if (logger.isTraceEnabled()) {
                        logger.trace(predictions[j]);
                        logger.trace(words[j]);
                    }// DEBUG
                }
                String word = "";
                String tag = "";
                float prob = 0f;
                NEWord w = null;
                for (int j = 0; j < vector.size(); j++) {
                    w = (NEWord) vector.get(j);
                    if (predictions[j].startsWith("B-") ||
                            (j > 0 && predictions[j].startsWith("I-") &&
                            (!predictions[j - 1].endsWith(predictions[j].substring(2))))) {

                        tag = predictions[j].substring(2);
                        prob = 0f;
                        word = new String();
                        open = true;
                    }
                    if (open) {
                        word += words[j] + " ";
                        prob += shapePred(w, tag);
                        // System.out.println(pro(w, tag) + " " + tag);
                        // first one
                        if (prob != Double.valueOf(shapePred(w, tag)).floatValue()) {
                            prob = prob / 2f;
                        }
                    }
                    if (open) {
                        boolean close = false;
                        if (j == vector.size() - 1)
                            close = true;
                        else {
                            if (predictions[j + 1].startsWith("B-"))
                                close = true;
                            if (predictions[j + 1].equals("O"))
                                close = true;
                            if (predictions[j + 1].indexOf('-') > -1 && (!predictions[j].endsWith(predictions[j + 1].substring(2))))
                                close = true;
                        }
                        if (close) {
                            open = false;
                            if (EntityClassMap.illinois(tag) != EntityClassMap.getNullCategory()) {
                                if (FoxCfg.get("illinoisDefaultRelevance") == null || Boolean.valueOf(FoxCfg.get("illinoisDefaultRelevance"))) {
                                    prob = Entity.DEFAULT_RELEVANCE;
                                }
                                set.add(getEntiy(word, EntityClassMap.illinois(tag), prob, getToolName()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("\n", e);
        }

        return post(set);
    }

    protected double shapePred(NEWord w, String tag) {
        switch (EntityClassMap.illinois(tag)) {
        case EntityClassMap.L:
            return w.shapePredLoc;
        case EntityClassMap.P:
            return w.shapePredPer;
        case EntityClassMap.O:
            return w.shapePredOrg;
        }
        return -1;
    }
}
