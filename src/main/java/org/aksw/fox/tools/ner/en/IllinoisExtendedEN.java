package org.aksw.fox.tools.ner.en;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
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
      LOG.error("\n", e);
    }
    tagger1 = new NETaggerLevel1(ParametersForLbjCode.currentParameters.pathToModelFile + ".level1",
        ParametersForLbjCode.currentParameters.pathToModelFile + ".level1.lex");
    tagger2 = new NETaggerLevel2(ParametersForLbjCode.currentParameters.pathToModelFile + ".level2",
        ParametersForLbjCode.currentParameters.pathToModelFile + ".level2.lex");
  }

  @Override
  public List<Entity> retrieve(final String input) {
    LOG.info("retrieve ...");

    // parse input
    final Vector<LinkedVector> sentences = PlainTextReader.parseText(input);
    final NERDocument doc = new NERDocument(sentences, "input");
    final Data data = new Data(doc);

    // set input
    try {
      ExpressiveFeaturesAnnotator.annotate(data);
    } catch (final Exception e) {
      LOG.error("\n", e);
    }

    // annotate input
    List<Entity> list = null;
    try {
      Decoder.annotateDataBIO(data, tagger1, tagger2);
      list = getEntities(sentences);
    } catch (final Exception e) {
      LOG.error("\n", e);
    }

    return list;
  }

  public List<Entity> getEntities(final Vector<LinkedVector> sentences) throws Exception {
    final List<Entity> list = new ArrayList<>();
    StringBuffer res = new StringBuffer();
    for (int i = 0; i < sentences.size(); i++) {
      final LinkedVector vector = sentences.elementAt(i);
      boolean open = false;
      final String[] predictions = new String[vector.size()];
      final String[] words = new String[vector.size()];
      for (int j = 0; j < vector.size(); j++) {
        predictions[j] = ((NEWord) vector.get(j)).neTypeLevel2;
        words[j] = ((NEWord) vector.get(j)).form;
      }

      String word = "";
      String tag = "";
      float prob = 0f;
      int probcount = 0;
      NEWord w = null;
      for (int j = 0; j < vector.size(); j++) {
        w = (NEWord) vector.get(j);
        if (predictions[j].startsWith("B-") || j > 0 && predictions[j].startsWith("I-")
            && !predictions[j - 1].endsWith(predictions[j].substring(2))) {
          res.append("[" + predictions[j].substring(2) + " ");
          tag = predictions[j].substring(2);
          prob = 0f;
          word = new String();
          open = true;
        }

        res.append(words[j] + " ");
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
            // SWM: makes the output a little cleaner
            final String str_res = res.toString().trim();
            res = new StringBuffer(str_res);
            res.append("] ");
            open = false;
            if (EntityClassMap.illinois(tag) != EntityClassMap.getNullCategory()) {
              // if ((FoxCfg.get("illinoisDefaultRelevance") == null)
              // || Boolean.valueOf(FoxCfg.get("illinoisDefaultRelevance"))) {
              prob = Entity.DEFAULT_RELEVANCE;
              // }
              list.add(getEntity(word, EntityClassMap.illinois(tag), prob, getToolName()));
            }
          }
        }
      }
    }
    return list;
  }

  protected double shapePred(final NEWord w, final String tag) {
    switch (EntityClassMap.illinois(tag)) {
      case EntityClassMap.L:
        return w.shapePredLoc;
      case EntityClassMap.P:
        return w.shapePredPer;
      case EntityClassMap.O:
        return w.shapePredOrg;
    }
    return -1;
  }

}
