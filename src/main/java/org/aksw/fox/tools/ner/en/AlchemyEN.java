package org.aksw.fox.tools.ner.en;

import org.aksw.fox.tools.ner.common.AlchemyCommon;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class AlchemyEN extends AlchemyCommon {

  public static void main(final String[] a) {
    PropertyConfigurator.configure(FoxCfg.LOG_FILE);

    new AlchemyEN().retrieve(FoxConst.NER_EN_EXAMPLE_1).forEach(LOG::info);
  }
}
