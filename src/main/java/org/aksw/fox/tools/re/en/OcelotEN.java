package org.aksw.fox.tools.re.en;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.aksw.fox.tools.re.AbstractRE;
import org.aksw.ocelot.application.Application;
import org.aksw.ocelot.application.IOcelot;
import org.aksw.ocelot.common.config.CfgManager;
import org.aksw.simba.knowledgeextraction.commons.dbpedia.DBpedia;
import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipe;
import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipeExtended;

public class OcelotEN extends AbstractRE {

  static String file = "data/ocelot/config";
  static {
    CfgManager.setFolder(file);
  }

  IOcelot ocelot = new Application(file);

  final StanfordPipe stanford = StanfordPipeExtended.getStanfordPipe();

  protected URI getUri(final String uri) {
    try {
      return (new URI(uri));
    } catch (final URISyntaxException e) {
      LOG.error("URISyntaxException for: " + uri);
    }
    return null;
  }

  protected Set<URI> getUris(final Set<String> uris) {
    final Set<URI> _uris = new HashSet<>();
    for (final String uri : uris) {
      try {
        _uris.add(new URI(uri));
      } catch (final URISyntaxException e) {
        LOG.error("URISyntaxException for: " + uri);
      }
    }
    return _uris;
  }

  @Override
  protected Set<Relation> _extract(final String text, final List<Entity> entities) {

    // keep the original entities with IDs
    final Map<Integer, Entity> idMap = setEntityIDs(entities);

    // copies all entities since we change the index of those
    final List<Entity> copiedEntities = new ArrayList<>();
    entities.forEach(entity -> copiedEntities.add(new Entity(entity)));

    // splits text to sentences
    final Map<Integer, String> sentences = stanford.getSentenceIndex(text);

    // find entities for sentence
    final Map<Integer, List<Entity>> sentenceToEntities =
        sentenceToEntities(sentences, copiedEntities);

    // each sentence
    for (final Entry<Integer, String> entry : sentences.entrySet()) {
      final int sentenceID = entry.getKey();
      final String sentence = entry.getValue();

      final Map<Integer, Entity> index = Entity.indexToEntity(sentenceToEntities.get(sentenceID));
      final List<Integer> sorted = new ArrayList<>(new TreeSet<Integer>(index.keySet()));

      for (int i = 0; (i + 1) < sorted.size(); i++) {

        final Entity subject = index.get(sorted.get(i));
        final Entity object = index.get(sorted.get(i + 1));

        final int sStart = Entity.getIndex(subject);
        final int oStart = Entity.getIndex(object);

        final String sType = subject.getType();
        final String oType = object.getType();

        final Set<String> uris = ocelot//
            .run(//
                sentence, sType, oType, //
                sStart, sStart + subject.getText().length(), //
                oStart, oStart + object.getText().length()//
        );

        if ((null != uris) && (uris.size() > 0)) {
          for (final String uri : uris) {
            final String p = uri.startsWith(DBpedia.ns_dbpedia_ontology) ? //
                uri : DBpedia.ns_dbpedia_ontology.concat(uri);

            if (checkDomainRange(//
                mapFoxTypesToDBpediaTypes(sType), //
                p, //
                mapFoxTypesToDBpediaTypes(oType)//
            )) {

              final String reLabel = p.replace(DBpedia.ns_dbpedia_ontology, "");

              final List<URI> ad = new ArrayList<URI>();
              ad.add(getUri(uri));

              Relation relation = null;
              relation = new Relation(//
                  idMap.get(subject.id), //
                  reLabel, //
                  uri, //
                  idMap.get(object.id), //
                  ad, //
                  getToolName(), //
                  Relation.DEFAULT_RELEVANCE//
              );

              LOG.info("found relation: " + relation);
              relations.add(relation);
            }
          }
        } // end if
      } // end for
    }
    return relations;
  }
}
