package org.aksw.fox.tools.ner;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.utils.FoxTextUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class AbstractNER implements INER {

  public static final Logger LOG = LogManager.getLogger(AbstractNER.class);

  protected CountDownLatch cdl = null;
  protected String input = null;

  protected List<Entity> entityList = null;
  protected Map<String, String> entityClasses = new HashMap<>();

  @Override
  abstract public List<Entity> retrieve(String input);

  public List<Entity> _retrieve(final String input) {
    final List<Entity> list = new ArrayList<>();
    final Set<Entity> set = new HashSet<>(retrieve(input));
    list.addAll(set);
    entityList = clean(list);
    return entityList;
  }

  @Override
  public String getToolName() {
    return getClass().getSimpleName();
  }

  @Override
  public void run() {
    if (input != null) {
      _retrieve(input);
    } else {
      LOG.error("Input not set!");
    }

    if (cdl != null) {
      cdl.countDown();
    } else {
      LOG.warn("CountDownLatch not set!");
    }

    logMsg();
  }

  @Override
  public List<Entity> getResults() {
    return entityList;
  }

  @Override
  public void setCountDownLatch(final CountDownLatch cdl) {
    this.cdl = cdl;
  }

  @Override
  public void setInput(final String input) {
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
  protected Entity getEntity(final String text, final String type, final float relevance,
      final String tool) {
    return new Entity(text, type, relevance, tool);
  }

  /**
   * Cleans the entities, uses a tokenizer to tokenize all entities with the same algorithm.
   *
   * @param list
   * @return
   */
  protected List<Entity> clean(List<Entity> list) {
    LOG.info("clean entities ...");

    // clean token with the tokenizer
    for (final Entity entity : list) {
      final StringBuilder cleanText = new StringBuilder();
      final String[] tokens = FoxTextUtil.getSentenceToken(entity.getText() + ".");
      // String[] tokens = FoxTextUtil.getToken(entity.getText());
      for (final String token : tokens) {
        if (!token.trim().isEmpty()) {
          cleanText.append(token);
          cleanText.append(" ");
        }
      }
      entity.setText(cleanText.toString().trim());
    }
    list = new ArrayList<Entity>(list);

    LOG.info("clean entities done.");
    return list;
  }

  public List<String> getSentences(final Locale lang, final String input) {
    final List<String> sentences = new ArrayList<>();
    final BreakIterator sentenceIterator = BreakIterator.getSentenceInstance(lang);
    sentenceIterator.setText(input);
    int start = sentenceIterator.first();
    int end = sentenceIterator.next();
    while (end != BreakIterator.DONE) {
      final String sentence = input.substring(start, end);
      if ((sentence != null) && !sentence.isEmpty()) {
        sentences.add(sentence);
      }
      start = end;
      end = sentenceIterator.next();
    }
    return sentences;
  }

  /**
   * Gets the supported entity type for tool entity type.
   */
  public String mapTypeToSupportedType(final String toolType) {
    if (entityClasses.isEmpty()) {
      LOG.warn("No entity types know. Please fill the entity classes map with types to map.");
    }
    String t = entityClasses.get(toolType);
    if (t == null) {
      t = EntityClassMap.N;
    }
    return t;
  }

  private void logMsg() {
    // DEBUG
    if (entityList.size() > 0) {
      LOG.debug(entityList.size() + "(" + entityList.iterator().next().getTool() + ")");
    }
    for (final Entity entity : entityList) {
      LOG.debug(entity.getText() + "=>" + entity.getType() + "(" + entity.getTool() + ")");
    }

    // INFO
    int l = 0, o = 0, p = 0;
    final List<String> list = new ArrayList<>();
    for (final Entity e : entityList) {
      if (!list.contains(e.getText())) {
        if (e.getType().equals(EntityClassMap.L)) {
          l++;
        }
        if (e.getType().equals(EntityClassMap.O)) {
          o++;
        }
        if (e.getType().equals(EntityClassMap.P)) {
          p++;
        }
        list.add(e.getText());
      }
    }
    LOG.info(getToolName() + ":");
    LOG.info(l + " LOCs found");
    LOG.info(o + " ORGs found");
    LOG.info(p + " PERs found");
    LOG.info(entityList.size() + " total found");
    l = 0;
    o = 0;
    p = 0;
    for (final Entity e : entityList) {
      if (e.getType().equals(EntityClassMap.L)) {
        l += e.getText().split(" ").length;
      }
      if (e.getType().equals(EntityClassMap.O)) {
        o += e.getText().split(" ").length;
      }
      if (e.getType().equals(EntityClassMap.P)) {
        p += e.getText().split(" ").length;
      }
    }
    LOG.info(getToolName() + "(token):");
    LOG.info(l + " LOCs found");
    LOG.info(o + " ORGs found");
    LOG.info(p + " PERs found");
    LOG.info(l + o + p + " total found");
  }
}
