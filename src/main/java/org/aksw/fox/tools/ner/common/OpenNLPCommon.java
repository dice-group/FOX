package org.aksw.fox.tools.ner.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.encode.BILOUEncoding;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.FoxTextUtil;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

public abstract class OpenNLPCommon extends AbstractNER {

  protected String[] modelPath;
  protected TokenNameFinderModel[] models;

  public OpenNLPCommon(final String[] modelPath) {

    entityClasses.put("location", EntityTypes.L);
    entityClasses.put("organization", EntityTypes.O);
    entityClasses.put("person", EntityTypes.P);

    this.modelPath = modelPath;

    models = new TokenNameFinderModel[modelPath.length];
    final InputStream[] modelIn = new InputStream[modelPath.length];

    for (int i = 0; i < models.length; i++) {
      try {
        modelIn[i] = new FileInputStream(modelPath[i]);
        models[i] = new TokenNameFinderModel(modelIn[i]);

      } catch (final IOException e) {
        LOG.error(e.getLocalizedMessage(), e);
      } finally {
        try {
          if (modelIn[i] != null) {
            modelIn[i].close();
          }
        } catch (final IOException e) {
          LOG.error(e.getLocalizedMessage(), e);
        }
      }
    }
  }

  // TODO: do parallel for each model, add index.
  @Override
  public List<Entity> retrieve(final String input) {

    final List<Entity> entityList = new ArrayList<>();

    final String[] sentences = FoxTextUtil.getSentences(input);

    for (int i = 0; i < models.length; i++) {
      final NameFinderME nameFinder = new NameFinderME(models[i]);

      int offset = 0;
      for (final String sentence : sentences) {
        final String[] tokens = FoxTextUtil.getSentenceToken(sentence);

        if (tokens.length > 0 && tokens[tokens.length - 1].trim().isEmpty()) {
          tokens[tokens.length - 1] = ".";
        }

        final Span[] nameSpans = nameFinder.find(tokens);
        nameFinder.probs(nameSpans);
        for (int ii = 0; ii < nameSpans.length; ii++) {
          final Span span = nameSpans[ii];

          String word = "";
          for (int j = 0; j < span.getEnd() - span.getStart(); j++) {
            word += tokens[span.getStart() + j] + " ";
          }
          word = word.trim();

          final int index = input.substring(offset, input.length()).indexOf(word) + offset;
          final String cl = mapTypeToSupportedType(span.getType());
          if (!cl.equals(BILOUEncoding.O)) {
            entityList.add(new Entity(word, cl, Entity.DEFAULT_RELEVANCE, getToolName(), index));
          }
        } // end nameSpans
        offset += sentence.length() + 1;
      } // end sentences

      nameFinder.clearAdaptiveData();
    }
    return entityList;
  }
}
