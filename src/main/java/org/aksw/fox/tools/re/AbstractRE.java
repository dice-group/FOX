package org.aksw.fox.tools.re;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.Relation;
import org.aksw.fox.tools.ATool;
import org.aksw.simba.knowledgeextraction.commons.dbpedia.DBpedia;
import org.aksw.simba.knowledgeextraction.commons.dbpedia.DBpediaOntology;
import org.aksw.simba.knowledgeextraction.commons.dbpedia.IDBpediaOntology;

public abstract class AbstractRE extends ATool implements IRE {

  protected final IDBpediaOntology dbpediaOntology = new DBpediaOntology();

  protected Set<Relation> relations = new HashSet<>();
  protected String input = null;
  protected List<Entity> entities = null;

  @Override
  public void run() {
    extract();
    if (cdl != null) {
      cdl.countDown();
    }
  }

  protected abstract Set<Relation> _extract(final String text, final List<Entity> entities);

  /**
   * Sorts {{@link #entities} and Calls {{@link #_extract(String, List)}.
   *
   * @return relations
   */
  @Override
  public Set<Relation> extract() {

    relations.clear();

    if (entities != null && !entities.isEmpty()) {

      Collections.sort(entities);
      relations = _extract(input, entities);
    }

    return relations;
  }

  @Override
  public void setInput(final String input, final List<Entity> entities) {
    this.input = input;
    this.entities = entities;
  }

  @Override
  public Set<Relation> getResults() {
    return relations;
  }

  protected Map<Integer, Integer> textIndexToSentenceIndex(//
      final int n, final Map<Integer, String> sentences, final List<Entity> entities) {

    final Map<Integer, Map<Integer, Integer>> textIndexToSentenceIndex = new HashMap<>();
    int offset = 0;
    // each sentence
    for (final Integer sentencesId : new TreeSet<>(sentences.keySet())) {

      textIndexToSentenceIndex.put(sentencesId, new HashMap<>());

      // all entities
      final int sentenceLength = sentences.get(sentencesId).length();

      for (final Entity entity : entities) {

        final int beginIndex = entity.getBeginIndex();
        final int endIndex = entity.getBeginIndex() + entity.getText().length();

        final boolean a = beginIndex >= offset;
        final boolean b = endIndex < offset + sentenceLength;
        if (a && b) {
          textIndexToSentenceIndex.get(sentencesId).put(beginIndex, beginIndex - offset);
        }
      } // end for entities
      offset += sentenceLength;
    } // end for sentences
    return textIndexToSentenceIndex.get(n);
  }

  /**
   * Allocates the entities to sentences. Updates the entities index according to the sentence
   * lengths.
   *
   * @param sentences an index to sentence
   * @param the entities in all sentences
   * @return LinkedHashMap with the input index to a list of entities in a sentence. <code>
  &#64;Deprecated
  protected Map<Integer, List<Entity>> sentenceToEntities(final Map<Integer, String> sentences,
      final List<Entity> entities) {
  
    final Map<Integer, List<Entity>> sentenceToEntities = new LinkedHashMap<>();
  
    // each sentence
    for (final Entry<Integer, String> entry : sentences.entrySet()) {
      final int sentencesId = entry.getKey();
      final int sentenceLength = entry.getValue().length();
  
      sentenceToEntities.put(sentencesId, new ArrayList<>());
  
      // all entities
      int offset = 0;
      for (final Entity entity : entities) {
  
        final int beginnIndex = entity.getIndex();
        final int endIndex = entity.getIndex() + entity.getText().length();
  
        if (beginnIndex >= offset && endIndex < sentenceLength + offset) {
          entity.setIndex(beginnIndex - offset);
          sentenceToEntities.get(sentencesId).add(entity);
        }
      } // end for entities
      offset += sentenceLength;
    } // end for sentences
    return sentenceToEntities;
  }
  
  /**
   * Adds an id to each entity.
   *
   * &#64;param entities
   * &#64;return map with id to entity

  &#64;Deprecated
  protected Map<Integer, Entity> setEntityIDs(final List<Entity> entities) {
    final Map<Integer, Entity> idMap = new HashMap<>();
    for (final Entity entity : entities) {
      idMap.put(entity.getIndex(), entity);
    }
    return idMap;
  }  </code>
   */

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
      case EntityTypes.P: {
        return DBpedia.ns_dbpedia_ontology.concat("Person");
      }
      case EntityTypes.L: {
        return DBpedia.ns_dbpedia_ontology.concat("Place");
      }
      case EntityTypes.O: {
        return DBpedia.ns_dbpedia_ontology.concat("Organisation");
      }
      default:
        return null;
    }
  }
}
