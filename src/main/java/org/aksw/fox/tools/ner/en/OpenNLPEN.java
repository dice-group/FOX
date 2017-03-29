package org.aksw.fox.tools.ner.en;

import org.aksw.fox.tools.ner.common.OpenNLPCommon;
import org.aksw.fox.utils.FoxConst;

public class OpenNLPEN extends OpenNLPCommon {

  static final String[] modelPath = {"data/openNLP/en-ner-person.bin",
      "data/openNLP/en-ner-location.bin", "data/openNLP/en-ner-organization.bin"};

  public OpenNLPEN() {
    super(modelPath);
  }

  public static void main(final String[] a) {
    LOG.info(new OpenNLPEN().retrieve(FoxConst.NER_EN_EXAMPLE_1));
  }
}
