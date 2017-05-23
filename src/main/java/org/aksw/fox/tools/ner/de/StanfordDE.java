package org.aksw.fox.tools.ner.de;

import java.util.Properties;

import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.common.StanfordCommon;
import org.aksw.fox.utils.FoxConst;

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

    entityClasses.put("I-ORG", EntityClassMap.O);
    entityClasses.put("I-LOC", EntityClassMap.L);
    entityClasses.put("I-PER", EntityClassMap.P);
    entityClasses.put("O", EntityClassMap.N);
    entityClasses.put("I-MISC", EntityClassMap.N);

    entityClasses.put("B-ORG", EntityClassMap.O);
    entityClasses.put("B-LOC", EntityClassMap.L);
    entityClasses.put("B-PER", EntityClassMap.P);
    entityClasses.put("O", EntityClassMap.N);
    entityClasses.put("B-MISC", EntityClassMap.N);
  }

  public static void main(final String[] a) {
    LOG.info(new StanfordDE().retrieve(FoxConst.NER_GER_EXAMPLE_1));
  }
}
