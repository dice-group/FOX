package org.aksw.fox.tools.ner.fr;

import java.util.Locale;

import org.aksw.fox.tools.ner.common.SpotlightCommon;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class SpotlightFR extends SpotlightCommon {

  public SpotlightFR() {
    super(Locale.FRENCH);
  }

  public static void main(final String[] a) {
    PropertyConfigurator.configure(FoxCfg.LOG_FILE);
    LOG.info(new SpotlightFR().retrieve(FoxConst.NER_FR_EXAMPLE_1));
  }
}
