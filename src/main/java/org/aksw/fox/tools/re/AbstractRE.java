package org.aksw.fox.tools.re;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.aksw.fox.tools.ATool;

public abstract class AbstractRE extends ATool implements IRE {

  protected Set<Relation> relations = new HashSet<>();
  protected String input = null;
  protected Set<Entity> entities = null;

  @Override
  public void run() {
    extract();
    if (cdl != null) {
      cdl.countDown();
    }
  }

  @Override
  public Set<Relation> extract() {
    relations.clear();

    if ((entities != null) && !entities.isEmpty()) {
      return _extract(input, Entity.breakdownAndSortEntity(entities));
    } else {
      LOG.warn("Entities not given!");
    }

    return relations;
  }

  protected abstract Set<Relation> _extract(final String text, final List<Entity> entities);

  @Override
  public void setInput(final String input, final Set<Entity> entities) {
    this.input = input;
    this.entities = entities;
  }

  @Override
  public Set<Relation> getResults() {
    return relations;
  }

  /**
   * Allocates the entities to sentences. Updates the entities index! The updated index should be
   * used just internally.
   *
   * @param sentences an index to sentence
   * @param the entities in all sentences
   * @return LinkedHashMap with the input index to a list of entities in a sentence.
   */
  protected Map<Integer, List<Entity>> sentenceToEntities(//
      final Map<Integer, String> sentences, final List<Entity> entities) {

    int offset = 0;
    final Map<Integer, List<Entity>> sentenceToEntities = new LinkedHashMap<>();

    // each sentence
    for (final Entry<Integer, String> entry : sentences.entrySet()) {
      final int id = entry.getKey();
      final String sentence = entry.getValue();

      sentenceToEntities.put(id, new ArrayList<>());

      // all entities
      final Iterator<Entity> iter = entities.iterator();
      while (iter.hasNext()) {
        final Entity e = iter.next();
        if (e.getIndices().size() > 1) {
          throw new UnsupportedOperationException(
              "Entity with multipe index. Split the entities first.");
        }
        final int beginnIndex = Entity.getIndex(e);
        final int endIndex = (beginnIndex + e.getText().length());

        if (beginnIndex >= offset) {
          // the start index of the entity is inside the current sentence
          if (endIndex < (sentence.length() + offset)) {
            // the end index of the entity is inside the current sentence
            e.getIndices().clear();
            e.getIndices().add(beginnIndex - offset);
            sentenceToEntities.get(id).add(e);
          }
        }
      } // end while

      offset += sentence.length();
    }
    return sentenceToEntities;
  }

  /**
   * Adds an id to each entity.
   *
   * @param entities
   * @return map with id to entity
   */
  protected Map<Integer, Entity> setEntityIDs(final List<Entity> entities) {
    final Map<Integer, Entity> idMap = new HashMap<>();
    for (int i = 0; i < entities.size(); i++) {
      final Entity entity = entities.get(i);
      entity.id = i;
      idMap.put(entity.id, entity);
    }
    return idMap;
  }
}
