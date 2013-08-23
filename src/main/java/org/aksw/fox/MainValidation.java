package org.aksw.fox;

import java.util.Set;

import org.aksw.fox.nerlearner.FoxClassifier;
import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.PropertyConfigurator;

import weka.classifiers.Classifier;
import weka.classifiers.meta.Vote;
import weka.core.SelectedTag;

public class MainValidation {
    static {
        PropertyConfigurator.configure("log4j.properties");
    }

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {

        // TODO: remove FoxClassifier dependency?
        FoxClassifier foxClassifier = new FoxClassifier();

        Set<String> toolResultKeySet = CrossValidation.foxNERTools.getToolResult().keySet();
        String[] prefix = toolResultKeySet.toArray(new String[toolResultKeySet.size()]);

        CrossValidation.logger.info("tools used: " + toolResultKeySet);

        switch (FoxCfg.get("learner").trim()) {
        case "result_vote": {

            foxClassifier.setClassifierResultVote(prefix, new SelectedTag(Vote.AVERAGE_RULE, Vote.TAGS_RULES));
            break;
        }
        case "class_vote": {
            foxClassifier.setClassifierClassVote(prefix, new SelectedTag(Vote.MAX_RULE, Vote.TAGS_RULES));
            break;
        }
        default:
        case "mp":
            foxClassifier.setClassifierMultilayerPerceptron();
        }

        Classifier cls = foxClassifier.getClassifier();

        try {
            CrossValidation.crossValidation(cls, new String[] { "input/small_test" });
        } catch (Exception e) {
            CrossValidation.logger.error("\n", e);
        }
    }
}
