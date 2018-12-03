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
import org.aksw.simba.knowledgeextraction.commons.config.CfgManager;
import org.aksw.simba.knowledgeextraction.commons.dbpedia.DBpedia;
import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipeExtended;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class OcelotEN extends AbstractRE {

  static {
    CfgManager.cfgFolder = "data/ocelot/config";
  }

  IOcelot ocelot = new Application(CfgManager.cfgFolder);

  public OcelotEN() {}

  protected URI toUri(final String uri) {
    try {
      return new URI(uri);
    } catch (final URISyntaxException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return null;
  }

  protected Set<URI> toUris(final Set<String> uris) {
    final Set<URI> urisSet = new HashSet<>();
    for (final String u : uris) {
      final URI uri = toUri(u);
      if (uri != null) {
        urisSet.add(uri);
      }
    }
    return urisSet;
  }

  @Override
  protected Set<Relation> _extract(final String text, final List<Entity> entities) {
    // keep the original entities with IDs
    final Map<Integer, Entity> idMap = setEntityIDs(entities);

    // copies all entities since we change the index of those
    final List<Entity> copiedEntities = new ArrayList<>();
    entities.forEach(entity -> copiedEntities.add(new Entity(entity)));

    final StanfordPipeExtended stanford = StanfordPipeExtended.instance();

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
      final List<Integer> sorted = new ArrayList<>(new TreeSet<>(index.keySet()));

      for (int i = 0; i + 1 < sorted.size(); i++) {

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

        if (null != uris && uris.size() > 0) {
          for (final String uri : uris) {
            final String p = uri.startsWith(DBpedia.ns_dbpedia_ontology) ? //
                uri : DBpedia.ns_dbpedia_ontology.concat(uri);

            if (checkDomainRange(//
                mapFoxTypesToDBpediaTypes(sType), //
                p, //
                mapFoxTypesToDBpediaTypes(oType)//
            )) {

              final List<URI> ad = new ArrayList<>();
              ad.add(toUri(uri));

              Relation relation = null;
              relation = new Relation(//
                  idMap.get(subject.id), //
                  p.replace(DBpedia.ns_dbpedia_ontology, ""), //
                  uri, //
                  idMap.get(object.id), //
                  ad, //
                  getToolName(), //
                  Relation.DEFAULT_RELEVANCE//
              );
              relations.add(relation);
            }
          }
        } // end if
      } // end for
    }
    return relations;
  }
}
