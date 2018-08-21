package org.aksw.fox.tools.re.en;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.aksw.fox.tools.re.AbstractRE;
import org.aksw.ocelot.application.Application;
import org.aksw.ocelot.application.IOcelot;
import org.aksw.ocelot.common.config.CfgManager;
import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipe;
import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipeExtended;

public class OcelotEN extends AbstractRE {

  static String file = "data/ocelot/config";
  static {
    CfgManager.setFolder(file);
  }
  IOcelot ocelot = new Application(file);
  final StanfordPipe stanford = StanfordPipeExtended.getStanfordPipe();

  private Set<URI> getUris(final Set<String> uris) {
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
    LOG.info(sentenceToEntities);

    // each sentence
    int offset = 0;
    for (final Entry<Integer, String> entry : sentences.entrySet()) {
      final int id = entry.getKey();
      final String sentence = entry.getValue();
      LOG.info(sentence);

      final List<Entity> sentenceEntities = sentenceToEntities.get(id);
      for (int i = 0; (i + 1) < sentenceEntities.size(); i++) {
        final Entity subject = sentenceEntities.get(i);
        final Entity object = sentenceEntities.get(i + 1);

        final String sType = subject.getType();
        final String oType = object.getType();

        final int si = subject.getIndices().iterator().next() - offset;
        final int oi = object.getIndices().iterator().next() - offset;

        final Set<String> uris = ocelot//
            .run(//
                sentence, sType, oType, //
                si, si + subject.getText().length(), //
                oi, oi + object.getText().length()//
        );

        if ((null != uris) && (uris.size() > 0)) {

          final String uri = "";
          final String reLabel = "";

          Relation relation = null;
          relation = new Relation(//
              idMap.get(subject.id), reLabel, //
              uri, //
              idMap.get(object.id), //
              new ArrayList<>(getUris(uris)), //
              getToolName(), //
              Relation.DEFAULT_RELEVANCE//
          );
          relations.add(relation);
        }
      } // end for
      offset += sentence.length();
    }
    return relations;
  }
}
