package org.aksw.fox.tools.ner.de;

import org.aksw.fox.tools.ner.common.BalieCommon;
import org.aksw.fox.utils.FoxConst;

import ca.uottawa.balie.Balie;

public class BalieDE extends BalieCommon {

  public BalieDE() {
    super(Balie.LANGUAGE_GERMAN);
  }

  public static void main(final String[] a) {
    LOG.info(new BalieDE().retrieve(FoxConst.NER_GER_EXAMPLE_1));
  }
}
