package org.aksw.fox.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 
 * @author rspeck
 * 
 */
public class TokenCategoryMatrix {

    public static Logger logger = Logger.getLogger(TokenCategoryMatrix.class);

    List<String> token = null;
    List<String> categories = null;
    boolean[][] values = null;

    public TokenCategoryMatrix(
            Set<String> token, Set<String> categories,
            String nullCategory, Set<Entity> foundEntities, String splitregText) {

        // LOGGER
        if (foundEntities != null && foundEntities.iterator().hasNext())
            logger.info("TokenCategoryMatrix : " + foundEntities.iterator().next().getTool());
        else
            logger.warn("No entities found.");
        // LOGGER

        this.categories = new ArrayList<>();
        this.categories.addAll(categories);
        this.token = new ArrayList<>(token);
        this.values = new boolean[token.size()][categories.size()];

        for (int i = 0; i < token.size(); i++)
            values[i][this.categories.indexOf(nullCategory)] = true;

        if (foundEntities != null)
            for (Entity e : foundEntities)
                setFound(e, splitregText);

        // TRACE
        if (logger.isTraceEnabled())
            logger.trace(this);
        // TRACE
    }

    public void setFound(Entity entity, String splitregText) {
        // DEBUG
        if (logger.isDebugEnabled())
            logger.debug("setFound: " + entity);
        // DEBUG

        String[] split = entity.text.split(splitregText);

        // TRACE
        if (logger.isTraceEnabled()) {
            logger.trace("split" + Arrays.asList(split));
        }// TRACE

        for (String s : split) {
            s = s.trim();
            if (!s.isEmpty()) {
                int tokenIndex = token.indexOf(s);
                if (tokenIndex > -1) {
                    for (int i = 0; i < categories.size(); i++)
                        values[tokenIndex][i] = false;

                    values[tokenIndex][categories.indexOf(entity.type)] = true;
                } else {
                    logger.error("token not found: " + s);
                }
            }
        }
    }

    public boolean getValue(String token, String category) {
        if (this.token.indexOf(token) != -1)
            return values[this.token.indexOf(token)][categories.indexOf(category)];
        else {
            logger.error("token not found: " + token);
            return false;
        }
    }

    @Override
    public String toString() {
        String r = "";
        for (int i = 0; i < token.size(); i++) {
            r += "[";
            for (int j = 0; j < categories.size(); j++)
                r += values[i][j] ? "1 " : "0 ";
            r += "] -> " + token.get(i) + "\n";
        }
        return r;
    }
}
