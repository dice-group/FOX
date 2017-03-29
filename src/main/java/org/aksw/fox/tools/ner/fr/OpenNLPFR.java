package org.aksw.fox.tools.ner.fr;

import org.aksw.fox.tools.ner.common.OpenNLPCommon;
import org.aksw.fox.utils.FoxConst;

public class OpenNLPFR extends OpenNLPCommon {

  static final String[] modelPath = {"data/openNLP/fr-ner-location.bin",
      "data/openNLP/fr-ner-organization.bin", "data/openNLP/fr-ner-person.bin"};

  public OpenNLPFR() {
    super(modelPath);
  }

  public static void main(final String[] a) {
    LOG.info(new OpenNLPFR().retrieve(FoxConst.NER_FR_EXAMPLE_1));
  }
}
