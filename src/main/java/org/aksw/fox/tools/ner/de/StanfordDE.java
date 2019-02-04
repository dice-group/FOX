package org.aksw.fox.tools.ner.de;

import java.util.Properties;

import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.encode.BILOUEncoding;
import org.aksw.fox.tools.ner.common.StanfordCommon;

/**
 *
 * @author rspeck
 *
 */
public class StanfordDE extends StanfordCommon {

  /**
   * <code>
  
     https://github.com/stanfordnlp/CoreNLP/blob/master/src/edu/stanford/nlp/pipeline/StanfordCoreNLP-german.properties
  
     </code>
   */
  private static Properties props = new Properties();

  static {
    /* props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse"); */
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
    props.setProperty("tokenize.language", "de");
    props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/german/german-hgc.tagger");
    props.setProperty("ner.model", "edu/stanford/nlp/models/ner/german.hgc_175m_600.crf.ser.gz");
    props.setProperty("ner.applyNumericClassifiers", "false");
    props.setProperty("ner.useSUTime", "false");

    /*
     * props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/germanFactored.ser.gz");
     */
  }

  public StanfordDE() {
    super(props);

    entityClasses.put("I-ORG", EntityTypes.O);
    entityClasses.put("I-LOC", EntityTypes.L);
    entityClasses.put("I-PER", EntityTypes.P);
    entityClasses.put("O", BILOUEncoding.O);
    entityClasses.put("I-MISC", BILOUEncoding.O);

    entityClasses.put("B-ORG", EntityTypes.O);
    entityClasses.put("B-LOC", EntityTypes.L);
    entityClasses.put("B-PER", EntityTypes.P);
    entityClasses.put("O", BILOUEncoding.O);
    entityClasses.put("B-MISC", BILOUEncoding.O);
  }
}
