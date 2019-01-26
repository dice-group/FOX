package org.aksw.fox.nerlearner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.fox.data.Entity;
import org.apache.log4j.Logger;

public class TokenCategoryMatrix {

  public static Logger logger = Logger.getLogger(TokenCategoryMatrix.class);

  List<String> token = null;
  List<String> categories = null;
  boolean[][] values = null;

  public TokenCategoryMatrix(final Set<String> token, final Set<String> categories,
      final String nullCategory, final Set<Entity> foundEntities, final String splitregText) {

    this.categories = new ArrayList<>(new TreeSet<>(categories));
    this.token = new ArrayList<>(token);

    values = new boolean[token.size()][categories.size()];

    for (int i = 0; i < token.size(); i++) {
      values[i][this.categories.indexOf(nullCategory)] = true;
    }

    if (foundEntities != null) {
      for (final Entity e : foundEntities) {
        setFound(e, splitregText);
      }
    }
  }

  private void setFound(final Entity entity, final String splitregText) {

    final String[] split = entity.getText().split(splitregText);

    for (String s : split) {
      s = s.trim();
      if (!s.isEmpty()) {
        final int tokenIndex = token.indexOf(s);
        if (tokenIndex > -1) {
          for (int i = 0; i < categories.size(); i++) {
            values[tokenIndex][i] = false;
          }

          values[tokenIndex][categories.indexOf(entity.getType())] = true;
        } else {
          logger.error("token not found: " + s);
        }
      }
    }
  }

  public boolean getValue(final String token, final String category) {
    if (this.token.indexOf(token) != -1) {
      return values[this.token.indexOf(token)][categories.indexOf(category)];
    } else {
      logger.error("token not found: " + token);
      return false;
    }
  }

  @Override
  public String toString() {
    String r = "";
    for (int i = 0; i < token.size(); i++) {
      r += "[";
      for (int j = 0; j < categories.size(); j++) {
        r += values[i][j] ? "1 " : "0 ";
      }
      r += "] -> " + token.get(i) + "\n";
    }
    return r;
  }
}
