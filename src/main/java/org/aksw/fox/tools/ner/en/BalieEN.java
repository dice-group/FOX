package org.aksw.fox.tools.ner.en;

import org.aksw.fox.tools.ner.common.BalieCommon;
import org.aksw.fox.utils.FoxConst;

public class BalieEN extends BalieCommon {

  public static void main(final String[] a) {
    LOG.info(new BalieEN().retrieve(FoxConst.NER_EN_EXAMPLE_1));
  }
}
