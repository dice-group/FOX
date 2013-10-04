package org.aksw.fox.nertools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Entity;
import org.aksw.fox.utils.FoxTextUtil;
import org.apache.log4j.Logger;

public class AbstractNER implements INER {

    public static Logger logger = Logger.getLogger(AbstractNER.class);
    protected List<Entity> entitySet = null;
    protected CountDownLatch cdl = null;
    protected String input = null;

    @Override
    public List<Entity> retrieve(String input) {
        return new ArrayList<>();
    }

    @Override
    public String getToolName() {
        return getClass().getSimpleName();
    }

    @Override
    public void run() {
        if (input != null)
            entitySet = clean(retrieve(input));
        else
            logger.error("Input not set!");

        if (cdl != null)
            cdl.countDown();
        else
            logger.warn("CountDownLatch not set!");
    }

    @Override
    public List<Entity> getResults() {
        return entitySet;
    }

    @Override
    public void setCountDownLatch(CountDownLatch cdl) {
        this.cdl = cdl;
    }

    @Override
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * Creates a new Entity object.
     * 
     * @param text
     * @param type
     * @param relevance
     * @param tool
     * @return
     */
    protected Entity getEntity(String text, String type, float relevance, String tool) {
        return new Entity(text, type, relevance, tool);
    }

    /**
     * Cleans the entities, uses a tokenizer to tokenize all entities with the
     * same algorithm.
     * 
     * @param list
     * @return
     */
    protected List<Entity> clean(List<Entity> list) {
        logger.info("clean entities ...");
        // TODO: cache use
        // clean token with the tokenizer
        for (Entity entity : list) {
            StringBuilder cleanText = new StringBuilder();
            String[] tokens = FoxTextUtil.getSentenceToken(entity.getText() + ".");
            for (String token : tokens) {
                if (!token.trim().isEmpty())
                    cleanText.append(token);
                if (tokens[tokens.length - 1] != token)
                    cleanText.append(" ");
            }
            entity.setText(cleanText.toString());
        }
        list = new ArrayList<Entity>(list);

        // TRACE
        if (logger.isTraceEnabled()) {
            if (list.size() > 0)
                logger.trace(list.size() + "(" + list.iterator().next().getTool() + ")");
            for (Entity entity : list)
                logger.trace(entity.getText() + "=>" + entity.getType() + "(" + entity.getTool() + ")");
        }
        // TRACE
        logger.info("clean entities done.");
        return list;
    }
}
