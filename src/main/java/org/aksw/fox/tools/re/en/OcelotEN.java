package org.aksw.fox.tools.re.en;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.aksw.fox.tools.re.AbstractRE;
import org.aksw.ocelot.application.Application;
import org.aksw.ocelot.application.IOcelot;
import org.aksw.simba.knowledgeextraction.commons.dbpedia.DBpedia;
import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipeExtended;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class OcelotEN extends AbstractRE {

  String cfg = "data/ocelot/config";
  IOcelot ocelot = Application.instance(cfg);

  public OcelotEN() {}

  protected URI toUri(final String uri) {
    try {
      return new URI(uri);
    } catch (final Exception e) {
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

    final Map<Integer, String> sentences = StanfordPipeExtended.instance().getSentenceIndex(text);

    for (final Integer n : new TreeSet<>(sentences.keySet())) {
      final String sentence = sentences.get(n);

      final Map<Integer, Integer> textIndexToSentenceIndex;
      textIndexToSentenceIndex = textIndexToSentenceIndex(n, sentences, entities);

      for (int i = 0; i + 1 < entities.size(); i++) {
        final Entity subject = entities.get(i);
        final Entity object = entities.get(i + 1);

        final Integer si = textIndexToSentenceIndex.get(subject.getIndex());
        final Integer oi = textIndexToSentenceIndex.get(object.getIndex());

        if (si == null || oi == null) {
          continue;
        }

        // runs ocelot
        Set<String> uris = null;
        {
          final String _sentence = sentence;
          final int _subjectBegin = si, _subjectEnd = si + subject.getText().length();
          final int _objectBegin = oi, _objectEnd = oi + subject.getText().length();
          final String _subjectType = subject.getType(), _objectType = object.getType();

          uris = ocelot//
              .run(//
                  _sentence, _subjectType, _objectType, //
                  _subjectBegin, _subjectEnd, //
                  _objectBegin, _objectEnd//
              );

          LOG.debug("found a relation: " + uris);
        }

        for (final String uri : uris) {

          // adds domain to uri if needed
          final String p = uri.startsWith(DBpedia.ns_dbpedia_ontology) ? //
              uri : DBpedia.ns_dbpedia_ontology.concat(uri);

          // checks entity domain and range to the relations
          final boolean checkDomainRange = checkDomainRange(//
              mapFoxTypesToDBpediaTypes(subject.getType()), //
              p, //
              mapFoxTypesToDBpediaTypes(object.getType()));

          if (checkDomainRange) {

            final List<URI> ad = new ArrayList<>();
            ad.add(toUri(p));

            Relation relation = null;
            relation = new Relation(//
                subject, //
                p.replace(DBpedia.ns_dbpedia_ontology, ""), //
                p, //
                object, //
                ad, //
                getToolName(), //
                Relation.DEFAULT_RELEVANCE//
            );
            LOG.debug(relation);
            relations.add(relation);
          }
        }
      }
    }
    return relations;
  }
}
