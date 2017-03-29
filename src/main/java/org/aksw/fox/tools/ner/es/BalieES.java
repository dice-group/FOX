package org.aksw.fox.tools.ner.es;

import org.aksw.fox.tools.ner.common.BalieCommon;
import org.aksw.fox.utils.FoxConst;

import ca.uottawa.balie.Balie;

public class BalieES extends BalieCommon {

  public BalieES() {
    super(Balie.LANGUAGE_SPANISH);
  }

  public static void main(final String[] a) {
    LOG.info(new BalieES().retrieve(FoxConst.NER_ES_EXAMPLE_1));
  }
}
