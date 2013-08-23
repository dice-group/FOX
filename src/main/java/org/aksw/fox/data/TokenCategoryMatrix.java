package org.aksw.fox.data;

import java.util.ArrayList;
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
    String nullCategory = null;
    boolean[][] values = null;
    String splitregText = null;

    public TokenCategoryMatrix(Set<String> token, Set<String> categories, String nullCategory, Set<Entity> foundEntities, String splitregText) {
        logger.info("TokenCategoryMatrix ...");

        this.categories = new ArrayList<>();
        this.categories.addAll(categories);
        this.nullCategory = nullCategory;
        this.token = new ArrayList<>(token);
        this.splitregText = splitregText;
        this.values = new boolean[token.size()][categories.size()];

        for (int i = 0; i < token.size(); i++)
            values[i][this.categories.indexOf(nullCategory)] = true;

        if (foundEntities != null)
            for (Entity e : foundEntities)
                setFound(e);
    }

    public void setFound(Entity entity) {
        // if (logger.isDebugEnabled())
        // logger.debug("setFound: " + entity);

        String[] split = entity.text.split(splitregText);
        for (String s : split) {
            if (!s.trim().isEmpty()) {
                int tokenIndex = token.indexOf(s.trim());
                if (tokenIndex >= 0) {
                    for (int i = 0; i < categories.size(); i++)
                        values[tokenIndex][i] = false;
                    values[tokenIndex][categories.indexOf(entity.type)] = true;
                } else {
                    logger.error("token not found: " + s.trim());
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

    // public boolean[] getValues(String token) {
    // return values[token.indexOf(token)];
    // }

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
