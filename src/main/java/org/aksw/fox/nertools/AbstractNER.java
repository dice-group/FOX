package org.aksw.fox.nertools;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Entity;
import org.aksw.fox.utils.FoxTextUtil;
import org.apache.log4j.Logger;

public class AbstractNER implements InterfaceRunnableNER {

    public static Logger logger = Logger.getLogger(AbstractNER.class);
    protected Set<Entity> entitySet = null;
    protected CountDownLatch cdl = null;
    protected String input = null;

    @Override
    public Set<Entity> retrieve(String input) {
        return null;
    }

    @Override
    public String getToolName() {
        return getClass().getSimpleName();
    }

    @Override
    public void run() {
        if (input != null)
            entitySet = retrieve(input);
        else
            logger.error("Input not set!");

        if (cdl != null)
            cdl.countDown();
        else
            logger.warn("CountDownLatch not set!");
    }

    @Override
    public Set<Entity> getResults() {
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

    protected Entity getEntiy(String text, String type, float relevance, String tool) {
        return new Entity(text, type, relevance, tool);
    }

    protected Set<Entity> post(Set<Entity> set) {

        // clean token
        for (Entity entity : set) {

            String cleanText = "";
            for (String token : FoxTextUtil.getSentenceToken(entity.getText() + "."))
                if (!token.trim().isEmpty())
                    cleanText += token + " ";

            entity.setText(cleanText.trim());
        }
        set = new HashSet<Entity>(set);

        if (set.size() > 0)
            logger.info(set.size() + "(" + set.iterator().next().getTool() + ")");

        // DEBUG
        if (logger.isDebugEnabled()) {
            for (Entity entity : set)
                logger.debug(entity.getText() + "=>" + entity.getType() + "(" + entity.getTool() + ")");
        }
        // DEBUG

        logger.info("done.");
        return set;
    }
}
