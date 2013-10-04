package org.aksw.fox.nertools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.nerlearner.FoxClassifier;
import org.aksw.fox.nerlearner.PostProcessing;
import org.aksw.fox.nerlearner.IPostProcessing;
import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.Logger;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;

/**
 * Runs ner tools and uses this results as input for the {@link FoxClassifier}.
 * 
 * @author rspeck
 * 
 */
public class FoxNERTools {

    public static Logger logger = Logger.getLogger(FoxNERTools.class);
    /**
     * Contains all tools to be used to retrieve entities.
     */
    protected List<INER> nerTools = new ArrayList<>();
    protected Map<String, Set<Entity>> toolResults = new HashMap<>();
    private boolean doTraining = false;

    // use learner and merge
    protected FoxClassifier foxClassifier = new FoxClassifier();

    /**
     * Initializes and fills {@link #nerTools}.
     */
    public FoxNERTools() {
        logger.info("FoxNERTools ...");
        if (FoxCfg.get("nerTools") != null) {
            String[] classes = FoxCfg.get("nerTools").split(",");
            for (String cl : classes) {
                nerTools.add((INER) FoxCfg.getClass(cl));
            }
        }
        initToolResults();
    }

    private void initToolResults() {
        for (INER nerTool : nerTools)
            toolResults.put(nerTool.getToolName(), null);
    }

    /**
     * 
     * @param input
     * @return entities
     */
    public Set<Entity> getEntities(String input) {
        logger.info("get entities ...");

        Set<Entity> results = null;

        // use all tools to retrieve entities
        // start all threads
        List<Fiber> fibers = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(nerTools.size());
        for (INER nerTool : nerTools) {

            nerTool.setCountDownLatch(latch);
            nerTool.setInput(input);

            Fiber fiber = new ThreadFiber();
            fiber.start();
            fiber.execute(nerTool);
            fibers.add(fiber);
        }
        // TODO: if nerTools.size() == 0 we wait for ever here?
        // wait x min for finish
        int min = Integer.parseInt(FoxCfg.get("foxNERLifeTime"));
        try {
            latch.await(min, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("Timeout after " + min + "min.");
            logger.error("\n", e);
            logger.error("input:\n" + input);
        }

        // shutdown threads
        for (Fiber fiber : fibers)
            fiber.dispose();

        // get results
        if (latch.getCount() == 0) {
            // TODO: relevance list
            for (INER nerTool : nerTools)
                toolResults.put(nerTool.getToolName(), new HashSet<Entity>(nerTool.getResults()));

        } else {
            if (logger.isDebugEnabled())
                logger.debug("timeout after " + min + "min.");

            // TODO: handle timeout
        }

        if (doTraining) {
            // TODO: we need changes here?
            // train the FoxClassifier separately

        } else {
            foxClassifier.readClassifier();
            // post
            IPostProcessing pp = new PostProcessing(new TokenManager(input), toolResults);
            // cleaned tool results
            toolResults = pp.getToolResults();

            results = foxClassifier.classify(pp);

            // try {
            // foxClassifier.eva();
            // } catch (Exception e) {
            // logger.error("\n", e);
            // }
        }
        logger.info("get entities done.");
        return results;
    }

    /**
     * 
     * @return results
     */
    public Map<String, Set<Entity>> getToolResult() {
        return toolResults;
    }

    /**
     * 
     * @param doTraining
     */
    public void setTraining(boolean doTraining) {
        this.doTraining = doTraining;
    }
}
