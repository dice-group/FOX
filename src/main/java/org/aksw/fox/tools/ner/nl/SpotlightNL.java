package org.aksw.fox.tools.ner.nl;

import java.util.Locale;

import org.aksw.fox.tools.ner.common.SpotlightCommon;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class SpotlightNL extends SpotlightCommon {
  public SpotlightNL() {
    super(new Locale("nl"));
  }

  public static void main(final String[] a) {
    PropertyConfigurator.configure(FoxCfg.LOG_FILE);
    LOG.info(new SpotlightNL().retrieve(FoxConst.NER_NL_EXAMPLE_2));
  }
}
