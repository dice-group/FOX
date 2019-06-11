package org.aksw.fox.tools.ner.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.encode.BILOUEncoding;
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

    final Annotation ann = new Annotation(text);
    pipeline.annotate(ann);

    final List<Entity> entities = new ArrayList<>();

    for (final CoreMap sentence : ann.get(SentencesAnnotation.class)) {
      String tokensentence = "";
      for (final CoreLabel token : sentence.get(TokensAnnotation.class)) {
        tokensentence += token.word() + " ";

        final String type = mapTypeToSupportedType(token.get(NamedEntityTagAnnotation.class));
        final String originalText = token.originalText();
        final int index = token.beginPosition();
        // check for multiword entities
        boolean contains = false;
        boolean equalTypes = false;
        Entity lastEntity = null;

        // checks if the current and previous entities have the
        // same type and occur with a space in the sentence
        if (!entities.isEmpty()) {
          lastEntity = entities.get(entities.size() - 1);
          contains = tokensentence.contains(lastEntity.getText() + " " + originalText + " ");
          equalTypes = type.equals(lastEntity.getType());
        }

        if (contains && equalTypes) {
          lastEntity.addText(originalText);
        } else {
          if (!type.equals(BILOUEncoding.O)) {
            entities.add(
                new Entity(originalText, type, Entity.DEFAULT_RELEVANCE, getToolName(), index));
          }
        }
      }
    }
    return entities;
  }
}
