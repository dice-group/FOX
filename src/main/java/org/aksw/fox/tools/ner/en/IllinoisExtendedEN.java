package org.aksw.fox.tools.ner.en;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.encode.BILOUEncoding;
import org.aksw.fox.tools.ner.AbstractNER;

import edu.illinois.cs.cogcomp.LbjNer.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.LbjNer.InferenceMethods.Decoder;
import edu.illinois.cs.cogcomp.LbjNer.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.LbjNer.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.Data;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NERDocument;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.Parameters;
import edu.illinois.cs.cogcomp.LbjNer.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.LbjNer.ParsingProcessingData.PlainTextReader;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;

public class IllinoisExtendedEN extends AbstractNER {

  public static String file = "config/conll.config";
  boolean areWeTraining = false;

  NETaggerLevel1 tagger1;
  NETaggerLevel2 tagger2;

  public IllinoisExtendedEN() {
    try {
      Parameters.readConfigAndLoadExternalData(file, areWeTraining);
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    tagger1 = new NETaggerLevel1(ParametersForLbjCode.currentParameters.pathToModelFile + ".level1",
        ParametersForLbjCode.currentParameters.pathToModelFile + ".level1.lex");
    tagger2 = new NETaggerLevel2(ParametersForLbjCode.currentParameters.pathToModelFile + ".level2",
        ParametersForLbjCode.currentParameters.pathToModelFile + ".level2.lex");
  }

  @Override
  public List<Entity> retrieve(final String input) {

    // parse input
    final Vector<LinkedVector> sentences = PlainTextReader.parseText(input);
    final NERDocument doc = new NERDocument(sentences, "input");
    final Data data = new Data(doc);

    // set input
    try {
      ExpressiveFeaturesAnnotator.annotate(data);
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    // annotate input
    List<Entity> list = null;
    try {
      Decoder.annotateDataBIO(data, tagger1, tagger2);
      list = getEntities(sentences, input);
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    return list;
  }

  public List<Entity> getEntities(final Vector<LinkedVector> sentences, final String input)
      throws Exception {
    final List<Entity> list = new ArrayList<>();
    int offset = 0;

    for (int i = 0; i < sentences.size(); i++) {
      final LinkedVector vector = sentences.elementAt(i);

      int sentenceSize = 0;

      boolean open = false;
      final String[] predictions = new String[vector.size()];
      final String[] words = new String[vector.size()];
      for (int j = 0; j < vector.size(); j++) {
        predictions[j] = ((NEWord) vector.get(j)).neTypeLevel2;
        words[j] = ((NEWord) vector.get(j)).form;
        sentenceSize += words[j].length();
      }

      String word = "";
      String tag = "";
      float prob = 0f;
      int probcount = 0;

      int sumlength = 0;
      // each token
      for (int j = 0; j < vector.size(); j++) {
        final NEWord w = (NEWord) vector.get(j);

        if (predictions[j].startsWith("B-") || j > 0 && predictions[j].startsWith("I-")
            && !predictions[j - 1].endsWith(predictions[j].substring(2))) {

          tag = predictions[j].substring(2);
          prob = 0f;
          word = new String();
          open = true;
        }

        if (open) {
          boolean close = false;
          word += words[j] + " ";
          prob += shapePred(w, tag);
          probcount++;
          if (j == vector.size() - 1) {
            close = true;
          } else {
            if (predictions[j + 1].startsWith("B-")) {
              close = true;
            }
            if (predictions[j + 1].equals("O")) {
              close = true;
            }
            if (predictions[j + 1].indexOf('-') > -1
                && !predictions[j].endsWith(predictions[j + 1].substring(2))) {
              close = true;
            }
          }

          if (close) {
            prob = prob / probcount;
            open = false;
            final String mention = word.trim();
            if (!illinois(tag).equals(BILOUEncoding.O)) {
              final int index = input.substring(offset + sumlength, input.length()).indexOf(mention)
                  + sumlength + offset;

              list.add(new Entity(mention, illinois(tag), Entity.DEFAULT_RELEVANCE, getToolName(),
                  index));
            }
            sumlength += mention.length();
          }
        }
      }
      offset += sentenceSize + 1;
    }
    return list;
  }

  protected double shapePred(final NEWord w, final String tag) {
    switch (illinois(tag)) {
      case EntityTypes.L:
        return w.shapePredLoc;
      case EntityTypes.P:
        return w.shapePredPer;
      case EntityTypes.O:
        return w.shapePredOrg;
    }
    return -1;
  }

  /**
   * Gets the entity class for a illinois entity type/class.
   */
  public static String illinois(final String tag) {
    switch (tag) {
      case "LOC":
        return EntityTypes.L;
      case "ORG":
        return EntityTypes.O;
      case "PER":
        return EntityTypes.P;
    }
    return BILOUEncoding.O;
  }
}
