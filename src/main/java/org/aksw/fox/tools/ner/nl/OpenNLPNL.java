package org.aksw.fox.tools.ner.nl;

import org.aksw.fox.tools.ner.common.OpenNLPCommon;

public class OpenNLPNL extends OpenNLPCommon {

  static final String[] modelPath = {"data/openNLP/nl-ner-person.bin",
      "data/openNLP/nl-ner-location.bin", "data/openNLP/nl-ner-organization.bin"};

  public OpenNLPNL() {
    super(modelPath);
  }

}
