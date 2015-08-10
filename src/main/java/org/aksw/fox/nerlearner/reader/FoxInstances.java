package org.aksw.fox.nerlearner.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.TokenCategoryMatrix;
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

    public static Logger  logger = Logger.getLogger(FoxInstances.class);

    protected Set<String> token  = null;

    /**
     * Gets instances from the given toolResults and oracle.
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

        if (logger.isTraceEnabled())
            logger.trace(toolResults);
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

        //
        this.token = token;

        // toolResults to make TokenCategory for each tool
        Map<String, TokenCategoryMatrix> toolTokenCategoryMatrix = getTokenCategoryMatrix(toolResults);

        // declare the feature vector
        FastVector featureVector = getFeatureVector(toolTokenCategoryMatrix);

        // train data
        Instances instances = new Instances(oracle != null ? "train data" : "test data", featureVector, token.size());
        instances.setClassIndex(featureVector.size() - 1);

        // fill values
        Instance row = new Instance(instances.numAttributes());

        List<String> sortedToolNames = new ArrayList<>(toolTokenCategoryMatrix.keySet());
        Collections.sort(sortedToolNames);

        // each row
        int diffNull = 0;
        for (String tok : token) {
            int i = 0; // tool index

            if (logger.isTraceEnabled())
                logger.trace("token: " + tok);

            for (String toolname : sortedToolNames) {
                if (logger.isTraceEnabled())
                    logger.trace("toolname: " + toolname);
                int c = 0; // category index
                int start = EntityClassMap.entityClasses.size();
                for (int j = i * start; j < i * start + start; j++) {

                    TokenCategoryMatrix tcm = toolTokenCategoryMatrix.get(toolname);
                    double v = tcm.getValue(tok, EntityClassMap.entityClasses.get(c)) ? 1.0 : 0.0;

                    if (logger.isTraceEnabled())
                        logger.trace(j + ": " + c + ": " + v);
                    row.setValue((Attribute) featureVector.elementAt(j), v);

                    c++;
                }
                i++;
            }
            if (oracle != null) {
                row.setValue((Attribute) featureVector.elementAt(instances.numAttributes() - 1), EntityClassMap.oracel(oracelToken.get(tok)));
                if (EntityClassMap.oracel(oracelToken.get(tok)) != EntityClassMap.getNullCategory())
                    diffNull++;
            }

            instances.add(row);
        }

        // DEBUG TRACE
        if (logger.isDebugEnabled()) {
            logger.debug("found all: " + (diffNull == oracelToken.size()));
            logger.trace("\n" + instances);
        }
        // DEBUG TRACE

        return instances;
    }

    /**
     * Gets instances from the given toolResults.
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

        final Set<String> entityClasses = new LinkedHashSet<>(EntityClassMap.entityClasses);
        final Map<String, TokenCategoryMatrix> toolTokenCategoryMatrix = new HashMap<>();
        final CountDownLatch latch = new CountDownLatch(toolResults.entrySet().size());

        List<Fiber> fibers = new ArrayList<>();
        for (final Entry<String, Set<Entity>> entry : toolResults.entrySet()) {
            Fiber fiber = new ThreadFiber();
            fiber.start();
            fiber.execute(new Runnable() {
                public void run() {
                    toolTokenCategoryMatrix.put(
                            entry.getKey(),
                            new TokenCategoryMatrix(
                                    token,
                                    entityClasses,
                                    EntityClassMap.getNullCategory(),
                                    entry.getValue(),
                                    FoxTextUtil.tokenSpliter
                            )
                            );
                    latch.countDown();
                }
            });
            fibers.add(fiber);
        }

        // TODO: time to cfg
        try {
            latch.await(Long.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("\n", e);
        }

        // shutdown threads
        for (Fiber fiber : fibers)
            fiber.dispose();

        return toolTokenCategoryMatrix;
    }

    private FastVector getFeatureVector(Map<String, TokenCategoryMatrix> toolTokenCategoryMatrix) {
        if (logger.isDebugEnabled())
            logger.debug("getFeatureVector ...");

        // declare the feature vector
        // declare numeric attribute along with its values
        FastVector featureVector = new FastVector();
        List<String> sortedToolNames = new ArrayList<>(toolTokenCategoryMatrix.keySet());
        Collections.sort(sortedToolNames);
        for (String toolname : sortedToolNames) {
            for (String cl : EntityClassMap.entityClasses)
                featureVector.addElement(new Attribute(toolname + cl));
        }
        // declare the class attribute along with its values
        FastVector attVals = new FastVector();
        for (String cl : EntityClassMap.entityClasses)
            attVals.addElement(cl);
        // class att. at last position!
        featureVector.addElement(new Attribute("class", attVals));
        return featureVector;
    }
}
