package org.aksw.fox.tools.ner.en;

import org.aksw.fox.tools.ner.common.AlchemyCommon;
import org.aksw.fox.utils.FoxConst;

public class AlchemyEN extends AlchemyCommon {

  public static void main(final String[] a) {
    new AlchemyEN().retrieve(FoxConst.NER_EN_EXAMPLE_2).forEach(LOG::info);
  }
}
