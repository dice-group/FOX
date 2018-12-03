package org.aksw.fox.tools.re;

import java.util.AbstractMap.SimpleEntry;
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
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.Relation;
import org.aksw.fox.tools.ATool;
import org.aksw.simba.knowledgeextraction.commons.dbpedia.DBpedia;
import org.aksw.simba.knowledgeextraction.commons.dbpedia.DBpediaOntology;
import org.aksw.simba.knowledgeextraction.commons.dbpedia.IDBpediaOntology;
import org.aksw.simba.knowledgeextraction.commons.time.SimpleStopwatch;

public abstract class AbstractRE extends ATool implements IRE {

  protected final IDBpediaOntology dbpediaOntology = new DBpediaOntology();
  protected final SimpleStopwatch watch = new SimpleStopwatch();

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
      watch.start();
      relations = _extract(input, Entity.breakdownAndSortEntity(entities));
      watch.stop();
    }

    LOG.info(getToolName() + " found " + relations.size() + " relations in " + watch.getTimeInSec()
        + "s: ");
    relations.forEach(LOG::info);

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

  /**
   * Checks domain and range of the given predicate.
   *
   * @param s domain e.g. http://dbpedia.org/ontology/Person
   * @param p predicate http://dbpedia.org/ontology/spouse
   * @param o range http://dbpedia.org/ontology/Person
   * @return true, in case the given s and o are the domain and range of p
   */
  protected boolean checkDomainRange(final String s, final String p, final String o) {
    final SimpleEntry<Set<String>, Set<String>> domainRange = dbpediaOntology.getDomainRange(p);
    final boolean rightDomain = domainRange.getKey().contains(s) || domainRange.getKey().isEmpty();
    final boolean rightRange =
        domainRange.getValue().contains(o) || domainRange.getValue().isEmpty();
    return rightDomain && rightRange;
  }

  protected String mapFoxTypesToDBpediaTypes(final String foxType) {
    switch (foxType) {
      case EntityClassMap.P: {
        return DBpedia.ns_dbpedia_ontology.concat("Person");
      }
      case EntityClassMap.L: {
        return DBpedia.ns_dbpedia_ontology.concat("Place");
      }
      case EntityClassMap.O: {
        return DBpedia.ns_dbpedia_ontology.concat("Organisation");
      }
      default:
        return null;
    }
  }
}
