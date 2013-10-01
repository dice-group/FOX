package org.aksw.fox.nertools;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import lbj.NETaggerLevel1;
import lbj.NETaggerLevel2;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;

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

    @Override
    public Set<Entity> retrieve(String input) {
        logger.info("retrieve ...");
        Set<Entity> set = new HashSet<>();
        try {
            Vector<LinkedVector> data = BracketFileManager.parseText(input);

            NETaggerLevel1 tagger1 = new NETaggerLevel1();
            // System.out.println("Reading model file : "+
            // Parameters.pathToModelFile+".level1");
            tagger1 = (NETaggerLevel1) Classifier.binaryRead(ParametersForLbjCode.pathToModelFile + ".level1");
            NETaggerLevel2 tagger2 = new NETaggerLevel2();
            // System.out.println("Reading model file : "+
            // Parameters.pathToModelFile+".level2");
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

                boolean open = false;
                String[] predictions = new String[vector.size()];
                String[] words = new String[vector.size()];

                for (int j = 0; j < vector.size(); j++) {
                    predictions[j] = bilou2bio(((NEWord) vector.get(j)).neTypeLevel2);
                    words[j] = ((NEWord) vector.get(j)).form;
                    // DEBUG
                    if (logger.isDebugEnabled()) {
                        logger.debug(predictions[j]);
                        logger.debug(words[j]);
                    }// DEBUG
                }
                String word = "";
                String tag = "";
                for (int j = 0; j < vector.size(); j++) {

                    if (predictions[j].startsWith("B-") || (j > 0 && predictions[j].startsWith("I-") && (!predictions[j - 1].endsWith(predictions[j].substring(2))))) {

                        tag = predictions[j].substring(2);
                        word = new String();
                        open = true;
                    }
                    if (open)
                        word += words[j] + " ";
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
                            if (EntityClassMap.illinois(tag) != EntityClassMap.getNullCategory())
                                set.add(getEntiy(word, EntityClassMap.illinois(tag), Entity.DEFAULT_RELEVANCE, getToolName()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("\n", e);
        }

        return post(set);
    }
}
