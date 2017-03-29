package org.aksw.fox.tools.ner.fr;

import org.aksw.fox.tools.ner.common.BalieCommon;
import org.aksw.fox.utils.FoxConst;

import ca.uottawa.balie.Balie;

public class BalieFR extends BalieCommon {

  public BalieFR() {
    super(Balie.LANGUAGE_FRENCH);
  }

  public static void main(final String[] a) {
    LOG.info(new BalieFR().retrieve(FoxConst.NER_FR_EXAMPLE_2));
  }
}
