package org.aksw.fox.tools.ner.en;

import org.aksw.fox.tools.ner.common.OpenNLPCommon;

public class OpenNLPEN extends OpenNLPCommon {

  static final String[] modelPath = {"data/openNLP/en-ner-person.bin",
      "data/openNLP/en-ner-location.bin", "data/openNLP/en-ner-organization.bin"};

  public OpenNLPEN() {
    super(modelPath);
  }

}
