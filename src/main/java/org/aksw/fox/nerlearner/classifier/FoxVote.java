package org.aksw.fox.nerlearner.classifier;

import org.aksw.fox.data.EntityClassMap;
import org.apache.log4j.Logger;

import weka.classifiers.meta.Vote;
import weka.core.Instance;
import weka.core.SelectedTag;
import weka.core.Tag;

public class FoxVote extends Vote {

    private static final long serialVersionUID = -1452748209560313894L;

    public static Logger logger = Logger.getLogger(FoxVote.class);

    public static final int CLASS = 7;
    public static final int RESULT = 8;
    public static final Tag[] FOXTAGS_RULES = {
            new Tag(CLASS, "CLASS", "CLASS"),
            new Tag(RESULT, "RESULT", "RESULT")
    };

    public SelectedTag getCombinationRule() {
        return new SelectedTag(m_CombinationRule, FOXTAGS_RULES);
    }

    /*
     * @Override public double classifyInstance(Instance instance) throws
     * Exception { logger.info("classifyInstance ..."); final int nullIndexInt =
     * instance.classAttribute().indexOfValue(EntityClassMap.getNullCategory());
     * double result = Double.valueOf(nullIndexInt);
     * 
     * double[] results = distributionForInstance(instance); switch
     * (m_CombinationRule) { case CLASS: { int founds = 0; for (int i = 0; i <
     * instance.numClasses(); i++) { if (results[i] > 0 && i != nullIndexInt) {
     * founds++; result = i; } } if (founds > 1) result =
     * Double.valueOf(nullIndexInt);
     * 
     * break; } case RESULT: { logger.info("restult:"); double max = 0; for (int
     * i = 0; i < instance.numClasses(); i++) { logger.info(results[i] + " ");
     * if (results[i] >= max && i != nullIndexInt) { max = results[i]; result =
     * Double.valueOf(i); } } if (max > 0) { int founds = 0; for (int i = 0; i <
     * instance.numClasses(); i++) if (results[i] == max) founds++;
     * 
     * if (founds > 1) { result = Double.valueOf(nullIndexInt); } } break; }
     * default: result = super.classifyInstance(instance); } return result; }
     */

    public double[] distributionForInstance(Instance instance) throws Exception {
        // logger.info("distributionForInstance ...");

        final int nullIndexInt = instance.classAttribute().indexOfValue(EntityClassMap.getNullCategory());

        double[] result = new double[instance.numClasses()];
        for (int i = 0; i < result.length; i++)
            result[i] = 0D;

        double nullIndex = Double.valueOf(instance.classAttribute().indexOfValue(EntityClassMap.getNullCategory()));
        double resultIndex = nullIndex;

        switch (m_CombinationRule) {
        case CLASS: {
            for (int i = 0; i < m_Classifiers.length; i++) {
                resultIndex = m_Classifiers[i].classifyInstance(instance);
                result[Double.valueOf(resultIndex).intValue()] = 1;
            }
        }
        case RESULT: {
            int[] votes = new int[instance.numClasses()];
            for (int i = 0; i < instance.numClasses(); i++)
                votes[i] = 0;

            for (int i = 0; i < m_Classifiers.length; i++) {
                resultIndex = m_Classifiers[i].classifyInstance(instance);
                votes[Double.valueOf(resultIndex).intValue()] += 1;
            }

            int max = 0;
            double maxV = 0D;
            for (int i = 0; i < instance.numClasses() - 1; i++) {
                if (votes[i] > max) {
                    max = i;
                    maxV = votes[i];
                }
            }

            if (maxV == 0D) {
                result[instance.numClasses() - 1] = 1;
            } else {
                int founds = 0;
                for (int i = 0; i < instance.numClasses() - 1; i++) {
                    if (maxV == votes[i]) {
                        founds++;
                    }
                }
                if (founds > 1) {
                    result[nullIndexInt] = 1;
                } else {
                    result[max] = 1;
                }
            }

            break;
        }
        default:
            return super.distributionForInstance(instance);
        }

        return result;
    }

    public void setCombinationRule(SelectedTag newRule) {
        m_CombinationRule = newRule.getSelectedTag().getID();
    }

    public static void main(String[] argv) {
        runClassifier(new FoxVote(), argv);
    }

    public String toString() {

        if (m_Classifiers == null) {
            return "Vote: No model built yet.";
        }

        String result = "Vote combines";
        result += " the probability distributions of these base learners:\n";
        for (int i = 0; i < m_Classifiers.length; i++) {
            result += '\t' + getClassifierSpec(i) + '\n';
        }
        result += "using the '";

        switch (m_CombinationRule) {
        case CLASS:
            result += "class vote";
            break;

        case RESULT:
            result += "result vote";
            break;
        default:
            return super.toString();
        }
        result += "' combination rule \n";

        return result;
    }
}
