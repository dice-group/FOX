package org.aksw.fox.tools.ner.de;

import java.util.Locale;

import org.aksw.fox.tools.ner.common.SpotlightCommon;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class SpotlightDE extends SpotlightCommon {
  public SpotlightDE() {
    super(Locale.GERMAN);
  }

  public static void main(final String[] a) {
    PropertyConfigurator.configure(FoxCfg.LOG_FILE);
    LOG.info(new SpotlightDE().retrieve(FoxConst.NER_GER_EXAMPLE_1));
  }
}
