package org.aksw.fox.nertools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.utils.FoxTextUtil;
import org.apache.log4j.Logger;

public abstract class AbstractNER implements INER {

    public static Logger logger = Logger.getLogger(AbstractNER.class);
    protected List<Entity> entityList = null;
    protected CountDownLatch cdl = null;
    protected String input = null;

    /*
     * @Override public List<Entity> retrieve(String input) { return new
     * ArrayList<>(); }
     */
    abstract public List<Entity> retrieve(String input);

    @Override
    public String getToolName() {
        return getClass().getSimpleName();
    }

    @Override
    public void run() {
        if (input != null)
            entityList = clean(retrieve(input));
        else
            logger.error("Input not set!");

        if (cdl != null)
            cdl.countDown();
        else
            logger.warn("CountDownLatch not set!");

        logMsg();
    }

    @Override
    public List<Entity> getResults() {
        return entityList;
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

        // clean token with the tokenizer
        for (Entity entity : list) {
            StringBuilder cleanText = new StringBuilder();
            String[] tokens = FoxTextUtil.getSentenceToken(entity.getText() + ".");
            // String[] tokens = FoxTextUtil.getToken(entity.getText());
            for (String token : tokens) {
                if (!token.trim().isEmpty()) {
                    cleanText.append(token);
                    cleanText.append(" ");
                }
            }
            entity.setText(cleanText.toString().trim());
        }
        list = new ArrayList<Entity>(list);

        logger.info("clean entities done.");
        return list;
    }

    private void logMsg() {
        // DEBUG
        if (entityList.size() > 0)
            logger.debug(entityList.size() + "(" + entityList.iterator().next().getTool() + ")");
        for (Entity entity : entityList)
            logger.debug(entity.getText() + "=>" + entity.getType() + "(" + entity.getTool() + ")");

        // INFO
        int l = 0, o = 0, p = 0;
        List<String> list = new ArrayList<>();
        for (Entity e : entityList) {
            if (!list.contains(e.getText())) {
                if (e.getType().equals(EntityClassMap.L))
                    l++;
                if (e.getType().equals(EntityClassMap.O))
                    o++;
                if (e.getType().equals(EntityClassMap.P))
                    p++;
                list.add(e.getText());
            }
        }
        logger.info(this.getToolName() + ":");
        logger.info(l + " LOCs found");
        logger.info(o + " ORGs found");
        logger.info(p + " PERs found");
        logger.info(entityList.size() + " total found");
        l = 0;
        o = 0;
        p = 0;
        for (Entity e : entityList) {
            if (e.getType().equals(EntityClassMap.L))
                l += e.getText().split(" ").length;
            if (e.getType().equals(EntityClassMap.O))
                o += e.getText().split(" ").length;
            if (e.getType().equals(EntityClassMap.P))
                p += e.getText().split(" ").length;
        }
        logger.info(this.getToolName() + "(token):");
        logger.info(l + " LOCs found");
        logger.info(o + " ORGs found");
        logger.info(p + " PERs found");
        logger.info(l + o + p + " total found");
    }
}
