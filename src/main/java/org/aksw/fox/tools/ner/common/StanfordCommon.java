package org.aksw.fox.tools.ner.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.AbstractNER;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public abstract class StanfordCommon extends AbstractNER {

  protected StanfordCoreNLP pipeline = null;

  public StanfordCommon(final Properties props) {
    pipeline = new StanfordCoreNLP(props);
  }

  @Override
  public List<Entity> retrieve(final String text) {
    LOG.info("retrieve ...");

    final Annotation ann = new Annotation(text);
    pipeline.annotate(ann);

    final List<Entity> list = new ArrayList<>();

    for (final CoreMap sentence : ann.get(SentencesAnnotation.class)) {
      String tokensentence = "";
      for (final CoreLabel token : sentence.get(TokensAnnotation.class)) {
        tokensentence += token.word() + " ";
        final String type = mapTypeToSupportedType(token.get(NamedEntityTagAnnotation.class));
        final String currentToken = token.originalText();
        // check for multiword entities
        boolean contains = false;
        boolean equalTypes = false;
        Entity lastEntity = null;
        if (!list.isEmpty()) {
          lastEntity = list.get(list.size() - 1);
          contains = tokensentence.contains(lastEntity.getText() + " " + currentToken + " ");
          equalTypes = type.equals(lastEntity.getType());
        }
        if (contains && equalTypes) {
          lastEntity.addText(currentToken);
        } else {
          if (type != EntityClassMap.getNullCategory()) {
            final float p = Entity.DEFAULT_RELEVANCE;
            list.add(getEntity(currentToken, type, p, getToolName()));
          }
        }
      }
    }
    // TRACE
    if (LOG.isTraceEnabled()) {
      LOG.trace(list);
    } // TRACE
    LOG.info("retrieve done.");
    return list;
  }
}
