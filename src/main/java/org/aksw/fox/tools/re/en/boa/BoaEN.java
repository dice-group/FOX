package org.aksw.fox.tools.re.en.boa;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.Relation;
import org.aksw.fox.tools.re.AbstractRE;

public class BoaEN extends AbstractRE {

  final String file = "/media/rspeck/store/Data/boa_backup/solr/data/boa/en/index/";
  final ReadBoaIndex boaindex = new ReadBoaIndex(file);

  // domain to range and relation
  Map<String, Map<String, Set<String>>> supportedRelations = new HashMap<>();

  public static void main(final String[] a) {
    final BoaEN boa = new BoaEN();
    LOG.info(boa.getToolName());

    //
    final String text =
        "The philosopher and mathematician Leibniz was born in Leipzig in 1646 and attended the University of Leipzig from 1661-1666. "
            + "The current chancellor of Germany, Angela Merkel, also attended this university. ";

    final Entity s = new Entity("Leibniz", EntityClassMap.P);
    s.addIndicies(34);

    final Entity o = new Entity("Leipzig", EntityClassMap.L);
    o.addIndicies(54);

    final Set<Entity> entities = new HashSet<>();
    entities.add(s);
    entities.add(o);

    boa.setInput(text, entities);

    final Set<Relation> relations = boa.extract();
    LOG.info(relations);
  }

  /**
   * Initializes {@link #supportedRelations}.
   *
   */
  public BoaEN() {
    relations = new HashSet<Relation>();

    createSupportedBoaRelations();
  }

  /***
   * Just a test.
   */
  @Override
  public Set<Relation> extract() {
    relations.clear();

    if ((entities != null) && !entities.isEmpty()) {
      return _extract(input, breakdownAndSortEntity(entities));
    } else {
      LOG.warn("Entities not given!");
    }

    return relations;
  }

  /**
   * Each entity with just one index and sorted.
   *
   * @param entities
   * @return sorted entities with one index in the index set
   */
  private List<Entity> breakdownAndSortEntity(final Set<Entity> entities) {

    final Map<Integer, Entity> sorted = new HashMap<>();

    for (final Entity entity : entities) {
      if (entity.getIndices().size() > 1) {
        final Iterator<Integer> iter = entity.getIndices().iterator();
        while (iter.hasNext()) {
          final Entity e = new Entity(entity.getText(), entity.getType(), entity.getRelevance(),
              entity.getTool());

          final int index = iter.next();
          e.addIndicies(index);
          sorted.put(index, e);
        }
      } else {
        sorted.put(entity.getIndices().iterator().next(), entity);
      }
    }
    final List<Entity> breakdownEntity = new ArrayList<>();
    for (final Integer i : sorted.keySet().stream().sorted().collect(Collectors.toList())) {
      breakdownEntity.add(sorted.get(i));
    }
    return breakdownEntity;
  }

  /**
   *
   * @param text
   * @param entities
   * @return
   */
  private Set<Relation> _extract(final String text, final List<Entity> entities) {

    for (int i = 0; (i + 1) < entities.size(); i++) {
      final Entity subject = entities.get(i);
      final Entity object = entities.get(i + 1);

      final String sType = subject.getType();
      final String oType = object.getType();

      final Set<String> uris = getURIs(sType, oType);

      final int sIndex = subject.getIndices().iterator().next();
      final int oIndex = object.getIndices().iterator().next();

      final String substring = text.substring(sIndex + subject.getText().length(), oIndex).trim();

      for (final String uri : uris) {
        final Map<String, Pattern> pattern = getPattern(uri);

        if (pattern.keySet().contains(substring)) {
          Relation relation;
          try {
            relation = new Relation(//
                subject, //
                substring, //
                uri, //
                object, //
                Arrays.asList(new URI(uri)), //
                getToolName(), //
                Relation.DEFAULT_RELEVANCE//
            );
            relations.add(relation);
          } catch (final URISyntaxException e) {
            LOG.error(e.getLocalizedMessage(), e);
          }
        }
      }
    }
    return relations;
  }

  /**
   * Gets all possible relations for the type pair combination.
   *
   * @param sType subject type
   * @param oType object type
   * @return matching uris
   */
  protected Set<String> getURIs(final String sType, final String oType) {
    return supportedRelations.get(sType).get(oType);
  }

  /**
   * Gets boa pattern from index.
   *
   * @param uri
   * @return pattern
   */
  public Map<String, Pattern> getPattern(final String uri) {
    try {
      final Map<String, Pattern> pattern = boaindex.processSearch(uri);
      pattern.keySet().forEach(LOG::info);
      return pattern;
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return new HashMap<>();
  }

  private void createSupportedBoaRelations() {
    supportedRelations.put(EntityClassMap.L, new HashMap<>());
    supportedRelations.put(EntityClassMap.P, new HashMap<>());
    supportedRelations.put(EntityClassMap.O, new HashMap<>());

    supportedRelations.get(EntityClassMap.L).put(EntityClassMap.L, new HashSet<String>());
    supportedRelations.get(EntityClassMap.L).put(EntityClassMap.P, new HashSet<String>());
    supportedRelations.get(EntityClassMap.L).put(EntityClassMap.O, new HashSet<String>());

    supportedRelations.get(EntityClassMap.P).put(EntityClassMap.L, new HashSet<String>());
    supportedRelations.get(EntityClassMap.P).put(EntityClassMap.P, new HashSet<String>());
    supportedRelations.get(EntityClassMap.P).put(EntityClassMap.O, new HashSet<String>());

    supportedRelations.get(EntityClassMap.O).put(EntityClassMap.L, new HashSet<String>());
    supportedRelations.get(EntityClassMap.O).put(EntityClassMap.P, new HashSet<String>());
    supportedRelations.get(EntityClassMap.O).put(EntityClassMap.O, new HashSet<String>());

    supportedRelations.get(EntityClassMap.L).get(EntityClassMap.P)
        .add("http://dbpedia.org/ontology/leaderName");
    supportedRelations.get(EntityClassMap.L).get(EntityClassMap.O)
        .add("http://dbpedia.org/ontology/team");

    supportedRelations.get(EntityClassMap.P).get(EntityClassMap.L)
        .add("http://dbpedia.org/ontology/deathPlace");
    supportedRelations.get(EntityClassMap.P).get(EntityClassMap.L)
        .add("http://dbpedia.org/ontology/birthPlace");
    supportedRelations.get(EntityClassMap.P).get(EntityClassMap.P)
        .add("http://dbpedia.org/ontology/spouse");
    supportedRelations.get(EntityClassMap.P).get(EntityClassMap.O)
        .add("http://dbpedia.org/ontology/team");

    supportedRelations.get(EntityClassMap.O).get(EntityClassMap.L)
        .add("http://dbpedia.org/ontology/foundationPlace");
    supportedRelations.get(EntityClassMap.O).get(EntityClassMap.O)
        .add("http://dbpedia.org/ontology/team");
    supportedRelations.get(EntityClassMap.O).get(EntityClassMap.O)
        .add("http://dbpedia.org/ontology/subsidiary");
  }
}
