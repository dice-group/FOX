package org.aksw.fox.tools.ner.en;

import java.util.Properties;

import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.encode.BILOUEncoding;
import org.aksw.fox.tools.ner.common.StanfordCommon;

/**
 *
 * @author rspeck
 *
 */
public class StanfordEN extends StanfordCommon {

  private static Properties props = new Properties();
  static {
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
    props.setProperty("tokenize.language", "en");
    props.setProperty("ner.applyNumericClassifiers", "false");
    props.setProperty("ner.useSUTime", "false");
    props.setProperty("ner.model",
        "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
  }

  public StanfordEN() {
    super(props);

    entityClasses.put("ORGANIZATION", EntityTypes.O);
    entityClasses.put("LOCATION", EntityTypes.L);
    entityClasses.put("PERSON", EntityTypes.P);
    entityClasses.put("PEOPLE", EntityTypes.P);
    entityClasses.put("O", BILOUEncoding.O);
  }
}
