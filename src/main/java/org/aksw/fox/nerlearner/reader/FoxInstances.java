package org.aksw.fox.nerlearner.reader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.TokenCategoryMatrix;
import org.aksw.fox.utils.FoxTextUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

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

    public static Logger logger = Logger.getLogger(FoxInstances.class);

    protected Set<String> token = null;

    /**
     * Gets training instances.
     * 
     * @param input
     *            plain text
     * @param toolResults
     *            found entities of each tool
     * @param oracel
     *            correct training results
     * @return instances object
     */

    public Instances getInstances(Set<String> token, Map<String, Set<Entity>> toolResults, Map<String, String> oracle) {
        if (logger.isDebugEnabled())
            logger.debug("getInstances ...");

        // read oracle
        Map<String, String> oracelToken = new HashMap<>();
        if (oracle != null) {
            for (Entry<String, String> e : oracle.entrySet()) {
                for (String t : FoxTextUtil.getSentenceToken(e.getKey() + "."))
                    if (!t.trim().isEmpty())
                        oracelToken.put(t.trim(), e.getValue());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("oracle:\n" + oracelToken);
            }
        }

        this.token = token;

        // toolResults to make TokenCategory for each tool
        Map<String, TokenCategoryMatrix> toolTokenCategoryMatrix = getTokenCategoryMatrix(toolResults);

        // declare the feature vector
        FastVector featureVector = getFeatureVector(toolTokenCategoryMatrix);

        // train data
        Instances isTrainingSet = new Instances("train data", featureVector, token.size());
        isTrainingSet.setClassIndex(featureVector.size() - 1);

        // fill values
        Instance row = new Instance(isTrainingSet.numAttributes());

        // each row
        int diffNull = 0;
        for (String tok : token) {
            int i = 0; // tool index
            // each tool
            for (String toolname : toolTokenCategoryMatrix.keySet()) {
                int c = 0; // category index
                int start = EntityClassMap.entityClasses.size();
                for (int j = i * start; j < i * start + start; j++) {

                    TokenCategoryMatrix tcm = toolTokenCategoryMatrix.get(toolname);
                    double v = tcm.getValue(tok, EntityClassMap.entityClasses.get(c++)) ? 1.0 : 0.0;

                    row.setValue((Attribute) featureVector.elementAt(j), v);
                }
                i++;
            }
            if (oracle != null) {
                row.setValue((Attribute) featureVector.elementAt(isTrainingSet.numAttributes() - 1), EntityClassMap.oracel(oracelToken.get(tok)));
                if (EntityClassMap.oracel(oracelToken.get(tok)) != EntityClassMap.getNullCategory())
                    diffNull++;
            }

            isTrainingSet.add(row);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("found all: " + (diffNull == oracelToken.size()));
            logger.trace("\n" + isTrainingSet);
        }

        return isTrainingSet;
    }

    /**
     * Gets instances.
     * 
     * @param input
     *            plain text
     * @param toolResults
     *            found entities of each tool
     * @return instances object
     */
    public Instances getInstances(Set<String> token, Map<String, Set<Entity>> toolResults) {
        return getInstances(token, toolResults, null);
    }

    // uses toolResults to make TokenCategoryMatrix object for each tool
    private Map<String, TokenCategoryMatrix> getTokenCategoryMatrix(Map<String, Set<Entity>> toolResults) {
        if (logger.isDebugEnabled())
            logger.debug("getTokenCategoryMatrix ...");

        Set<String> entityClasses = new LinkedHashSet<>();
        entityClasses.addAll(EntityClassMap.entityClasses);

        String nullCategory = EntityClassMap.getNullCategory();
        String tokenSpliter = FoxTextUtil.tokenSpliter;

        // TODO: parallel build of TokenCategoryMatrix
        // each tool init. TokenCategoryMatrix
        Map<String, TokenCategoryMatrix> toolTokenCategoryMatrix = new LinkedHashMap<>();
        for (Entry<String, Set<Entity>> e : toolResults.entrySet())
            toolTokenCategoryMatrix.put(e.getKey(), new TokenCategoryMatrix(token, entityClasses, nullCategory, e.getValue(), tokenSpliter));

        // if (logger.isDebugEnabled())
        // for (Entry<String, TokenCategoryMatrix> e :
        // toolTokenCategoryMatrix.entrySet())
        // logger.debug(e.getKey() + ":\n" + e.getValue().toString());

        return toolTokenCategoryMatrix;
    }

    private FastVector getFeatureVector(Map<String, TokenCategoryMatrix> toolTokenCategoryMatrix) {
        if (logger.isDebugEnabled())
            logger.debug("getFeatureVector ...");

        // Declare the feature vector
        // Declare numeric attribute along with its values
        FastVector featureVector = new FastVector();
        for (String toolname : toolTokenCategoryMatrix.keySet()) {
            for (String cl : EntityClassMap.entityClasses)
                featureVector.addElement(new Attribute(toolname + cl));
        }
        // Declare the class attribute along with its values
        FastVector attVals = new FastVector();
        for (String cl : EntityClassMap.entityClasses)
            attVals.addElement(cl);
        // class att. at last position!
        featureVector.addElement(new Attribute("class", attVals));
        return featureVector;
    }

    public static void main(String[] args) throws Exception {
        // prints arff file from input data
        PropertyConfigurator.configure("log4j.properties");

        TrainingInputReader trainingInputReader = new TrainingInputReader(new String[] { "input/1/1" });
        String input = trainingInputReader.getInput();
        Map<String, String> oracle = trainingInputReader.getEntities();

        Map<String, Set<Entity>> map = new HashMap<>();
        Set<Entity> set = new HashSet<>();

        for (Entry<String, String> e : oracle.entrySet()) {
            set.add(new Entity(e.getKey(), e.getValue()));
        }
        map.put("oracle", set);

        String[] tokenSplit = FoxTextUtil.getSentencesToken(input);
        List<String> token = Arrays.asList(tokenSplit);
        Set<String> tokenSet = new HashSet<>();
        // TODO: replace token with index as we use right now
        tokenSet.addAll(token);

        FoxInstances foxinstances = new FoxInstances();
        Instances instances = foxinstances.getInstances(tokenSet, map);
        System.out.println(instances);

    }
}
